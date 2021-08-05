// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring

import org.zeromq.SocketType
import org.zeromq.ZMQ
import spock.lang.Specification

import java.util.logging.Logger

class NodeMonitoringSpec extends Specification {
    def subSocket = Mock(ZMQ.Socket)
    def pubSocket = Mock(ZMQ.Socket)

    def myHostname_
    def underTest_
    def message_
    def state_
    void setupSpec() {
        NodeMonitoring.HEARTBEAT_MILLISECONDS = 30L
    }
    void setup() {
        myHostname_ = "node1"
        underTest_ = new NodeMonitoring(myHostname_, new String[] {myHostname_, "node2", "node3"}, this::eventCallback, Mock(Logger))
        underTest_.creator_ = this::createSocket
        subSocket.recvStr(_ as Integer) >>> ["alive", "node2", "alive", "node3"]
        underTest_.setPort(10000)
    }

    ZMQ.Socket createSocket(ZMQ.Context ctx, SocketType type) {
        if(type == SocketType.SUB)
            return subSocket
        else
            return pubSocket
    }

    void eventCallback(String message, boolean state) {
        message_ = message
        state_ = state
    }

    void handler(String message) {
    }

    def "Test startMonitoring"() {
        underTest_.startMonitoring()
        Thread.sleep(85L)
        def running = underTest_.isRunning()
        underTest_.stopMonitoring()
        expect: message_ == "node3"
        and:    running == true
    }

    def "Test startMonitoring 2"() {
        underTest_.waitForMonitoring()
        underTest_.startMonitoring()
        Thread.sleep(85L)
        def running = underTest_.isRunning()
        underTest_.stopMonitoring()
        underTest_.waitForMonitoring()
        expect: message_ == "node3"
        and:    running == true
    }

    def "Test add and remove handlers"() {
        underTest_.addHandler("myTopic", this::handler)
        underTest_.addHandler("myTopic", this::handler)
        underTest_.removeHandler("myTopic")
        underTest_.removeHandler("myTopic")
        expect: underTest_.handlers_.size() == 1
    }

    def "Test add negative"() {
        when: underTest_.addHandler(TOPIC, HANDLER)
        then: thrown(IllegalArgumentException)
        where:
        TOPIC   | HANDLER
        null    | this::handler
        "alive" | this::handler
        "topic" | null
    }

    def "Test remove negative"() {
        when: underTest_.removeHandler(TOPIC)
        then: thrown(IllegalArgumentException)
        where:
        TOPIC   | RESULT
        null    | null
        "alive" | null
    }

    def "Test sendMessage"() {
        underTest_.startMonitoring()
        Thread.sleep(15L)
        underTest_.sendMessage("myTopic", "node10")
        underTest_.sendMessage("myTopic", "node2")
        underTest_.stopMonitoring()
        expect: true
    }

    def "Test sendMessage negative"() {
        underTest_.startMonitoring()
        Thread.sleep(15L)
        underTest_.sendMessage("myTopic", "node10")
        underTest_.sendMessage("myTopic", "node2")
        underTest_.stopMonitoring()
        when: underTest_.sendMessage(TOPIC, MESSAGE)
        then: thrown(IllegalArgumentException)
        where:
        TOPIC   | MESSAGE
        null    | "node2"
        "alive" | "node2"
        "topic" | null
    }

    def "Test createSocket"() {
        expect: underTest_.createSocket(Mock(ZMQ.Context), SocketType.PUB) == null // Null because the context is mocked
    }
}
