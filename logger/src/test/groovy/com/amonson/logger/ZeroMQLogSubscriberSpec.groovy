package com.amonson.logger

import org.zeromq.ZMQ
import spock.lang.Specification

import java.util.logging.LogRecord

class ZeroMQLogSubscriberSpec extends Specification {
    def messageCount_

    ZMQ.Socket createMockSocket() {
        ZMQ.Socket socket = Mock(ZMQ.Socket)
        socket.recv(_ as Integer) >>> ["topic".bytes, message_.bytes]
        return socket
    }

    void callback(String topic, LogRecord record, String hostname, int remoteProcessId) {
        messageCount_++
    }

    ZeroMQLogSubscriber underTest_
    def setup() {
        messageCount_ = 0
        underTest_ = new ZeroMQLogSubscriber("tcp://*:20000", this::callback)
        underTest_.creator_ = this::createMockSocket
    }

    def "Test Run"() {
        new Thread(underTest_).start()
        while(!underTest_.isRunning()) { Thread.sleep(10) }
        Thread.sleep(10)
        underTest_.signalStopServer()
        while(underTest_.isRunning()) { Thread.sleep(10) }
        expect: messageCount_ > 0
    }

    def "Test Run with Topics"() {
        underTest_ = new ZeroMQLogSubscriber("tcp://*:20000", this::callback, "t1", "t2")
        underTest_.creator_ = this::createMockSocket
        new Thread(underTest_).start()
        while(!underTest_.isRunning()) { Thread.sleep(10) }
        Thread.sleep(10)
        underTest_.signalStopServer()
        while(underTest_.isRunning()) { Thread.sleep(10) }
        expect: messageCount_ > 0
    }

    def "Test Run Twice"() {
        new Thread(underTest_).start()
        while(!underTest_.isRunning()) { Thread.sleep(10) }
        when: underTest_.run()
        then: thrown(RuntimeException)
        and:  underTest_.signalStopServer()
    }

    def "Test CreateSocket"() {
        expect: underTest_.createSocket() != null
    }

    String message_ = """{
    "timestamp": 1625940510063691700,
    "name": "name",
    "severity": "SEVERE",
    "thread": 1,
    "sequence": 2,
    "message": "message",
    "class": "com.amonson.logger.SomeClass",
    "method": "someMethod",
    "exception": null,
    "hostname": "me",
    "pid": 42
}"""
}
