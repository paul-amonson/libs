// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger

import com.amonson.prop_store.PropMap
import com.amonson.prop_store.PropStoreFactory
import spock.lang.Ignore
import spock.lang.Specification

import java.util.logging.LogRecord
import java.util.logging.Logger

// These tests are actually integration tests and DO use the network with ZeroMQ!!! Generally this is safe.
class ZeroMQTests extends Specification {
    def server_
    LogRecord lastMessage_
    String hostanme_
    int pid_
    static Logger logger_

    def setupSpec() {
        ZeroMQPublishHandler client = new ZeroMQPublishHandler("tcp://127.0.0.1:65099", "test")
        logger_ = NativeLoggerFactory.getNamedConfiguredLogger("testLogger", client, new DefaultJsonFormatter())
    }

    def setup() {
        server_ = new ZeroMQLogSubscriber("tcp://*:65099", this::callback)
        new Thread(server_).start()
        while(!server_.isRunning()) { Thread.sleep(10) }
    }

    def cleanup() {
        server_.signalStopServer()
    }

    static String logRecordToString(LogRecord record) {
        PropMap map = LogRecordSerialization.serializeLogRecord(record)
        return PropStoreFactory.getStore("json").toString(map)
    }

    void callback(String topic, LogRecord record, String hostname, int remoteProcessId) {
        println(String.format("It worked: topic=%s; hostname=%s; pid=%d; record=%s", topic, hostname, remoteProcessId,
                logRecordToString(record)))
        lastMessage_ = record
        hostanme_ = hostname
        pid_ = remoteProcessId
    }

    @Ignore
    def "Test Client and Server"() {
        logger_.info("Test Message")
        Thread.sleep(250)
        expect: pid_ == ProcessHandle.current().pid()
        and:    lastMessage_.getMessage() == "Test Message"
    }

    @Ignore
    def "Test Client and Server with Exception"() {
        def exception = new Exception("MyException")
        logger_.throwing("MyClass", "MyMethod", exception)
        Thread.sleep(250)
        expect: lastMessage_.getThrown().getMessage().equals(exception.getMessage())
    }
}
