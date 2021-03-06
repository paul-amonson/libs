// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import com.amonson.prop_store.*;
import org.zeromq.SocketType;
import org.zeromq.ZMQException;
import org.zeromq.ZMQ;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Create a handler to use ZeroMQ PUB to a SUB server (i.e. PUB connect to SUB). The topic use to send to is the
 * hostname of the sending logs.
 */
public class ZeroMQPublishHandler extends Handler {
    /**
     * Create a ZeroMQ PUB Handler for logging.
     *
     * @param zeroMQUrl The zeroMQ URL to connect to (must be a SUB server).
     * @param topic  The topic to publish to at the above url.
     * @throws RuntimeException when the JSON parser cannot be created. This should be very rare.
     */
    public ZeroMQPublishHandler(String zeroMQUrl, String topic) {
        url_ = zeroMQUrl;
        topic_ = topic;
        try {
            store_ = PropStoreFactory.getStore("json");
        } catch(PropStoreFactoryException e) {
            throw new RuntimeException("Failed to create a JSON parser instance!", e);
        }
        ctx_ = ZMQ.context(1);
    }

    /**
     * Send the topic and message to the PUB socket connected to the SUB server.
     *
     * @param logRecord The LogRecord given from the logger.
     * @throws RuntimeException when the log record cannot be sent. 15 retries will be tried before giving up.
     */
    @Override
    public void publish(LogRecord logRecord) {
        if(publish_ == null)
            repairConnection();
        publishZeroMQ(getFormatter().format(logRecord));
    }

    private synchronized void publishZeroMQ(String message) {
        boolean sent = false;
        int retries = 0;
        while(!sent && retries < 15) {
            try {
                publish_.send(topic_, ZMQ.SNDMORE);
                publish_.send(message);
                sent = true;
            } catch (ZMQException | NullPointerException e) {
                repairConnection();
                retries++;
            }
        }
        if(retries >= 15)
            throw new RuntimeException("Failed to send message: " + message);
    }

    @Override public void flush() { }
    @Override public void close() throws SecurityException { }

    private void repairConnection() {
        if(publish_ != null) {
            try {
                publish_.disconnect(url_);
            } catch(ZMQException e) { /* Ignore error as we are resetting... */ }
        }
        publish_ = creator_.create();
        publish_.connect(url_);
        try { Thread.sleep(50); } catch(InterruptedException e2) { /* */ }
    }

    private ZMQ.Socket createSocket() {
        return ctx_.socket(SocketType.PUB);
    }

    private final PropStore store_;
    private final String url_;
    private final String topic_;
    private final ZMQ.Context  ctx_;
    private       ZMQ.Socket publish_ = null;
    private       SocketCreator creator_ = this::createSocket;

    @FunctionalInterface
    interface SocketCreator {
        ZMQ.Socket create();
    }
}
