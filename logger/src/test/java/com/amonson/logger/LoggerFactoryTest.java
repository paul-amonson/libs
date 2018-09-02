package com.amonson.logger;

import org.junit.Test;

import static org.junit.Assert.*;

public class LoggerFactoryTest {
    private static final class TestLogger extends Logger {
        public TestLogger() {}

        @Override
        protected void outputFinalString(Level lvl, String logLine) { }
    }

    @Test
    public void tests() {
        try {
            LoggerFactory.getLogger();
            fail();
        } catch(RuntimeException e) { /* PASS */ }
        assertTrue(LoggerFactory.addImplementation("test", TestLogger.class));
        assertFalse(LoggerFactory.addImplementation("test", TestLogger.class));
        Logger l1 = LoggerFactory.getNamedLogger("myTest", "test");
        assertNotNull(l1);
        Logger l2 = LoggerFactory.getNamedLogger("myTest", "test");
        assertEquals(l1, l2);
        assertNull(LoggerFactory.getNamedLogger("myTest", "unknown"));
        Logger l3 = LoggerFactory.getLogger();
        assertEquals(l1, l3);
    }
}
