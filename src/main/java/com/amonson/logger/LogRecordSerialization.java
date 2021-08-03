// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import com.amonson.prop_store.PropList;
import com.amonson.prop_store.PropMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * Static Class with 2 package private methods to serialize and deserialize the java.util.logging.LogRecord class. This is
 * intended to be used to send the log data over a network or unix socket to a log receiver outside the current
 * process.
 */
public final class LogRecordSerialization {
    private LogRecordSerialization() {}

    /**
     * Serialize a LogRecord to a PropMap type. This does not include ResourceBundle, ResourceBundleName or Parameters.
     *
     * @param record The LogRecord to serialize.
     * @return The PropMap containing the contents of the LogRecord minus the above listed exceptions.
     */
    public static PropMap serializeLogRecord(LogRecord record) {
        PropMap map = new PropMap();
        map.put("timestamp", (record.getInstant().getEpochSecond() * 1_000_000_000L) +
                ((long)record.getInstant().getNano()));
        map.put("name", record.getLoggerName());
        map.put("severity", record.getLevel().toString());
        map.put("thread", record.getThreadID());
        map.put("sequence", record.getSequenceNumber());
        map.put("message", record.getMessage());
        map.put("class", record.getSourceClassName());
        map.put("method", record.getSourceMethodName());
        map.put("exception", serializeThrowable(record.getThrown()));
        return map;
    }

    /**
     * Deserialize a PropMap object that contains data from the serializeLogRecord method.
     *
     * @param map The data to convert back to a LogRecord.
     * @return The converted LogRecord.
     */
    public static LogRecord deserializeLogRecord(PropMap map) {
        LogRecord record = new LogRecord(Level.parse(map.getString("severity")), map.getString("message"));
        long ts = map.getLong("timestamp");
        record.setInstant(Instant.ofEpochSecond(ts / 1_000_000_000L, (int)(ts % 1_000_000_000L)));
        record.setLoggerName(map.getString("name"));
        record.setSequenceNumber(map.getLong("sequence"));
        record.setThreadID(map.getInteger("thread"));
        record.setSourceClassName(map.getString("class"));
        record.setSourceMethodName(map.getString("method"));
        if(map.getMap("exception") != null)
            record.setThrown(deserializeThrowable(map.getMap("exception")));
        return record;
    }

    private static PropMap serializeThrowable(Throwable exception) {
        PropMap map = new PropMap();
        if(exception == null)
            return null;
        map.put("class", exception.getClass().getCanonicalName());
        map.put("message", exception.getMessage());
        PropList st = new PropList();
        for(StackTraceElement e: exception.getStackTrace())
            st.add(serializeStackTraceElement(e));
        map.put("stack", st);
        if(exception.getCause() != null)
            map.put("cause", serializeThrowable(exception.getCause()));

        return map;
    }

    private static Throwable createThrowableDynamically(String className, String message)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        return createThrowableDynamically(className, message, null);
    }

    private static Throwable createThrowableDynamically(String className, String message, Throwable cause)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        Class<?> clazz = Class.forName(className);
        Constructor<?> ctor;
        if(cause == null) {
            ctor = clazz.getConstructor(String.class);
            return (Throwable)ctor.newInstance(message);
        } else {
            ctor = clazz.getConstructor(String.class, Throwable.class);
            return (Throwable)ctor.newInstance(message, cause);
        }
    }

    private static Throwable deserializeThrowable(PropMap map) {
        Throwable cause;
        try {
            if (map.getMap("cause") != null)
                cause = createThrowableDynamically(map.getString("class"), map.getString("message"),
                        deserializeThrowable(map.getMap("cause")));
            else
                cause = createThrowableDynamically(map.getString("class"), map.getString("message"));
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                ClassNotFoundException e) {
            // Fall back to a generic exception...
            if(map.getMap("cause") != null)
                cause = new Throwable(map.getString("message"), deserializeThrowable(map.getMap("cause")));
            else
                cause = new Throwable(map.getString("message"));
        }
        StackTraceElement[] st = new StackTraceElement[map.getArray("stack").size()];
        for(int i = 0; i < st.length; i++)
            st[i] = deserializeStackTraceElement(map.getArray("stack").getMap(i));
        cause.setStackTrace(st);
        return cause;
    }

    private static PropMap serializeStackTraceElement(StackTraceElement element) {
        PropMap map = new PropMap();
        map.put("loader", element.getClassLoaderName());
        map.put("module", element.getModuleName());
        map.put("version", element.getModuleVersion());
        map.put("class", element.getClassName());
        map.put("method", element.getMethodName());
        map.put("file", element.getFileName());
        map.put("line", element.getLineNumber());
        return map;
    }

    private static StackTraceElement deserializeStackTraceElement(PropMap map) {
        return new StackTraceElement(map.getString("loader"),
                map.getString("module"),
                map.getString("version"),
                map.getString("class"),
                map.getString("method"),
                map.getString("file"),
                map.getInteger("line"));
    }
}
