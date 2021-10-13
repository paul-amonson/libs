// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring

import org.apache.logging.log4j.core.Logger
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class PrimarySecondaryServiceMonitorSpec extends Specification {
    NodeMonitoring monitor_
    PrimarySecondaryServiceMonitor underTest_
    String hostname_
    PrimarySecondaryRole newRole_
    int sendMessageCount_
    void setup() {
        sendMessageCount_ = 0
        hostname_ = "me"
        monitor_ = Mock(NodeMonitoring)
        monitor_.getLogger() >> Mock(Logger)
        monitor_.getMyHostname() >> hostname_
        monitor_.isRunning() >> true
        monitor_.sendMessage(_ as Message) >> { ++sendMessageCount_ }
        underTest_ = new PrimarySecondaryServiceMonitor(monitor_, this::roleChange)
        underTest_.rng_ = Mock(Random)
        underTest_.rng_.nextInt(_ as Integer) >>> [5, 0, 6, 4]
        underTest_.map_.put("me", 5)
    }

    void roleChange(PrimarySecondaryRole newRole) {
        newRole_ = newRole
    }

    def "Test evaluatePrimary where I am not primary"() {
        def msg = new Message("_ANNOUNCE_", "other", "me,other,other2", "1")
        underTest_.announceHandler(msg)
        msg = new Message("_ANNOUNCE_", "other2", "me,other,other2", "10")
        underTest_.announceHandler(msg)
        def t1 = underTest_.primary_
        underTest_.nodeStateChangeCallback("other2", RemoteNodeState.MISSING, Instant.now().toEpochMilli())
        expect: t1 == "other2"
        and:    underTest_.primary_ == hostname_
    }

    def "Test announceHandler with me"() {
        def msg = new Message("_ANNOUNCE_", "me", "me,other", "1")
        underTest_.announceHandler(msg)
        expect: true
    }

    def "Test announceHandler with collision"() {
        def msg = new Message("_ANNOUNCE_", "other", "me,other", "5")
        underTest_.announceHandler(msg)
        expect: true
    }

    def "Test nodeStateChangeCallback"() {
        def current = sendMessageCount_
        def spy = Spy(underTest_)
        underTest_.nodeStateChangeCallback("other", RemoteNodeState.ACTIVE, Instant.now().toEpochMilli())
        expect: (sendMessageCount_ - current) == 1
    }

    def "Test enableHandler"() {
        underTest_.enableHandler(false)
        expect: true
    }

    @Unroll("#MONITOR; #HANDLER; #RUNNING")
    def "Test ctor negative"() {
        given:
            MONITOR.getLogger() >> Mock(Logger)
            MONITOR.getMyHostname() >> hostname_
            MONITOR.isRunning() >> RUNNING
        when:
            new PrimarySecondaryServiceMonitor(MONITOR, HANDLER)
        then:
            thrown(EXCEPTION)
        where:
            MONITOR              | HANDLER          | RUNNING || EXCEPTION
            Mock(NodeMonitoring) | null             | true    || IllegalArgumentException
            null                 | this::roleChange | true    || IllegalArgumentException
            Mock(NodeMonitoring) | this::roleChange | false   || RuntimeException
    }
}
