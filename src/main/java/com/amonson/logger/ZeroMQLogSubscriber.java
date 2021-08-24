// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import org.zeromq.SocketType;
import org.zeromq.ZMQException;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to receive data from the ZeroMQPublishHandler class.
 */
public class ZeroMQLogSubscriber implements Runnable, Callable<Void> {
    /**
     * Construct a class to run a server for the ZMQPublishHandler.
     *
     * @param url The ZeroMQ URL to bind to. The address may be '*' to bind to all interfaces.
     * @param messageCallback The callback that will receive all published log messages.
     * @param topics The topics to listen on. If no topics are specified then all are listened to.
     */
    public ZeroMQLogSubscriber(String url, ReceivedLogMessageHandler messageCallback, String... topics) {
        url_ = url;
        callback_ = messageCallback;
        topics_ = topics;
        ctx_ = ZMQ.context(1);
    }

    /**
     * Method implements the Runnable interface.
     *
     * @throws RuntimeException encapsulating the checked exception from call().
     */
    @Override
    public void run() {
        try {
            call();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start the subscriber as a server and listen for messages.
     *
     * @return null for the Void generic parameter.
     * @throws Exception Required by the Callable functional interface.
     */
    @Override
    public Void call() throws Exception {
        internalRun();
        return null;
    }

    /**
     * Signal the server receive loop to stop.
     */
    public void signalStopServer() {
        if(isRunning())
            zeroMQThread_.interrupt();
    }

    /**
     * Test if the server is running.
     *
     * @return true is the server is in the receiving loop, false otherwise.
     */
    public boolean isRunning() { return running_.get(); }

    private void internalRun() throws Exception {
        if(isRunning())
            throw new IOException("Server is already Running!");
        running_.set(true);
        zeroMQThread_ = Thread.currentThread();
        try (ZMQ.Socket subscriber = ctx_.socket(SocketType.SUB)) {
            if(topics_.length > 0)
                for(String topic: topics_)
                    subscriber.subscribe(topic);
            else
                subscriber.subscribe(""); // subscribe to all topics.
            subscriber.bind(url_);
            while(!Thread.currentThread().isInterrupted()) {
                String topic = subscriber.recvStr(0);
                if(topic != null) {
                    String message = subscriber.recvStr(0);
                    if (message != null && callback_ != null)
                        callback_.received(topic, message);
                }
            }
        } catch(ZMQException e) {
            if(e.getErrorCode() != 4)
                throw new IOException(e);
        } finally {
            zeroMQThread_ = null;
            running_.set(false);
        }
    }

    private final String url_;
    private final String[] topics_;
    private final ReceivedLogMessageHandler callback_;
    private final AtomicBoolean running_ = new AtomicBoolean(false);
    private       ZMQ.Context ctx_; // Not final for testing...
    private       Thread zeroMQThread_ = null;
}
