package com.amonson.logger

import org.zeromq.SocketType
import org.zeromq.ZMQ
import spock.lang.Specification

class ZeroMQLogSubscriberSpec extends Specification {
    int messageCount_
    ZMQ.Socket socket_
    ZMQ.Context ctx_
    ZeroMQLogSubscriber underTest_
    def setup() {
        socket_ = Mock(ZMQ.Socket)
        socket_.recvStr(_ as Integer) >>> ["topic", message_,"topic",null,null]
        ctx_ = Mock(ZMQ.Context)
        ctx_.socket(_ as SocketType) >> socket_
        messageCount_ = 0
        underTest_ = new ZeroMQLogSubscriber("tcp://*:20000", this::callback)
        underTest_.ctx_ = ctx_
    }

    void callback(String topic, String json) {
        messageCount_++
    }

    def "Test Run"() {
        new Thread(() -> {
            Thread.sleep(10)
            underTest_.signalStopServer()
        }).start()
        underTest_.run()
        underTest_.signalStopServer()
        expect: messageCount_ > 0
        and:    !underTest_.isRunning()
    }

    def "Test Run with Topics"() {
        underTest_ = new ZeroMQLogSubscriber("tcp://*:20000", this::callback, "t1", "t2")
        underTest_.ctx_ = ctx_
        new Thread(() -> {
            Thread.sleep(10)
            underTest_.signalStopServer()
        }).start()
        underTest_.run()
        expect: messageCount_ > 0
    }

    def "Test Run Twice"() {
        new Thread(underTest_).start()
        while(!underTest_.isRunning()) { Thread.sleep(10) }
        when: underTest_.run()
        then: thrown(RuntimeException)
        and:  underTest_.signalStopServer()
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
