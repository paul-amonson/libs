package com.amonson.node_monitoring

import spock.lang.Specification

import java.util.logging.Logger

class ClusterServiceMonitorSpec extends Specification {
    def underTest_
    void setup() {
        underTest_ = new ClusterServiceMonitor("me", new String[] {"me", "other"}, null, Mock(Logger))
        underTest_.monitoring_ = Mock(NodeMonitoring)
        underTest_.monitoring_.isRunning() >> true
    }

    def "Test 1"() {
        underTest_.setPort(10000)
        underTest_.startMonitoring(false)
        expect: underTest_.isRunning()
    }
}
