// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import org.zeromq.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * This is a JeroMQ based (a specific Java implementation of ZeroMQ) implementation of the
 * {@link NodeMonitoring} interface.
 *
 * Specific implementation for this JeroMQ-based implementation are:
 *
 *      ZeroMQThreads      - (def. 1) This is the number of internal threads used by ZeroMQ for I/O. 1-4 is typical.
 */
class NodeMonitoringZeroMQ implements NodeMonitoring {
    @Override
    public void sendMessage(Message message) {
        if(zeroMQThreadId_ != Long.MIN_VALUE) {
            if(message.targetsContains("*")) {
                message.replaceTargets(lastSeen_.keySet());
                message.addTargets(me_);
            }
            ZMsg msg = convertMessage(message);
            boolean useRelay = Thread.currentThread().getId() != zeroMQThreadId_;
            if(useRelay)
                msg.addFirst(RELAY);
            ZMQ.Socket socket = publish_;
            if(useRelay) {
                log_.fine("Sending inproc message across threads first: " + message);
                socket = nonZeroMQThreads_.getOrDefault(Thread.currentThread().getId(), null);
                if (socket == null) {
                    socket = CTX.socket(SocketType.PUSH);
                    socket.connect(INPROC_URL);
                    nonZeroMQThreads_.put(Thread.currentThread().getId(), socket);
                }
            } else
                log_.fine("Sending message directly to cluster network: " + message);
            msg.send(socket);
        }
    }

    @Override
    public void start(boolean blocking) {
        if(zeroMQThreadId_ == Long.MIN_VALUE) {
            log_.fine(String.format("Starting %s listening on 'tcp://*:%d'...", blocking?"blocking":"non-blocking",
                    port_));
            if(blocking)
                startBlocking();
            else
                new Thread(this::startBlocking).start();
        }
    }

    @Override
    public void stop() {
        if(zeroMQThreadId_ != Long.MIN_VALUE) {
            log_.fine("Stopping listening.");
            if (zeroMQThreadId_ != Thread.currentThread().getId())
                sendMessage(new Message(STOP, me_, me_));
            else
                Thread.currentThread().interrupt();
            if(zeroMQThread_ != null) {
                try {
                    zeroMQThread_.join(60_000L);
                } catch(InterruptedException e) {
                    /* Ignore and assume thread finished. */
                } finally {
                    zeroMQThread_ = null;
                }
            }
            for(Map.Entry<Long,ZMQ.Socket> entry: nonZeroMQThreads_.entrySet())
                entry.getValue().close();
            nonZeroMQThreads_.clear();
        }
    }

    @Override
    public void waitForExitWhenOnThread() {
        if(zeroMQThread_ != null) {
            try {
                zeroMQThread_.join();
            } catch(InterruptedException e) { /* */ }
        }
    }

    @Override
    public boolean isRunning() {
        return zeroMQThreadId_ != Long.MIN_VALUE;
    }

    @Override
    public MessageHandler addOrReplaceMessageHandler(String topic, MessageHandler handler) {
        assert topic != null:"The 'topic' may not be null!";
        assert handler != null:"The 'handler' may not be null!";
        synchronized (messageHandlers_) {
            return messageHandlers_.put(topic, handler);
        }
    }

    @Override
    public void addMessageHandler(String topic, MessageHandler handler) {
        assert topic != null:"The 'topic' may not be null!";
        assert handler != null:"The 'handler' may not be null!";
        synchronized (messageHandlers_) {
            messageHandlers_.putIfAbsent(topic, handler);
        }
    }

    @Override
    public MessageHandler removeMessageHandler(String topic) {
        assert topic != null:"The 'topic' may not be null!";
        synchronized (messageHandlers_) {
            return messageHandlers_.remove(topic);
        }
    }

    @Override
    public boolean addNodeStateChangeHandler(NodeStateChangeHandler newHandler) {
        synchronized (nodeStateChangeHandler_) {
            return nodeStateChangeHandler_.add(newHandler);
        }
    }

    @Override
    public boolean removeNodeStateChangeHandler(NodeStateChangeHandler newHandler) {
        synchronized (nodeStateChangeHandler_) {
            return nodeStateChangeHandler_.remove(newHandler);
        }
    }

    @Override
    public String getMyHostname() {
        return me_;
    }

    @Override
    public Logger getLogger() {
        return log_;
    }

    NodeMonitoringZeroMQ(String myHostname, List<String> allHostnames, int port,
                         NodeStateChangeHandler nodeStateChangeHandler, Logger logger, Properties config) {
        if(myHostname == null || myHostname.isBlank())
            throw new IllegalArgumentException("The 'myHostname' must not be null or empty!");
        if(port < 1024)
            throw new IllegalArgumentException("Cannot be a privileged port!");
        if(port > 65534)
            throw new IllegalArgumentException("The 'port' cannot be greater than 65534 because both the 'port' " +
                    "and 'port+1' are used!");
        if(logger == null)
            throw new IllegalArgumentException("The 'logger' cannot be null!");
        me_ = myHostname;
        for(String hostname: allHostnames)
            if (!myHostname.equals(hostname))
                lastSeen_.put(hostname, new Pair<>(0L, RemoteNodeState.MISSING));
        port_ = port;
        log_ = logger;
        if(nodeStateChangeHandler != null)
            addNodeStateChangeHandler(nodeStateChangeHandler);
        configure(config);
        if(CTX == null)
            CTX = ZMQ.context(zeroMQThreadCount_);
        loop_ = new ZLoop(CTX);
    }

    private void configure(Properties config) {
        if(config != null) {
            expireDeltaMilliseconds_ = Long.parseLong(config.getProperty("NodeExpirationTime",
                    Long.toString(expireDeltaMilliseconds_)));
            checkPeriodMilliseconds_ = Long.parseLong(config.getProperty("NodeAlivePeriod",
                    Long.toString(checkPeriodMilliseconds_)));
            zeroMQThreadCount_ = Integer.parseInt(config.getProperty("ZeroMQThreads",
                    Integer.toString(zeroMQThreadCount_)));
            threadPoolCount_ = Integer.parseInt(config.getProperty("ThreadsPoolCount",
                    Integer.toString(threadPoolCount_)));
        }
    }

    private void startBlocking() {
        if(zeroMQThreadId_ == Long.MIN_VALUE) {
            zeroMQThreadId_ = Thread.currentThread().getId();
            startPool();
            reportAllMissing();
            String bindUrl = String.format("tcp://*:%d", port_);
            String connectUrl = String.format("tcp://%%s:%d", port_);
            ZMQ.Socket subExternal = CTX.socket(SocketType.SUB);
            subExternal.subscribe("");
            subExternal.bind(bindUrl);
            ZMQ.Socket pullLocalhost = CTX.socket(SocketType.PULL);
            pullLocalhost.bind(String.format("tcp://localhost:%d", port_ + 1));
            ZMQ.Socket inproc = CTX.socket(SocketType.PULL);
            inproc.bind(INPROC_URL);
            publish_ = CTX.socket(SocketType.PUB);
            for (String hostname : lastSeen_.keySet())
                publish_.connect(String.format(connectUrl, hostname));
            loop_.addPoller(new ZMQ.PollItem(pullLocalhost, ZMQ.Poller.POLLIN), this::localhostIncomingRequest, null);
            loop_.addPoller(new ZMQ.PollItem(subExternal, ZMQ.Poller.POLLIN), this::fromExternalCluster, null);
            loop_.addPoller(new ZMQ.PollItem(inproc, ZMQ.Poller.POLLIN), this::fromOtherThreadInProcess, null);
            loop_.addTimer((int) checkPeriodMilliseconds_, 0, this::timerTick, null);

            // Block this thread here...
            loop_.start();

            inproc.close();
            publish_.close();
            pullLocalhost.close();
            subExternal.close();
            stopPool();
            zeroMQThreadId_ = Long.MIN_VALUE;
        }
    }

    private void reportAllMissing() {
        for(Map.Entry<String,Pair<Long,RemoteNodeState>> entry: lastSeen_.entrySet())
            callNodeStateChangeHandlerOnThread(entry.getKey(), entry.getValue().second, entry.getValue().first);
    }

    // Incoming handler for tcp://localhost PULL incoming messages.
    private int localhostIncomingRequest(ZLoop zLoop, ZMQ.PollItem pollItem, Object o) {
        ZMsg msg = indirectCall_.call(pollItem.getSocket(), true);
        Message message = new Message(msg.removeFirst().getString(StandardCharsets.UTF_8),
                msg.removeFirst().getString(StandardCharsets.UTF_8),
                msg.removeFirst().getString(StandardCharsets.UTF_8), parseFrames(msg));
        if(message.targetsContains(me_))
            callMessageHandlerOnThread(message);
        sendMessage(message);
        return 0;
    }

    private String[] parseFrames(ZMsg msg) {
        String[] frames = new String[msg.size()];
        int i = 0;
        for(ZFrame frame: msg)
            frames[i++] = frame.getString(StandardCharsets.UTF_8);
        return frames;
    }

    private int timerTick(ZLoop zLoop, ZMQ.PollItem pollItem, Object o) {
        sendAlive();
        checkForMissingNodes();
        return 0;
    }

    // Incoming handler for tcp://* SUB incoming messages.
    private int fromExternalCluster(ZLoop loop, ZMQ.PollItem item, Object arg) {
        ZMsg recvMsg = indirectCall_.call(item.getSocket(), true);
        String topic = recvMsg.removeFirst().getString(StandardCharsets.UTF_8);
        String sender = recvMsg.removeFirst().getString(StandardCharsets.UTF_8);
        if(topic.equals(ALIVE)) {
            log_.finest(String.format("Received ALIVE message from '%s'!", sender));
            long previous = lastSeen_.get(sender).first;
            lastSeen_.get(sender).first = Instant.now().toEpochMilli();
            if(previous == 0L && nodeStateChangeHandler_ != null) {
                lastSeen_.get(sender).second = RemoteNodeState.ACTIVE;
                callNodeStateChangeHandlerOnThread(sender, lastSeen_.get(sender).second, lastSeen_.get(sender).first);
            }
            return 0;
        }
        Message message = new Message(topic, sender, recvMsg.removeFirst().getString(StandardCharsets.UTF_8));
        if(message.targetsContains(me_)) {
            MessageHandler handler = messageHandlers_.getOrDefault(topic, null);
            if(handler != null) {
                for(ZFrame frame: recvMsg)
                    message.addMessageParts(frame.getString(StandardCharsets.UTF_8));
                callMessageHandlerOnThread(message);
            }
        }
        return 0;
    }

    // Incoming handler for inproc:// PULL incoming messages.
    private int fromOtherThreadInProcess(ZLoop loop, ZMQ.PollItem item, Object arg) {
        ZMsg recvMsg = indirectCall_.call(item.getSocket(), true);
        String topic = recvMsg.removeFirst().getString(StandardCharsets.UTF_8);
        if(topic.equals(STOP))
            Thread.currentThread().interrupt();
        else if(topic.equals(RELAY))
            recvMsg.send(publish_);
        return 0;
    }

    private void sendAlive() {
        ZMsg msg = new ZMsg();
        msg.add(ALIVE);
        msg.add(me_);
        msg.send(publish_);
    }

    private void checkForMissingNodes() {
        long now = Instant.now().toEpochMilli();
        final List<String> missingList = new ArrayList<>();
        for(Map.Entry<String, Pair<Long,RemoteNodeState>> entry: lastSeen_.entrySet())
            if(entry.getValue().first > 0L && (now - entry.getValue().first) > expireDeltaMilliseconds_)
                missingList.add(entry.getKey());
        if(nodeStateChangeHandler_.size() > 0 && missingList.size() > 0)
            for (String node : missingList) {
                lastSeen_.get(node).first = 0L;
                lastSeen_.get(node).second = RemoteNodeState.MISSING;
                callNodeStateChangeHandlerOnThread(node, lastSeen_.get(node).second, lastSeen_.get(node).first);
            }
    }

    // Start the threads for the thread pool for callback tasks.
    private void startPool() {
        push_ = CTX.socket(SocketType.PUSH);
        push_.bind("inproc://thread_pool");
        // Pause for slow bind...
        try { Thread.sleep(100); } catch(InterruptedException e) { /* Ignore */ }
        for(int i = 0; i < threadPoolCount_; i++) {
            Thread t = new Thread(this::pullLoop);
            t.start();
            threadPool_.add(t);
        }
    }

    // Stop the threads for the thread pool for callback tasks.
    private void stopPool() {
        for(Thread t: threadPool_) {
            t.interrupt();
            try {
                t.join();
            } catch(InterruptedException e) { /* Ignore */ }
        }
        push_.close();
    }

    private ZMsg convertMessage(Message message) {
        ZMsg msg = ZMsg.newStringMsg(message.getTopic(), message.getSender(), message.getTargetsAsString());
        for(String part: message.getMessagePartsIterable())
            msg.add(part);
        return msg;
    }

    // Call MessageHandler on a separate thread to avoid blocking main loop.
    private void callMessageHandlerOnThread(Message message) {
        ZMsg msg = convertMessage(message);
        msg.addFirst("MessageHandler");
        msg.send(push_, false);
    }

    // Call NodeStateChangeHandler on a separate thread to avoid blocking main loop.
    private void callNodeStateChangeHandlerOnThread(String node, RemoteNodeState state, long lastSeen) {
        ZMsg msg = ZMsg.newStringMsg("NodeStateChangeHandler", node, state.toString(), Long.toString(lastSeen));
        msg.send(push_, false);
    }

    // ZeroMQ based worker threads.
    private void pullLoop() {
        ZMQ.Socket pull = CTX.socket(SocketType.PULL);
        pull.connect("inproc://thread_pool");
        while(!Thread.currentThread().isInterrupted()) {
            ZMsg msg = indirectCall_.call(pull, true);
            if(msg != null) {
                String call = msg.removeFirst().getString(StandardCharsets.UTF_8);
                if(call.equals("NodeStateChangeHandler")) {
                    String node = msg.removeFirst().getString(StandardCharsets.UTF_8);
                    RemoteNodeState state = RemoteNodeState.valueOf(msg.removeFirst().getString(StandardCharsets.UTF_8));
                    long seen = Long.parseLong(msg.removeFirst().getString(StandardCharsets.UTF_8));
                    synchronized (nodeStateChangeHandler_) {
                        for (NodeStateChangeHandler handler : nodeStateChangeHandler_)
                            handler.nodeStateChanged(node, state, seen);
                    }
                } else if(call.equals("MessageHandler")) {
                    String topic = msg.removeFirst().getString(StandardCharsets.UTF_8);
                    MessageHandler handler = messageHandlers_.getOrDefault(topic, null);
                    Message message = new Message(topic, msg.removeFirst().getString(StandardCharsets.UTF_8),
                            msg.removeFirst().getString(StandardCharsets.UTF_8), parseFrames(msg));
                    handler.handleMessage(message);
                } else
                    log_.warning("Unknown callback on thread: '" + call + "'!");
            }
        }
        pull.close();
    }

    // Constructor of instance set only
    private final Logger log_;
    private final String me_;
    private final Map<String, Pair<Long,RemoteNodeState>> lastSeen_ = new HashMap<>();
    private final int port_;
    private final Set<NodeStateChangeHandler> nodeStateChangeHandler_ = new HashSet<>();
    private final Map<Long, ZMQ.Socket> nonZeroMQThreads_ = new HashMap<>();
    private final Map<String, MessageHandler> messageHandlers_ = new HashMap<>();
    private final List<Thread> threadPool_ = new ArrayList<>();
    private       ZLoop loop_; // Not final for UT override.
    private       ZMsgReceiveMsg indirectCall_ = ZMsg::recvMsg; // For test overriding...

    // Variables
    private ZMQ.Socket publish_;
    private ZMQ.Socket push_;
    private long zeroMQThreadId_ = Long.MIN_VALUE;
    private Thread zeroMQThread_ = null;
    private long expireDeltaMilliseconds_ = 4_000L;
    private long checkPeriodMilliseconds_ = 1_900L;
    private int zeroMQThreadCount_ = 1;
    private int threadPoolCount_ = 3;

    // Static Variables
    private static ZMQ.Context CTX = null;
    private static NodeMonitoringZeroMQ instance_ = null;

    // Static Constants
    private static final String ALIVE = "ALIVE";
    private static final String RELAY = "RELAY";
    private static final String STOP = "STOP";
    private static final String INPROC_URL = "inproc://in_process";

    // Internal classes
    private static class Pair<T1,T2> {
        Pair(T1 first, T2 second) { this.first = first; this.second = second; }
        T1 first;
        T2 second;
    }

    @FunctionalInterface private interface ZMsgReceiveMsg { ZMsg call(ZMQ.Socket socket, boolean wait); }
}
