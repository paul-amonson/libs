// Copyright 2018 Paul Amonson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.amonson.logger;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * General logger interface for implementations.
 */
public abstract class Logger {
    private static final int STACK_INDEX = 2;
    /**
     * Enum for logger levels.
     */
    public enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR,
        CRITICAL
    }

    /**
     * Log a debug level message.
     *
     * @param msg The message or if args are not null/empty the format string.
     * @param args The arguments for the format string.
     */
    public void debug(String msg, Object... args) {
        log(Level.DEBUG, Thread.currentThread().getStackTrace()[STACK_INDEX], msg, args);
    }

    /**
     * Log a informational level message.
     *
     * @param msg The message or if args are not null/empty the format string.
     * @param args The arguments for the format string.
     */
    public void info(String msg, Object... args) {
        log(Level.INFO, Thread.currentThread().getStackTrace()[STACK_INDEX], msg, args);
    }

    /**
     * Log a warning level message.
     *
     * @param msg The message or if args are not null/empty the format string.
     * @param args The arguments for the format string.
     */
    public void warn(String msg, Object... args) {
        log(Level.WARN, Thread.currentThread().getStackTrace()[STACK_INDEX], msg, args);
    }

    /**
     * Log a error level message.
     *
     * @param msg The message or if args are not null/empty the format string.
     * @param args The arguments for the format string.
     */
    public void error(String msg, Object... args) {
        log(Level.ERROR, Thread.currentThread().getStackTrace()[STACK_INDEX], msg, args);
    }

    /**
     * Log a critical level message.
     *
     * @param msg The message or if args are not null/empty the format string.
     * @param args The arguments for the format string.
     */
    public void critical(String msg, Object... args) {
        log(Level.CRITICAL, Thread.currentThread().getStackTrace()[STACK_INDEX], msg, args);
    }

    /**
     * Log an exception with the full stack trace to the error log level.
     *
     * @param throwable The exception to log.
     */
    public void except(Throwable throwable) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[STACK_INDEX];
        List<String> exceptionLines = new ArrayList<>();
        dumpExceptionTrace(exceptionLines, throwable);
        log(Level.ERROR, element, "Logged Exception..." + exceptionSeparator_ + String.join(exceptionSeparator_, exceptionLines));
    }

    /**
     * Log an exception with the full stack trace and a message to the error log level.
     *
     * @param throwable The exception to log.
     * @param msg The message of format string if args are not null/empty.
     * @param args The arguments for the format string.
     */
    public void except(Throwable throwable, String msg, Object... args) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[STACK_INDEX];
        List<String> exceptionLines = new ArrayList<>();
        dumpExceptionTrace(exceptionLines, throwable);
        log(Level.ERROR, element, msg + exceptionSeparator_ + String.join(exceptionSeparator_, exceptionLines), args);
    }

    /**
     * Sets the date format string (see SimpleDateFormat for string descriptions of this string's format)
     *
     * @param format The date timestamp format to use in the log line.
     */
    public void setDateFormatString(String format) { dateFormat_ = format; }

    /**
     * Gets the date format string currently used for the timestamp.
     *
     * @return The current format string.
     */
    public String getDateFormatString() { return dateFormat_; }

    /**
     * Sets the log line format string.
     *
     * @param format The format string that describes the format of the logged line.
     *
     * %D - Timestamp
     * %L - Log level
     * %S - Class/file/line information
     * %M - User message or exception information.
     */
    public void setLogFormatString(String format) { logLineFormat_ = format; }

    /**
     * Gets the log line format string. See setLogFormatString for details.
     *
     * @return The format string for the log line.
     */
    public String getLogFormatString() { return logLineFormat_; }

    /**
     * Sets the delimiter used when dumping an exception stack trace (defaults to a newline).
     *
     * @param delimiter Usually a single character delimiter used when dumping an exception stack.
     */
    public void setExceptionSeperator(String delimiter) { exceptionSeparator_ = delimiter; }

    /**
     * Gets the delimiter used when dumping an exception stack trace (defaults to a newline).
     *
     * @return Usually a single character delimiter used when dumping an exception stack.
     */
    public String getExceptionSeperator() { return exceptionSeparator_; }

    /**
     * Sets the new threshold for log filtering. The base class does not filter, should be used by derived
     * implementations.
     *
     * @param newLevel The log level used for filtering log messages.
     */
    public void setLevel(Level newLevel) { currentLevel_ = newLevel; }

    /**
     * Gets the new threshold for log filtering. The base class does not filter, should be used by derived
     * implementations.
     *
     * @return the current logging filter level.
     */
    public Level getLevel() { return currentLevel_; }

    /**
     * Called to initialize the logger with arguments passed to the factory.
     *
     * @param config The config for the specified implementation.
     */
    public void initialize(Properties config) {
        String levelKey = "com.amonson.logger.Logger.level";
        String level = "INFO";
        try {
            Properties defaults = new Properties();
            defaults.load(getClass().getResourceAsStream(File.separator + "default.properties"));
            if(config != null) {
                for (String key : defaults.stringPropertyNames())
                    config.setProperty(key, defaults.getProperty(key));
            } else
                config = defaults;
        } catch(IOException e) { /* Ignore */ }
        if(config != null)
            level = config.getProperty(levelKey, System.getProperty(levelKey, level)).toUpperCase();
        else
            level = System.getProperty(levelKey, level).toUpperCase();
        level = level.toUpperCase();
        currentLevel_ = Enum.valueOf(Level.class, level);
    }

    /**
     * Required to override in derived class implementation.
     *
     * @param lvl The log level of the log line to be used for filtering.
     * @param logLine The full log line to log.
     */
    protected abstract void outputFinalString(Level lvl, String logLine);

    private void log(Level lvl, StackTraceElement callinglocation, String msg, Object... args) {
        String fullMsg = String.format(msg, args);
        outputFinalString(lvl, buildLogLine(callinglocation, lvl, fullMsg));
    }

    private String buildLogLine(StackTraceElement trace, Level lvl, String msg) {
        String result = "%D: %L: (%S): %M";
        if(logLineFormat_ != null && !logLineFormat_.trim().equals(""))
            result = logLineFormat_;
        result = result.replace("%D", getTimeStamp());
        result = result.replace("%L", String.format("%-8s", lvl));
        result = result.replace("%S", String.format("%s:%d", trace.getFileName(), trace.getLineNumber()));
        result = result.replace("%M", msg);
        return result;
    }

    private String getTimeStamp() {
        if(dateFormat_ != null && !dateFormat_.trim().equals("")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat_).withZone(ZoneId.of("UTC"));
            return formatter.format(Instant.now());
        } else
            return Instant.now().toString();
    }

    private void dumpExceptionTrace(List<String> exceptionLines, Throwable throwable) {
        dumpExceptionTrace(exceptionLines, throwable, false);
    }

    private void dumpExceptionTrace(List<String> exceptionLines, Throwable throwable, boolean isCausedBy) {
        if(isCausedBy)
            exceptionLines.add("  Caused By: " + throwable.getClass().getCanonicalName() + ": " + throwable.getMessage());
        else
            exceptionLines.add("  Exception: " + throwable.getClass().getCanonicalName() + ": " + throwable.getMessage());

        for(StackTraceElement e: throwable.getStackTrace())
            exceptionLines.add(String.format("      at %s.%s(%s:%d)", e.getClassName(), e.getMethodName(),
                    e.getFileName(), e.getLineNumber()));
        if(throwable.getCause() != null)
            dumpExceptionTrace(exceptionLines, throwable.getCause(), true);
    }

    private String dateFormat_ = "yyyy-MM-dd'T'kk:mm:ss.SSS'Z'";
    private String logLineFormat_ = "%D: %L: (%S): %M";
    private String exceptionSeparator_ = "\n";
    private Level currentLevel_ = Level.INFO;
}
