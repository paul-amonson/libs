// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger

import org.zeromq.ZMQException
import org.zeromq.ZMQ
import spock.lang.Specification

import java.util.logging.LogRecord
import java.util.logging.Level

class ZeroMQPublishHandlerSpec extends Specification {
    ZMQ.Socket creator() {
        return Mock(ZMQ.Socket)
    }

    ZMQ.Socket creator2() {
        ZMQ.Socket socket = Mock(ZMQ.Socket)
        socket.send(_ as String, _ as Integer) >> { throw new ZMQException("Message", -1) }
        return socket
    }

    def underTest_
    def setup() {
        underTest_ = new ZeroMQPublishHandler("url", "test")
        underTest_.creator_ = this::creator
        underTest_.setFormatter(new DefaultJsonFormatter())
    }

    def "Test Publish"() {
        LogRecord record = new LogRecord(Level.INFO, "some message")
        underTest_.publish(record)
        expect: true
    }

    def "Test Publish Negative"() {
        given:
            underTest_.creator_ = this::creator2
            LogRecord record = new LogRecord(Level.INFO, "some message")
        when:
            underTest_.publish(record)
        then:
            thrown(RuntimeException)
    }

    def "Test createSocket"() {
        underTest_.flush()
        underTest_.close()
        expect: underTest_.createSocket() != null
    }
}
