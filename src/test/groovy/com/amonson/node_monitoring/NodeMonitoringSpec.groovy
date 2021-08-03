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

    def myIP_
    def underTest_
    def message_
    def state_
    void setup() {
        Enumeration e = NetworkInterface.getNetworkInterfaces()
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement()
            Enumeration ee = n.getInetAddresses()
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement()
                if(i.getAddress().length == 4 && !i.isLoopbackAddress())
                    myIP_ = i.getHostAddress()
            }
        }
        underTest_ = new NodeMonitoring(new String[] {myIP_, "192.168.0.2", "192.168.0.3"}, this::eventCallback, Mock(Logger))
        underTest_.creator_ = this::createSocket
        subSocket.recvStr(_ as Integer) >>> ["alive", "192.168.0.2", "alive", "192.168.0.3"]
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
        Thread.sleep(2_700L)
        def running = underTest_.isRunning()
        underTest_.stopMonitoring()
        expect: message_ == "192.168.0.3"
        and:    running == true
    }

    def "Test startMonitoring 2"() {
        underTest_.waitForMonitoring()
        underTest_.startMonitoring()
        Thread.sleep(2_700L)
        def running = underTest_.isRunning()
        underTest_.stopMonitoring()
        underTest_.waitForMonitoring()
        expect: message_ == "192.168.0.3"
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
        Thread.sleep(500L)
        underTest_.sendMessage("myTopic", "192.168.0.10")
        underTest_.sendMessage("myTopic", "192.168.0.2")
        underTest_.stopMonitoring()
        expect: true
    }

    def "Test sendMessage negative"() {
        underTest_.startMonitoring()
        Thread.sleep(500L)
        underTest_.sendMessage("myTopic", "192.168.0.10")
        underTest_.sendMessage("myTopic", "192.168.0.2")
        underTest_.stopMonitoring()
        when: underTest_.sendMessage(TOPIC, MESSAGE)
        then: thrown(IllegalArgumentException)
        where:
        TOPIC   | MESSAGE
        null    | "192.168.0.2"
        "alive" | "192.168.0.2"
        "topic" | null
    }

    def "Test createSocket"() {
        expect: underTest_.createSocket(Mock(ZMQ.Context), SocketType.PUB) == null // Null because the context is mocked
    }
}
