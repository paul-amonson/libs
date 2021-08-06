package com.amonson.node_monitoring

import spock.lang.Specification

import java.util.logging.Logger

class ClusterServiceMonitorSpec extends Specification implements ClusterServiceMonitor.RoleChange, ClusterServiceMonitor.MessageCallback {
    boolean running_
    int port_
    String receivedMessage_
    String topic_
    String from_
    Collection<String> to_
    def underTest_

    @Override
    void stateChanged(ClusterServiceMonitor.Role role) {
    }

    void setup() {
        topic_ = null
        receivedMessage_ = null
        from_ = null
        to_ = null
        port_ = 9999
        running_ = false
        underTest_ = new ClusterServiceMonitor("me", new String[] {"me", "other"}, Mock(Logger))
        underTest_.setRoleCallback(this)
        underTest_.monitoring_ = Mock(NodeMonitoring)
        underTest_.monitoring_.isRunning() >> { return running_ }
        underTest_.monitoring_.startMonitoring() >> { running_ = true }
        underTest_.monitoring_.stopMonitoring() >> { running_ = false }
        underTest_.role_ = ClusterServiceMonitor.Role.Primary
        underTest_.monitoring_.setPort(_ as Integer) >> { Integer port -> this.port_ = port }
        underTest_.monitoring_.sendMessage(_ as String, _ as String) >> {
            String topic, String msg ->
                topic_ = topic
                receivedMessage_ = msg
        }
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
        underTest_ = new ClusterServiceMonitor("me", new String[] {"me", "other"}, Mock(Logger))
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

    def "Test sendMessageAll"() {
        underTest_.sendMessageAll("message")
        expect: receivedMessage_ == "me|other|message"
        and: topic_ == "MESSAGE"
    }

    def "Test sendMessage for Primary"() {
        underTest_.sendMessage(ClusterServiceMonitor.Role.Primary, "message")
        expect: receivedMessage_ == null
        and: topic_ == null
    }

    def "Test sendMessage for Secondary"() {
        underTest_.sendMessage(ClusterServiceMonitor.Role.Secondary, "message")
        expect: receivedMessage_ == "me|other|message"
        and: topic_ == "MESSAGE"
    }

    def "Test sendMessage for Primary 2"() {
        underTest_.role_ = ClusterServiceMonitor.Role.Secondary
        underTest_.primary_ = "other"
        underTest_.sendMessage(ClusterServiceMonitor.Role.Primary, "message")
        expect: receivedMessage_ == "me|other|message"
        and: topic_ == "MESSAGE"
    }

    def "Test sendMessage for null Role"() {
        underTest_.sendMessage((ClusterServiceMonitor.Role)null, "message")
        expect: receivedMessage_ == null
        and: topic_ == null
    }

    def "Test sendMessage for null Collection"() {
        underTest_.sendMessage((Collection<String>)null, "message")
        expect: receivedMessage_ == null
        and: topic_ == null
    }

    def "Test sendMessage for Collection null message"() {
        underTest_.sendMessage(["other"], null)
        expect: receivedMessage_ == null
        and: topic_ == null
    }

    @Override
    void incomingMessage(String from, List<String> to, String message) {
        from_ = from
        to_ = to
        receivedMessage_ = message
    }
    def "Test messageCallback"() {
        underTest_.setMessageCallback(this)
        underTest_.messageCallback("me|other|message")
        expect: from_ == "me"
        and:    receivedMessage_ == "message"
        and:    to_.size() == 1
        and:    to_[0] == "other"
    }

    def "Test messageCallback 2"() {
        underTest_.setMessageCallback(this)
        underTest_.setMessageCallback(null)
        underTest_.messageCallback("me|other|message")
        expect: from_ == null
    }
}
