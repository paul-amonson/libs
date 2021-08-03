// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger

import com.amonson.prop_store.PropStore
import com.amonson.prop_store.PropStoreException
import com.amonson.prop_store.PropStoreFactory
import spock.lang.Specification

import java.util.logging.Handler
import java.util.logging.LogRecord

class NativeLoggerFactorySpec extends Specification {
    def "Test GetNamedConfiguredLogger name and handler"() {
        Handler handler = new TestHandler()
        boolean ok = true
        java.util.logging.Logger logger = NativeLoggerFactory.getNamedConfiguredLogger("test", handler)
        logger.finest("finest")
        ok = ok && handler.validJson
        logger.finer("finer")
        ok = ok && handler.validJson
        logger.fine("fine")
        ok = ok && handler.validJson
        logger.info("info")
        ok = ok && handler.validJson
        logger.warning("warning")
        ok = ok && handler.validJson
        logger.severe("severe")
        ok = ok && handler.validJson
        logger.entering(getClass().getCanonicalName(), "GetNamedConfiguredLogger")
        ok = ok && handler.validJson
        logger.exiting(getClass().getCanonicalName(), "GetNamedConfiguredLogger")
        ok = ok && handler.validJson
        logger.config("config")
        ok = ok && handler.validJson
        logger.throwing(getClass().getCanonicalName(), "GetNamedConfiguredLogger", new Exception("exception", new RuntimeException("runtime")))
        ok = ok && handler.validJson
        expect: ok
    }

    def "Test GetNamedConfiguredLogger name only"() {
        java.util.logging.Logger logger = NativeLoggerFactory.getNamedConfiguredLogger("test")
        logger.entering("", "GetNamedConfiguredLogger")
        logger.exiting(getClass().getCanonicalName(), "")
        expect: true
    }

    def "Test GetNamedConfiguredLogger name and formatter"() {
        java.util.logging.Logger logger = NativeLoggerFactory.getNamedConfiguredLogger("test", new DefaultLineFormatter())
        logger.severe("severe")
        expect: true
    }

    class TestHandler extends Handler {
        boolean validJson

        @Override
        void publish(LogRecord logRecord) {
            String json = getFormatter().format(logRecord)
            PropStore store = PropStoreFactory.getStore("json")
            try {
                store.fromStringToMap(json)
                validJson = true
            } catch(PropStoreException e) {
                validJson = false
            }
        }

        @Override void flush() { }
        @Override void close() throws SecurityException { }
    }
}
