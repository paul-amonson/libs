// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import java.util.Properties;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class LoggerTest {
    private void outputFinalString(Properties config, Level lvl, StackTraceElement location,
                                   BuildLogLineInterface buildLine, String message, Level filter) {
        String logLine = buildLine.buildLogLine(location, lvl, message);
        output_.append(logLine);output_.append("\n");
    }

    private boolean earlyMethod(Properties config, Level level, Level filter, StackTraceElement stackTraceElement,
                                String s, Object... objects) {
        return true;
    }

    private boolean earlyMethod2(Properties config, Level level, Level filter, StackTraceElement stackTraceElement,
                                 String s, Object... objects) {
        return false;
    }

    @BeforeEach
    public void setUp() {
        output_ = new StringBuilder();
        logger_ = new Logger();
        logger_.addOutputTarget(this::outputFinalString);
    }

    @Test
    public void levelTests() {
        assertEquals(Level.DEBUG, Level.valueOf("DEBUG"));
        assertEquals(Level.INFO, Level.valueOf("INFO"));
        assertEquals(Level.WARN, Level.valueOf("WARN"));
        assertEquals(Level.ERROR, Level.valueOf("ERROR"));
        assertEquals(Level.CRITICAL, Level.valueOf("CRITICAL"));
        assertEquals(5, Level.values().length);
    }

    @Test
    public void test() throws Throwable {
        logger_.setDateFormatString(logger_.getDateFormatString());
        logger_.setExceptionSeparator(logger_.getExceptionSeparator());
        logger_.setLogFormatString(logger_.getLogFormatString());
        logger_.setLevel(logger_.getLevel());
        logger_.debug("Debug");
        logger_.info("Informational");
        logger_.warn("Warning");
        logger_.error("Error");
        logger_.critical("Critical");
        logger_.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")), "Test Message");
        logger_.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")));
        logger_.setEarlyLogMethod(this::earlyMethod);
        logger_.info("Informational");
        logger_.setEarlyLogMethod(this::earlyMethod2);
        logger_.info("Informational");
        logger_.setEarlyLogMethod(null);
        logger_.clearOutputTargets();
        logger_.addOutputTarget(null);
        new Logger(new Properties());
    }

    @Test
    public void negativeTests() throws Throwable {
        logger_.setDateFormatString(null);
        logger_.setLogFormatString(null);
        logger_.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")), "Test Message");
        logger_.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")));
        logger_.setDateFormatString("");
        logger_.setLogFormatString("");
        logger_.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")), "Test Message");
        logger_.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")));
    }

    private StringBuilder output_;
    private Logger logger_;
}
