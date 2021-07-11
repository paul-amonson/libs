// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger

import com.amonson.prop_store.PropMap
import spock.lang.Specification

import java.time.Instant
import java.util.logging.LogRecord
import java.util.logging.Level

class LogRecordSerializationSpec extends Specification {

    def "Test serialize and deserialize of Throwable"() {
        Throwable causeCause = new Exception("CauseCause")
        Throwable cause = new Exception("Cause", causeCause)
        Throwable exception = new IOException("Exception", cause)
        exception.setStackTrace(Thread.currentThread().getStackTrace())
        LogRecord record = new LogRecord(Level.INFO, "Message")
        record.setLoggerName("Logger")
        record.setSequenceNumber(5)
        record.setSourceClassName(this.getClass().getCanonicalName())
        record.setSourceMethodName("Method")
        record.setThrown(exception)
        record.setInstant(Instant.now())
        PropMap serialized = LogRecordSerialization.serializeLogRecord(record)
        LogRecord deserialized = LogRecordSerialization.deserializeLogRecord(serialized)
        expect: true
    }
}
