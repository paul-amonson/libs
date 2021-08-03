// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import com.amonson.prop_store.PropMap;
import com.amonson.prop_store.PropStore;
import com.amonson.prop_store.PropStoreFactory;
import com.amonson.prop_store.PropStoreFactoryException;
import org.zeromq.SocketType;
import org.zeromq.ZMQException;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.concurrent.Callable;

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
     * @throws RuntimeException if the JSON parser cannot be created.
     */
    public ZeroMQLogSubscriber(String url, ReceivedLogMessageHandler messageCallback, String... topics) {
        url_ = url;
        callback_ = messageCallback;
        topics_ = topics;
        try {
            parser_ = PropStoreFactory.getStore("json");
        } catch(PropStoreFactoryException e) {
            throw new RuntimeException(e);
        }
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
        if(thread_ != null && !thread_.isInterrupted()) {
            thread_.interrupt();
            try { thread_.join(500); } catch(InterruptedException e) { /* Ignore */ }
        }
    }

    /**
     * Test if the server is running.
     *
     * @return true is the server is in the receiving loop, false otherwise.
     */
    public boolean isRunning() { return thread_ != null; }

    private void internalRun() throws Exception {
        if(isRunning())
            throw new IOException("Server is already Running!");
        try (ZMQ.Socket subscriber = creator_.create()) {
            subscriber.bind(url_);
            if(topics_.length > 0)
                for(String topic: topics_)
                    subscriber.subscribe(topic);
            else
                subscriber.subscribe(""); // subscribe to all topics.
            Thread.sleep(100); // Apparently there is a bug and this is required.
            thread_ = Thread.currentThread();
            while(!Thread.currentThread().isInterrupted()) {
                byte[] topic = subscriber.recv(0);
                byte[] message = subscriber.recv(0);
                if (callback_ != null) {
                    PropMap map = parser_.fromStringToMap(new String(message, ZMQ.CHARSET));
                    callback_.received(new String(topic, ZMQ.CHARSET),
                            LogRecordSerialization.deserializeLogRecord(map),
                            map.getStringOrDefault("hostname", null),
                            map.getIntegerOrDefault("pid", 0));
                }
            }
        } catch(ZMQException e) {
            if(e.getErrorCode() != 4)
                throw new IOException(e);
        } finally {
            thread_ = null;
        }
    }

    private ZMQ.Socket createSocket() {
        return ctx_.socket(SocketType.SUB);
    }

    private final PropStore parser_;
    private final String url_;
    private final String[] topics_;
    private final ZMQ.Context ctx_;
    private       Thread thread_;
    private final ReceivedLogMessageHandler callback_;
    private       SocketCreator creator_ = this::createSocket;

    @FunctionalInterface
    interface SocketCreator {
        ZMQ.Socket create();
    }
}
