package com.amonson.node_monitoring

import spock.lang.Specification

import java.util.logging.Logger

class ClusterServiceMonitorSpec extends Specification implements ClusterServiceMonitor.RoleChange {
    boolean running_
    int port_
    def underTest_

    @Override
    void stateChanged(ClusterServiceMonitor.Role role) {
    }

    void setup() {
        port_ = 9999
        running_ = false
        underTest_ = new ClusterServiceMonitor("me", new String[] {"me", "other"}, this, Mock(Logger))
        underTest_.monitoring_ = Mock(NodeMonitoring)
        underTest_.monitoring_.isRunning() >> { return running_ }
        underTest_.monitoring_.startMonitoring() >> { running_ = true }
        underTest_.monitoring_.stopMonitoring() >> { running_ = false }
        underTest_.role_ = ClusterServiceMonitor.Role.Primary
        underTest_.monitoring_.setPort(_ as Integer) >> { Integer port -> this.port_ = port }
    }

    def "Test non-blocking"() {
        boolean before = underTest_.isRunning()
        underTest_.startMonitoring(false)
        underTest_.startMonitoring(false)
        boolean after = underTest_.isRunning()
        underTest_.stopMonitoring()
        underTest_.stopMonitoring()
        expect: !before
        and:    after
    }

    def "Test blocking"() {
        underTest_.startMonitoring(true)
        boolean after = underTest_.isRunning()
        underTest_.stopMonitoring()
        expect:    after
    }

    def "Test setPort"() {
        underTest_.setPort(10000)
        expect: this.port_ == 10000
    }

    def "Test getRole"() {
        expect: underTest_.getRole() == ClusterServiceMonitor.Role.Primary
    }

    def "Test callback"() {
        underTest_.callback("other", false)
        underTest_.callback("other", true)
        expect: true
    }

    def "Test announceCallback"() {
        underTest_.announceCallback(String.format("other:%d", underTest_.nodes_.get("me")))
        underTest_.announceCallback(String.format("other:%d", Integer.MAX_VALUE))
        expect: underTest_.getRole() == ClusterServiceMonitor.Role.Secondary
    }

    def "Test no callback"() {
        underTest_ = new ClusterServiceMonitor("me", new String[] {"me", "other"}, null, Mock(Logger))
        underTest_.monitoring_ = Mock(NodeMonitoring)
        underTest_.monitoring_.isRunning() >> { return running_ }
        underTest_.monitoring_.startMonitoring() >> { running_ = true }
        underTest_.monitoring_.stopMonitoring() >> { running_ = false }
        underTest_.role_ = ClusterServiceMonitor.Role.Primary
        underTest_.monitoring_.setPort(_ as Integer) >> { Integer port -> this.port_ = port }
        underTest_.announceCallback(String.format("other:%d", Integer.MAX_VALUE))
        ClusterServiceMonitor.Role before = underTest_.getRole()
        underTest_.announceCallback(String.format("other:%d", 0))
        expect: before == ClusterServiceMonitor.Role.Secondary
        and:    underTest_.getRole() == ClusterServiceMonitor.Role.Primary
    }
}
