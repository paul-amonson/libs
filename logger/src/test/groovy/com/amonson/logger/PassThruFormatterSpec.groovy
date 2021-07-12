package com.amonson.logger

import spock.lang.Specification

import java.util.logging.*

class PassThruFormatterSpec extends Specification {
    def "Test Format"() {
        given:
            Formatter formatter = new PassThruFormatter();
            String original = "My Special Message";
            LogRecord record = new LogRecord(Level.INFO, original)
            String raw = formatter.format(record)
        expect:
            raw == original + "\n"
    }
}
