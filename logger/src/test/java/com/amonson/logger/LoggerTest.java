package com.amonson.logger;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class LoggerTest {
    void outputFinalString(Level lvl, String logLine, Level filter) {
        output_.append(logLine);output_.append("\n");
    }

    void earlyMethod(Level level, StackTraceElement stackTraceElement, String s, Object... objects) {
    }

    @Before
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
        logger_.setDefaultLogMethod();
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
