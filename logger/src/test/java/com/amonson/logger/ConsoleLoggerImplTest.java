package com.amonson.logger;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConsoleLoggerImplTest {
    @Test
    public void tests() {
        Logger log = LoggerFactory.getNamedLogger("myTest", "console");
        assertNotNull(log);
        log.error("Testing...");
        log.info("Testing...");
        log.debug("Testing...");
    }
}
