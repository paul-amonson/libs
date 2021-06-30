// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import org.zeromq.ZMQException;
import org.zeromq.ZSocket;
import zmq.ZMQ;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Create a handler to use ZeroMQ PUB to a SUB server (i.e. PUB connect to SUB).
 */
public class ZeroMQPublishHandler extends Handler {
    /**
     * Create a ZeroMQ PUB Handler for logging.
     *
     * @param zeroMQUrl The zeroMQ URL to connect to (must be a SUB server).
     * @param topic The topic to use when sending the log message.
     */
    public ZeroMQPublishHandler(String zeroMQUrl, String topic) {
        url_ = zeroMQUrl;
        topic_ = topic;
    }

    /**
     * Send the topic and message to the PUB socket connected to the SUB server.
     *
     * @param logRecord The LogRecord given from the logger.
     */
    @Override
    public void publish(LogRecord logRecord) {
        String message = getFormatter().format(logRecord);
        publishZeroMQ(message);
    }

    private synchronized void publishZeroMQ(String message) {
        boolean sent = false;
        while(!sent) {
            try {
                publish_.sendStringUtf8(topic_, ZMQ.ZMQ_MORE);
                publish_.sendStringUtf8(message);
                sent = true;
            } catch (ZMQException | NullPointerException e) {
                cleanupConnection();
                try { Thread.sleep(50); } catch(InterruptedException e2) { /* */ }
                connect();
            }
        }
    }

    @Override public void flush() { }
    @Override public void close() throws SecurityException { }

    private void cleanupConnection() {
        if(publish_ != null) {
            try {
                publish_.disconnect(url_);
            } catch(ZMQException e) { /* Ignore error as we are resetting... */ }
        }
        publish_ = creator_.create();
    }

    private void connect() {
        try {
            publish_.connect(url_);
        } catch(ZMQException e) { /* Ignore, the loop above will retry. */ }
    }

    private ZSocket createSocket() {
        return new ZSocket(ZMQ.ZMQ_PUB);
    }

    private final String url_;
    private final String topic_;
    private       ZSocket publish_ = null;
    private       SocketCreator creator_ = this::createSocket;

    @FunctionalInterface
    interface SocketCreator {
        ZSocket create();
    }
}
