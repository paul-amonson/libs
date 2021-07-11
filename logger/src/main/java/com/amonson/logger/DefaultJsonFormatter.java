// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import com.amonson.prop_store.PropMap;
import com.amonson.prop_store.PropStore;
import com.amonson.prop_store.PropStoreFactory;
import com.amonson.prop_store.PropStoreFactoryException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple default Json formatter with a eye on performance. A general JSON serializer was not used. Instead, a
 * simple StringBuilder is used.
 */
public class DefaultJsonFormatter extends Formatter {
    /**
     * Default ctor that prefetches the hostname and PID for the process and stores them as fields.
     *
     * @throws RuntimeException if the JSON parser cannot be created or the hostname cannot be retrieved.
     */
    public DefaultJsonFormatter() {
        try {
            parser_ = PropStoreFactory.getStore("json");
            String program = "/usr/bin/hostname";
            if (System.getProperty("os.name").startsWith("Windows"))
                program = "/Windows/System32/HOSTNAME.EXE";
            ProcessBuilder pb = new ProcessBuilder(program);
            Process p = pb.start();
            if (!p.waitFor(5, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IOException("Failed to get hostname using: " + program);
            }
            hostName_ = new String(new BufferedInputStream(p.getInputStream()).readAllBytes(),
                    StandardCharsets.US_ASCII).trim();
        } catch(InterruptedException | IOException | PropStoreFactoryException e) {
            throw new RuntimeException("Failed to get the system hostname!", e);
        }
        pid_ = ProcessHandle.current().pid();
    }

    /**
     * Overridden format method used by the Handler to format the LogRecord as a String. In this case a JSON string.
     *
     * @param logRecord The LogRecord to format from the handler.
     * @return The formatted string.
     */
    @Override
    public String format(LogRecord logRecord) {
        PropMap map = LogRecordSerialization.serializeLogRecord(logRecord);
        map.put("hostname", hostName_);
        map.put("pid", pid_);
        return parser_.toString(map);
    }

    private final String hostName_;
    private final long pid_;
    private final PropStore parser_;
}
