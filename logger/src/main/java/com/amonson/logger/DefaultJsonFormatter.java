// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple default Json formatter with a eye on performance. A general JSON serializer was not used. Instead, a
 * simple StringBuilder is used.
 */
public class DefaultJsonFormatter extends Formatter {
    /**
     * Default ctor that prefetches the hostname and PID for the process and stores them as fields.
     */
    public DefaultJsonFormatter() {
        try {
            hostName_ = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName_ = "UNKNOWN";
        }
        pid_ = ProcessHandle.current().pid();
    }

    /**
     * Overridden format methos used by the Handler to format the LogRecord as a String. In this case a JSON string.
     *
     * @param logRecord The LogRecord to format from the handler.
     * @return The formatted string.
     */
    @Override
    public String format(LogRecord logRecord) {
        StringBuilder builder = new StringBuilder().append('{');
        addString(builder, "timestamp", logRecord.getInstant().toString()).append(',');
        addString(builder, "hostname", hostName_).append(',');
        addString(builder, "name", logRecord.getLoggerName()).append(',');
        addString(builder, "severity", logRecord.getLevel().toString()).append(',');
        addString(builder, "thread", getThreadNameFromId(logRecord.getThreadID())).append(',');
        addNumber(builder, "process", pid_).append(',');
        addNumber(builder, "sequence", logRecord.getSequenceNumber()).append(',');
        String message = logRecord.getMessage();
        String clazz = logRecord.getSourceClassName();
        if(clazz != null && !clazz.isBlank() && entryExit_.contains(message))
            addString(builder, "class", clazz).append(',');
        String method = logRecord.getSourceMethodName();
        if(method != null && !method.isBlank() && entryExit_.contains(message))
            addString(builder, "method", logRecord.getSourceMethodName()).append(',');
        if(!message.equals("THROW"))
            addString(builder, "message", message);
        addThrown(builder, logRecord.getThrown());
        return builder.append('}').toString();
    }

    private StringBuilder addString(StringBuilder builder, String key, String value) {
        builder.append('"').append(key).append("\":").append('"').append(value).append('"');
        return builder;
    }

    private StringBuilder addNumber(StringBuilder builder, String key, Number value) {
        builder.append('"').append(key).append("\":").append(value.toString());
        return builder;
    }

    private void addThrown(StringBuilder builder, Throwable e) {
        if(e != null) {
            StringBuilder stack = new StringBuilder();
            stack.append("Exception: ").append(e.getClass().getCanonicalName()).append(": ").append(e.getMessage()).append('\n');
            addTrace(stack, e);
            addCausedBy(stack, e.getCause());
            addString(builder, "exception", stack.toString());
        }
    }

    private void addCausedBy(StringBuilder stack, Throwable cause) {
        if(cause != null) {
            stack.append("Caused By: ").append(cause.getClass().getCanonicalName()).append(": ").
                    append(cause.getMessage()).append('\n');
            addTrace(stack, cause);
            addCausedBy(stack, cause.getCause());
        }
    }

    private void addTrace(StringBuilder stack, Throwable e) {
        for(StackTraceElement element: e.getStackTrace())
            stack.append("  ").append(element.toString()).append('\n');
    }

    private String getThreadNameFromId(Integer id) {
        for(Thread thread: Thread.getAllStackTraces().keySet())
            if(thread.getId() == id)
                return thread.getName();
        return id.toString();
    }

    private       String hostName_;
    private final long pid_;

    @SuppressWarnings("serial")
    private static final List<String> entryExit_ = new ArrayList<>() {{
        add("ENTRY");
        add("RETURN");
    }};
}
