package com.amonson.logger;

import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.util.Properties;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class FileRotateTargetTest {
    @Test
    public void test() {
        Properties properties = new Properties();
        properties.put("com.amonson.logger.FileRotateTarget.maxRotateSize", "5000");
        FileRotateTarget target = new FileRotateTarget(new File("/tmp/testfile.log"));
        Logger log = new Logger(properties);
        log.clearOutputTargets();
        log.addOutputTarget(target);
        for(int i = 1; i <= 100; i++)
            log.error("%03d: Some longer text for the log test error message output", i);
        log.debug("DEBUG LINE");
    }

    @Test
    public void negativeTest() {
        assertThrows(RuntimeException.class, () -> {
            new FileRotateTarget(null);
        });
    }
}
