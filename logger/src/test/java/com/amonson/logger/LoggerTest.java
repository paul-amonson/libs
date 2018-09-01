package com.amonson.logger;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoggerTest extends Logger {
    @Override
    protected void outputFinalString(Level lvl, String logLine) {
        output_.append(logLine);output_.append("\n");
    }

    @Before
    public void setUp() {
        output_ = new StringBuilder();
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
        this.setDateFormatString(this.getDateFormatString());
        this.setExceptionSeperator(this.getExceptionSeperator());
        this.setLogFormatString(this.getLogFormatString());
        this.setLevel(this.getLevel());
        this.debug("Debug");
        this.info("Informational");
        this.warn("Warning");
        this.error("Error");
        this.critical("Critical");
        this.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")), "Test Message");
        this.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")));
    }

    @Test
    public void negativeTests() throws Throwable {
        this.setDateFormatString(null);
        this.setLogFormatString(null);
        this.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")), "Test Message");
        this.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")));
        this.setDateFormatString("");
        this.setLogFormatString("");
        this.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")), "Test Message");
        this.except(new RuntimeException("Test Exception Message", new RuntimeException("Inner Test Exception Message")));
    }

    private StringBuilder output_;
}
