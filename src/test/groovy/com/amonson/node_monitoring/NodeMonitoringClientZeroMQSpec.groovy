// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring

import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Logger

class NodeMonitoringClientZeroMQSpec extends Specification {
    static def CTX
    ZMQ.Socket socket_
    NodeMonitoringClientZeroMQ underTest_
    boolean send_
    void setup() {
        send_ = true
        CTX = Mock(ZMQ.Context)
        socket_ = Mock(ZMQ.Socket)
        underTest_ = NodeMonitoringFactory.createClient("zeromq",
                "me", Arrays.asList("me", "other"), 34534, Mock(Logger))
        underTest_.indirectCall_ = this::create
        underTest_.indirectSend_ = this::send
        CTX.socket(_ as SocketType) >> socket_
    }

    ZMQ.Context create(int threads) {
        return CTX
    }

    boolean send(ZMsg msg, ZMQ.Socket socket) {
        return send_
    }

    def "Test sendMessage"() {
        def message = new Message("topic","me", "*", "Hello World!")
        def rv = !underTest_.sendMessage(message)
        underTest_.socket_ = Mock(ZMQ.Socket)
        message = new Message("topic","me", "other", "Hello World!")
        expect: rv
        and:    underTest_.sendMessage(message)
    }

    def "Test sendMessage negative"() {
        def message = new Message("topic","not_me", "*", "Hello World!")
        when: underTest_.sendMessage(message)
        then: thrown(IllegalArgumentException)
    }

    def "Test close"() {
        underTest_.close()
        def message = new Message("topic","me", "*", "Hello World!")
        underTest_.sendMessage(message)
        underTest_.socket_ = Mock(ZMQ.Socket)
        underTest_.close()
        expect: underTest_.ctx_ == null
        and:    underTest_.socket_ == null
    }

    def "Test getMyHostname"() {
        expect: underTest_.getMyHostname() == "me"
    }

    def "Test indirectCtx"() {
        expect: underTest_.indirectCtx(1) != null
    }

    def "Test indirectSend"() {
        expect: underTest_.indirectSend(new ZMsg(), Mock(ZMQ.Socket))
    }

    def "Test ctor"() {
        expect: new NodeMonitoringClientZeroMQ("me", null, 34524, Mock(Logger)) != null
    }

    @Unroll("HOST=#HOST; PROT=#PORT; LOGGER=#LOGGER")
    def "Test ctor negative"() {
        when: new NodeMonitoringClientZeroMQ(HOST, null, PORT, LOGGER)
        then: thrown(IllegalArgumentException)
        where:
        HOST | PORT  | LOGGER
        null | 10000 | Mock(Logger)
        ""   | 10000 | Mock(Logger)
        "me" | 100   | Mock(Logger)
        "me" | 80000 | Mock(Logger)
        "me" | 10000 | null
    }
}
