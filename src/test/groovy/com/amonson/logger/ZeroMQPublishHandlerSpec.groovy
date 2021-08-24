// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger

import com.amonson.prop_store.PropStoreFactoryException
import org.zeromq.SocketType
import org.zeromq.ZMQException
import org.zeromq.ZMQ
import spock.lang.Specification

import java.util.logging.LogRecord
import java.util.logging.Level

class ZeroMQPublishHandlerSpec extends Specification {
    ZMQ.Socket socket_
    def underTest_
    def setup() {
        socket_ = Mock(ZMQ.Socket)
        underTest_ = new ZeroMQPublishHandler("url")
        underTest_.ctx_ = Mock(ZMQ.Context)
        underTest_.ctx_.socket(_ as SocketType) >> socket_
        underTest_.setFormatter(new DefaultJsonFormatter("hostname"))
    }

    def "Test Publish"() {
        LogRecord record = new LogRecord(Level.INFO, "some message")
        underTest_.publish(record)
        underTest_.publish(record)
        underTest_.flush()
        underTest_.close()
        expect: true
    }

    def "Test Publish Negative"() {
        given:
            socket_.send(_ as String, _ as Integer) >> { throw new ZMQException(1); }
            LogRecord record = new LogRecord(Level.INFO, "some message")
        when:
            underTest_.publish(record)
        then:
            thrown(RuntimeException)
    }
}
