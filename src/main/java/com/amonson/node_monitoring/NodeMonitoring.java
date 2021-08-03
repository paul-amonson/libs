// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.net.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class to participate in a cluster as a mesh where all nodes are monitored to be active or missing by every
 * other node.
 */
public class NodeMonitoring {
    /**
     * ctor to create a new monitoring class. This will consume 4 threads, but they are mostly light weight.
     *
     * @param ips The array of IPs or hostnames in this cluster. It must be the complete list.
     * @param eventCallback Called when the online/offline state of a remote node changes.
     * @param logger The logger used to log warnings, errors, or debug information.
     * @throws IllegalArgumentException if the networking cannot correctly navigate the local NICs.
     */
    public NodeMonitoring(String[] ips, NodeEventHandler eventCallback, Logger logger) {
        for(String ip: ips)
            lastSeen_.put(ip, 0L);
        events_ = eventCallback;
        log_ = logger;
        handlers_.put(ALIVE, this::processHeartbeat);

        if (getNetworkEnvironment())
            throw new IllegalArgumentException("Failed to include this node in the list of all nodes!");
    }

    /**
     * The functional interface for registered incoming messages.
     */
    @FunctionalInterface
    public interface MessageHandler { void handleMessage(String message); }

    /**
     * Adds a new MessageHandler callback for the specified topic. This will not overwrite an entry.
     *
     * @param topic The topic to add the callback handler for.
     * @param handler The callback to call when a message is received for the specified topic.
     * @return The current value is already set or null if the handler was added.
     * @throws IllegalArgumentException is topic is null or 'alive'. Also thrown when the handler is null.
     */
    public MessageHandler addHandler(String topic, MessageHandler handler) {
        if(topic == null)
            throw new IllegalArgumentException("The topic cannot be 'null!");
        if(handler == null)
            throw new IllegalArgumentException("The handler cannot be 'null!");
        if(topic.equals(ALIVE))
            throw new IllegalArgumentException("The topic 'alive' is reserved for heartbeat functionality!");
        return handlers_.putIfAbsent(topic, handler);
    }

    /**
     * Removes a previously added MessageHandler for a specific topic.
     *
     * @param topic The topic to remove.
     * @return The removed handler or null if the the topic was not found.
     * @throws IllegalArgumentException is topic is null or 'alive'.
     */
    public MessageHandler removeHandler(String topic) {
        if(topic == null)
            throw new IllegalArgumentException("The topic cannot be 'null!");
        if(topic.equals(ALIVE))
            throw new IllegalArgumentException("The topic 'alive' is reserved for heartbeat functionality!");
        return handlers_.remove(topic);
    }

    /**
     * If not running this will start the monitoring threads. This can only be called once per instance.
     */
    public void startMonitoring() {
        if(subscriberThread_ == null) {
            startSendThread();
            startHeartbeat();
            startMonitoringInternal();
            startSubscriber();
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) { /* Ignored */ }
        }
    }

    /**
     * If running this will stop the monitoring threads.
     */
    public void stopMonitoring() {
        if(subscriberThread_ != null) {
            stopThread(subscriberThread_);
            stopThread(monitoringThread_);
            stopThread(heartbeatThread_);
            stopThread(sendThread_);
        }
    }

    /**
     * Send a message to all other nodes in this participating in the monitoring cluster.
     *
     * @param topic The topic to send the message for.
     * @param message The message to publish.
     * @throws IllegalArgumentException is topic is null or 'alive'. Also thrown when the message is null.
     */
    public void sendMessage(String topic, String message) {
        if(topic == null)
            throw new IllegalArgumentException("The topic cannot be 'null!");
        if(message == null)
            throw new IllegalArgumentException("The message cannot be 'null!");
        if(topic.equals(ALIVE))
            throw new IllegalArgumentException("The topic 'alive' is reserved for heartbeat functionality!");
        sendQueue_.add(new TopicMessage(topic, message));
    }

    /**
     * Test if the monitoring is running.
     *
     * @return true when the monitoring is running, false otherwise.
     */
    public boolean isRunning() {
        return subscriberThread_ != null && subscriberThread_.isAlive();
    }

    /**
     * Wait indefinitly or until an interrupt occurs while for the monitoring thread.
     *
     * @throws InterruptedException If interrupted while waiting.
     */
    public void waitForMonitoring() throws InterruptedException {
        if(isRunning())
            subscriberThread_.join();
    }

    /**
     * Functional interface for online/offline events for remote nodes.
     */
    @FunctionalInterface
    public interface NodeEventHandler {
        void event(String ip, boolean online);
    }

    private void fireEvent(String ip, boolean present) {
        if(events_ != null)
            events_.event(ip, present);
    }

    private void startMonitoringInternal() {
        monitoringThread_ = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(HEARTBEAT_SECONDS * 500L);
                } catch(InterruptedException e) {
                    break; // Exit if interrupt happens here!
                }
                for(Map.Entry<String,Long> entry: lastSeen_.entrySet()) {
                    long now = Instant.now().toEpochMilli();
                    if(entry.getValue() < (now - EXPIRED_MILLISECONDS) && entry.getValue() != 0L) {
                        fireEvent(entry.getKey(), false);
                        lastSeen_.put(entry.getKey(), 0L);
                    }
                }
            }
        });
        monitoringThread_.start();
    }

    private void startHeartbeat() {
        heartbeatThread_ = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                log_.fine("*** DEBUG: PING...");
                sendQueue_.add(new TopicMessage(ALIVE, myIP_));
                try {
                    Thread.sleep(HEARTBEAT_SECONDS * 1_000L);
                } catch(Exception e) {
                    break; // Exit if interrupt happens here!
                }
            }
        });
        heartbeatThread_.start();
    }

    private void startSendThread() {
        sendThread_ = new Thread(() -> {
            ZMQ.Context ctx = ZMQ.context(1);
            ZMQ.Socket publisher = creator_.create(ctx, SocketType.PUB);
            for(String ip: lastSeen_.keySet())
                publisher.connect(String.format("tcp://%s:%d", ip, port_));

            while(!Thread.currentThread().isInterrupted()) {
                if(sendQueue_.size() > 0) {
                    TopicMessage msg = sendQueue_.poll();
                    publisher.sendMore(msg.topic());
                    publisher.send(msg.message());
                    log_.fine(msg.toString() + "; OK");
                }
            }
            try {
                publisher.close();
            } catch(Exception e) { /* Ignore because this is expected on exit. */}
            try {
                ctx.close();
            } catch(Exception e) { /* Ignore because this is expected on exit. */ }
        });
        sendThread_.start();
    }

    private void stopThread(Thread thread) {
        if(thread != null && thread.isAlive() && !thread.isInterrupted()) {
            thread.interrupt();
            try {
                thread.join();
            } catch(Exception e) { /* Ignore and hope the thread is stopped. */ }
        }
    }

    private void startSubscriber() {
        subscriberThread_ = new Thread(() -> {
            ZMQ.Context ctx = ZMQ.context(1);
            ZMQ.Socket subscriber = creator_.create(ctx, SocketType.SUB);
            subscriber.bind(String.format("tcp://%s:%d", myIP_, port_));
            subscriber.subscribe("");
            try {
                Thread.sleep(100); // For a race condition inside JeroMQ...
            } catch(InterruptedException e) { /* Ignored, but hope it works! */ }

            while(!Thread.currentThread().isInterrupted()) {
                try {
                    String topic = subscriber.recvStr(0);
                    if (topic != null) {
                        String msg = subscriber.recvStr(0);
                        processMessage(topic, msg);
                    } else {
                        log_.warning("Received a 'null' topic!");
                    }
                } catch(ZMQException e) {
                    ZMQ.Error err = ZMQ.Error.findByCode(e.getErrorCode());
                    if(err == ZMQ.Error.EINTR || err == ZMQ.Error.ETERM)
                        break;
                    log_.warning(String.format("%s: %s", e.getMessage(), err));
                } catch(Exception e) {
                    log_.warning(e.getMessage());
                }
            }
            try {
                subscriber.close();
            } catch(Exception e) { /* Ignore because this is expected on exit. */ }
            try {
                ctx.close();
            } catch(Exception e) { /* Ignore because this is expected on exit. */ }
        });
        subscriberThread_.start();
    }

    private void processMessage(String topic, String msg) {
        if(handlers_.containsKey(topic) && handlers_.get(topic) != null)
            handlers_.get(topic).handleMessage(msg);
        else
            log_.warning("### Dropped Message from topic: " + topic);
    }

    void processHeartbeat(String msg) {
        if(lastSeen_.containsKey(msg)) {
            long now = Instant.now().toEpochMilli();
            long last = lastSeen_.get(msg);
            if(last < (now - EXPIRED_MILLISECONDS) || last == 0L)
                fireEvent(msg, true);
            lastSeen_.put(msg, now);
        } else
            log_.fine("### Dropped Message from unknown IP: " + msg);
    }

    private boolean getNetworkEnvironment() {
        lastSeen_ = replaceHostsWithIPs(lastSeen_);
        try {
            Enumeration<NetworkInterface> enumer = NetworkInterface.getNetworkInterfaces();
            while(enumer.hasMoreElements())
                findMyAddress(enumer.nextElement());
        } catch(SocketException e) {
            log_.severe("ERROR: Failed to iterate NIC interfaces on this system!");
            return true;
        }
        if(myIP_ == null) {
            log_.severe("ERROR: Failed to find this systems IP which must also be in the passed IP list!");
            return true;
        }
        lastSeen_.remove(myIP_);
        return false;
    }

    private void findMyAddress(NetworkInterface nic) {
        for(InterfaceAddress address: nic.getInterfaceAddresses()) {
            InetAddress addr = address.getAddress();
            if(!addr.isLoopbackAddress() && addr.getAddress().length == 4) {
                String ip = addr.getHostAddress();
                if(lastSeen_.containsKey(ip))
                    myIP_ = ip;
            }
        }
    }

    private Map<String,Long> replaceHostsWithIPs(Map<String, Long> input) {
        Map<String, Long> output = new ConcurrentHashMap<>();
        for(String host: input.keySet()) {
            if(Pattern.compile("^[0-9]+[0-9]+[0-9]+[0-9]+$").matcher(host).matches())
                output.put(host, 0L);
            else {
                try {
                    output.put(InetAddress.getByName(host).getHostAddress(), 0L);
                } catch(UnknownHostException e) {
                    log_.warning(String.format("WARNING: Dropping '%s' as a unknown host!", host));
                }
            }
        }
        return output;
    }

    private ZMQ.Socket createSocket(ZMQ.Context ctx, SocketType type) { return ctx.socket(type); }

    private final Logger log_;
    private final int port_ = 23456;
    private final Map<String, MessageHandler> handlers_ = new HashMap<>();
    private final Queue<TopicMessage> sendQueue_ = new ConcurrentLinkedQueue<>();
    private final NodeEventHandler events_;
    private       String myIP_ = null;
    private       Map<String,Long> lastSeen_ = new HashMap<>();
    private       SocketCreator creator_ = this::createSocket; // To make UT easier....
    private       Thread sendThread_ = null;
    private       Thread heartbeatThread_ = null;
    private       Thread monitoringThread_ = null;
    private       Thread subscriberThread_ = null;

    private static final long HEARTBEAT_SECONDS = 1L;
    private static final long EXPIRED_MILLISECONDS = (HEARTBEAT_SECONDS * 1_000L * 5L) / 2L;
    private static final String ALIVE = "alive";

    @FunctionalInterface
    interface SocketCreator { ZMQ.Socket create(ZMQ.Context ctx, SocketType type); }

    private static class TopicMessage {
        TopicMessage(String topic, String message) { topic_ = topic; message_ = message; }

        String topic() { return topic_; }
        String message() { return message_; }

        private final String topic_;
        private final String message_;
    }
}
