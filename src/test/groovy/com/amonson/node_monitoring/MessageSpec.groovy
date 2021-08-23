// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring

import spock.lang.Specification
import spock.lang.Unroll

class MessageSpec extends Specification {
    Message underTest_
    def setup() {
        underTest_ = new Message("topic", "me", "n1,n2", "part1", "part2")
    }

    def "Test ctor"() {
        given:
            List<String> frames = Arrays.asList("part1", "part2")
            List<String> nodes = Arrays.asList("n1", "n2")
            Message msg1 = new Message("topic", "me", "n1,n2", frames)
            Message msg2 = new Message("topic", "me", nodes, frames)
            Message msg3 = new Message("topic", "me", nodes)
            Message msg4 = new Message("topic", "me", nodes, (Iterable<String>)null)
            Message msg5 = new Message("topic", "me", "n1,n2", (Iterable<String>)null)
        expect: msg1 != null
        and:    msg2 != null
        and:    msg3 != null
        and:    msg4 != null
        and:    msg5 != null
    }

    @Unroll("TOPIC=#TOPIC; SENDER=#SENDER; TARGETS=#TARGETS")
    def "Test ctor negative 1"() {
        when:
            new Message(TOPIC, SENDER, (String)TARGETS, "part1", "part2")
        then:
            thrown(IllegalArgumentException)
        where:
            TOPIC   | SENDER | TARGETS
            null    | "me"   | "n1,n2"
            "topic" | null   | "n1,n2"
            "topic" | "me"   | null
            "  "    | "me"   | "n1,n2"
            "topic" | "  "   | "n1,n2"
            "topic" | "me"   | "  "
    }

    @Unroll("TARGETS.length=#DUMMY")
    def "Test ctor negative 2"() {
        when:
            new Message("topic", "sender", TARGETS)
        then:
            thrown(IllegalArgumentException)
        where:
            TARGETS         | DUMMY
            new ArrayList() | 0
            null            | "null"
    }

    def "Test forEach"() {
        List<String> results = new ArrayList<>()
        underTest_.forEachTargetDo((String target, Object arg) -> {
            results.add(target)
        }, null)
        expect: results.size() == 2
    }

    def "Test getTargetsAsString"() {
        String s1 = underTest_.targetsAsString
        underTest_.replaceTargets("n1")
        expect: s1 == "n1,n2"
        and:    underTest_.targetsAsString == "n1"
    }

    def "Test other targets with collection"() {
        List<String> targets = Arrays.asList("n3", "n4")
        underTest_.addTargets(targets)
        String s1 = underTest_.targetsAsString
        underTest_.replaceTargets(targets)
        expect: s1 == "n1,n2,n3,n4"
        and:    underTest_.targetsAsString == "n3,n4"
    }

    def "Test other message parts"() {
        underTest_.addMessageParts("part3", "part4")
        underTest_.addMessageParts(Arrays.asList("part5", "part6"))
        expect: underTest_.getMessageParts().length == 6
    }
}
