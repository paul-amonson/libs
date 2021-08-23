// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring

import org.zeromq.SocketType
import org.zeromq.ZLoop
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Logger

class NodeMonitoringZeroMQSpec extends Specification {
    boolean stateCallbackCalled
    NodeMonitoringZeroMQ underTest_

    void setup() {
        def others = new ArrayList<String>()
        others.add("n1")
        others.add("n2")
        NodeMonitoringZeroMQ.instance_ = null
        underTest_ = NodeMonitoringFactory.createSingletonInstance("zeromq", "me", others,
                8192, this::stateCallback, Mock(Logger), null)
        underTest_.CTX = Mock(ZMQ.Context)
        underTest_.loop_ = Mock(ZLoop)
        underTest_.indirectCall_ = this::getMsg
        def clusterSocket = Mock(ZMQ.Socket)
        def inprocSocket = Mock(ZMQ.Socket)
        def repSocket = Mock(ZMQ.Socket)
        def pubSocket = Mock(ZMQ.Socket)
        underTest_.CTX.socket(_ as SocketType) >>> [clusterSocket, inprocSocket, repSocket, pubSocket, pubSocket]
        stateCallbackCalled = false
    }

    void stateCallback(String node, RemoteNodeState state, long lastSeen) {
        stateCallbackCalled = true
    }

    void messageCallback(String from, String to, ZMsg message) {
    }

    ZMsg getMsg(ZMQ.Socket socket, boolean wait) {
        ZMsg msg = ZMsg.newStringMsg("A", "n1,me", "hello")
        return msg
    }

    ZMsg getAlive(ZMQ.Socket socket, boolean wait) {
        ZMsg msg = ZMsg.newStringMsg("ALIVE", "n1")
        return msg
    }

    def "Test if you get bac=k the same object"() {
        def others = new ArrayList<String>()
        NodeMonitoringZeroMQ inst = NodeMonitoringFactory.createSingletonInstance("zeromq",
                "me", others, 8192, this::stateCallback, Mock(Logger), null)
        expect: inst == underTest_
    }

    def "Test start"() {
        underTest_.start(true)
        underTest_.zeroMQThreadId_ = 0L
        underTest_.start(true)
        underTest_.zeroMQThreadId_ = Long.MIN_VALUE
        underTest_.start(false)
        expect: true
    }

    def "Test stop"() {
        underTest_.zeroMQThreadId_ = 0L
        underTest_.stop()
        underTest_.zeroMQThreadId_ = Thread.currentThread().getId()
        underTest_.stop()
        underTest_.zeroMQThread_ = new Thread(() -> { Thread.sleep(5_000) })
        underTest_.zeroMQThread_.start()
        underTest_.stop()
        underTest_.zeroMQThreadId_ = Long.MIN_VALUE
        underTest_.stop()
        expect: true
    }

    def "Test handler management"() {
        def rv = underTest_.addOrReplaceMessageHandler("topic", this::messageCallback)
        underTest_.addMessageHandler("topic", this::messageCallback)
        expect: rv == null
        and:    underTest_.removeMessageHandler("topic") != null
    }

    @Unroll("with #TOPIC and #HANDLER")
    def "Test handler management with null #1"() {
        when: underTest_.addOrReplaceMessageHandler(TOPIC, HANDLER)
        then: thrown(AssertionError)
        where:
            TOPIC | HANDLER
            null  | this::messageCallback
            ""    | null
    }

    @Unroll("with #TOPIC and #HANDLER")
    def "Test handler management with null #2"() {
        when: underTest_.addMessageHandler(TOPIC, HANDLER)
        then: thrown(AssertionError)
        where:
        TOPIC | HANDLER
        null  | this::messageCallback
        ""    | null
    }

    @Unroll("with topic '#TOPIC' to '#TO' and thread #THREAD")
    def "Test sendMessage"() {
        String[] msg = new String[] {"one", "two", "three"}
        underTest_.zeroMQThreadId_ = THREAD
        if(THREAD != 0L)
            underTest_.publish_ = Mock(ZMQ.Socket)
        else
            underTest_.nonZeroMQThreads_.put(Thread.currentThread().getId(), Mock(ZMQ.Socket))
        Message message = new Message(TOPIC, "me", TO, msg)
        underTest_.sendMessage(message)
        expect: true
        where:
            TOPIC | TO   | THREAD
            "A"   | "n1" | 0L
            "A"   | "*"  | 0L
            "A"   | "n1" | Long.MIN_VALUE
            "A"   | "n1" | Thread.currentThread().getId()
    }

    def "Test ctor negative"() {
        List<String> others = Arrays.asList("n1","n2","me")
        when: new NodeMonitoringZeroMQ(ME, others, PORT, this::stateCallback, LOGGER, null)
        then: thrown(IllegalArgumentException)
        where:
        ME   | PORT  | LOGGER
        null | 10000 | Mock(Logger)
        "me" | 128   | Mock(Logger)
        "me" | 65536 | Mock(Logger)
        "me" | 10000 | null
    }

    def "Test startBlocking negative"() {
        underTest_.zeroMQThreadId_ = 0L
        underTest_.startBlocking()
        expect: true
    }

    def "Test timerTick"() {
        underTest_.publish_ = Mock(ZMQ.Socket)
        underTest_.timerTick(Mock(ZLoop), null, null)
        expect: true
    }

    def "Test localhostIncomingRequest"() {
        underTest_.publish_ = Mock(ZMQ.Socket)
        underTest_.localhostIncomingRequest(Mock(ZLoop), new ZMQ.PollItem(Mock(ZMQ.Socket), ZMQ.Poller.POLLIN), null)
        expect: true
    }

    def "Test fromExternalCluster"() {
        underTest_.publish_ = Mock(ZMQ.Socket)
        underTest_.fromExternalCluster(Mock(ZLoop), new ZMQ.PollItem(Mock(ZMQ.Socket), ZMQ.Poller.POLLIN), null)
        expect: true
    }

    def "Test fromExternalCluster 2"() {
        underTest_.publish_ = Mock(ZMQ.Socket)
        underTest_.push_ = Mock(ZMQ.Socket)
        underTest_.indirectCall_ = this::getAlive
        underTest_.fromExternalCluster(Mock(ZLoop), new ZMQ.PollItem(Mock(ZMQ.Socket), ZMQ.Poller.POLLIN), null)
        expect: true
    }

    def "Test fromOtherThreadInProcess"() {
        underTest_.publish_ = Mock(ZMQ.Socket)
        underTest_.fromOtherThreadInProcess(Mock(ZLoop), new ZMQ.PollItem(Mock(ZMQ.Socket), ZMQ.Poller.POLLIN), null)
        expect: true
    }

    def "Test callMessageHandlerOnThread"() {
        underTest_.callMessageHandlerOnThread(new Message("topic", "me", "me"))
        expect: true
    }

    def "Test configure"() {
        underTest_.configure(new Properties())
        expect: true
    }

    def "Test removeNodeStateChangeHandler"() {
        expect: !underTest_.removeNodeStateChangeHandler(this::stateCallback)
    }
}
