// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Default description for class NodeMonitoringClientZeroMQ
 */
class NodeMonitoringClientZeroMQ implements NodeMonitoringClient {
    NodeMonitoringClientZeroMQ(String myHostname, List<String> allHostnames, int port, Logger logger) {
        if(myHostname == null || myHostname.isBlank())
            throw new IllegalArgumentException("The 'myHostname' must not be null or empty!");
        if(port < 1024)
            throw new IllegalArgumentException("Cannot be a privileged port!");
        if(port > 65534)
            throw new IllegalArgumentException("The 'port' cannot be greater than 65534 because the " +
                    "'port+1' is used in the client!");
        if(logger == null)
            throw new IllegalArgumentException("The 'logger' cannot be null!");
        log_ = logger;
        me_ = myHostname;
        url_ = String.format("tcp://localhost:%d", port + 1);
        if(allHostnames != null)
            validNodes_.addAll(allHostnames);
        validNodes_.add(me_);
    }

    @Override
    public boolean sendMessage(Message message) {
        if(message.getTargetsAsString().equals("*"))
            message.replaceTargets(validNodes_);
        if(!message.getSender().equals(me_))
            throw new IllegalArgumentException("You cannot spoof the sender, please use the getMyHostname() " +
                    "method to get the correct sender!");
        ZMsg msg = convertMessage(message);
        createSocket();
        if(socket_ == null || !indirectSend_.send(msg, socket_)) {
            log_.warning("Failed to send the message to the open socket. Is the socket open?");
            return false;
        }
        return true;
    }

    @Override
    public String getMyHostname() {
        return me_;
    }

    @Override
    public void close() throws Exception {
        if(socket_ != null) {
            socket_.close();
            socket_ = null;
        }
        if(ctx_ != null) {
            ctx_.close();
            ctx_ = null;
        }
    }

    private void createSocket() {
        if(ctx_ == null)
            ctx_ = indirectCall_.create(1);
        if(socket_ == null) {
            socket_ = ctx_.socket(SocketType.PUSH);
            if(socket_ == null) {
                log_.warning("Failed to create a PUSH socket!");
            } else if(!socket_.connect(url_)) {
                log_.warning("Failed to connect to the specified port at localhost!");
                socket_.close();
                socket_ = null;
            }
        }
    }

    private ZMsg convertMessage(Message message) {
        ZMsg msg = ZMsg.newStringMsg(message.getTopic(), message.getSender(), message.getTargetsAsString());
        for(String part: message.getMessagePartsIterable())
            msg.add(part);
        return msg;
    }

    private ZMQ.Context indirectCtx(int threads) {
        return ZMQ.context(threads);
    }

    private boolean indirectSend(ZMsg msg, ZMQ.Socket socket) {
        return msg.send(socket);
    }

    private final Logger log_;
    private final String me_;
    private final String url_;
    private final Set<String> validNodes_ = new HashSet<>();
    private       IndirectCtx indirectCall_ = this::indirectCtx;
    private       IndirectSend indirectSend_ = this::indirectSend;

    private ZMQ.Context ctx_ = null;
    private ZMQ.Socket socket_ = null;

    @FunctionalInterface private interface IndirectCtx { ZMQ.Context create(int threads); }
    @FunctionalInterface private interface IndirectSend { boolean send(ZMsg msg, ZMQ.Socket socket); }
}
