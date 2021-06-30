// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger

import org.zeromq.ZSocket
import spock.lang.Specification

import java.util.logging.LogRecord
import java.util.logging.Level

class ZeroMQPublishHandlerSpec extends Specification {
    ZSocket creator() {
        return Mock(ZSocket)
    }

    def underTest_
    def setup() {
        underTest_ = new ZeroMQPublishHandler("url", "test")
        underTest_.creator_ = this::creator
        underTest_.setFormatter(new DefaultJsonFormatter())
    }

    def "Test Publish"() {
        LogRecord record = new LogRecord(Level.INFO, "some message")
        underTest_.publish(record)
        expect: true
    }

    def "Test createSocket"() {
        underTest_.flush()
        underTest_.close()
        expect: underTest_.createSocket() != null
    }
}
