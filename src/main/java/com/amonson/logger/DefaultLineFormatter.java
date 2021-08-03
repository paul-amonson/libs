// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import java.util.logging.LogRecord;

/**
 * A simple line formatter where the DefaultJsonFormatter is the base. This class makes sure that a newline is present
 * as the last character of the message. The will behave correctly when using the ConsoleHandler and show one JSON
 * message per line on the console.
 */
public class DefaultLineFormatter extends DefaultJsonFormatter {
    /**
     * Call the parent (DefaultJsonFormatter) format method and appends a newline for text output.
     *
     * @param logRecord The LogRecord to format from the handler.
     * @return the modified JSON string.
     */
    @Override
    public String format(LogRecord logRecord) {
        return super.format(logRecord) + "\n";
    }
}
