// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * General light weight logger for output implementations. The default logger goes to the console with log Levels
 * ERROR and CRITICAL going to stderr and the others going to stdout. The default logger uses the following properties:
 *
 * com.amonson.logger.Level - Sets the filter level of the Logger.
 */
public final class Logger {
    /**
     * Default logger to the console.
     */
    public Logger() {
        this(null);
    }

    /**
     * Default logger to the console that takes config properties. The following properties are used:
     * <ul>
     * <li>com.amonson.logger.filterLevel - filter level for the output target.</li>
     * </ul>
     * @param config The proerties for the logger.
     */
    public Logger(Properties config) {
        configuration_ = config;
        targets_.add(this::defaultOutputFinalString);
        setEarlyLogMethod(null);
        initialize();
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
    public void setExceptionSeparator(String delimiter) { exceptionSeparator_ = delimiter; }

    /**
     * Gets the delimiter used when dumping an exception stack trace (defaults to a newline).
     *
     * @return Usually a single character delimiter used when dumping an exception stack.
     */
    public String getExceptionSeparator() { return exceptionSeparator_; }

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
     * Add a output target to the Logger.
     *
     * @param target The output target.
     */
    public void addOutputTarget(OutputTargetInterface target) {
        if(target != null)
            targets_.add(target);
    }

    /**
     * Remove all output targets. Use when you don't want the default console output.
     */
    public void clearOutputTargets() { targets_.clear(); }

    /**
     * Set the new early log response method.
     *
     * @param method The replacement method to call when the public logging methods are called.
     */
    public void setEarlyLogMethod(EarlyInterceptLog method) { logMethod_ = method; }

    private void initialize() {
        String levelKey = "Level";
        String level = "INFO";
        if (configuration_ != null)
            level = configuration_.getProperty(levelKey,
                    System.getProperty(getClass().getCanonicalName() + "." + levelKey, level)).toUpperCase();
        else
            level = System.getProperty(levelKey, level).toUpperCase();
        currentLevel_ = Level.valueOf(level);
    }

    private void log(Level lvl, StackTraceElement callingLocation, String msg, Object... args) {
        boolean callTargets = true;
        String fullMsg = String.format(msg, args);
        if(logMethod_ != null)
            callTargets = logMethod_.log(configuration_, lvl, currentLevel_, callingLocation, msg, args);
        if(callTargets)
            for(OutputTargetInterface output: targets_)
                output.outputFinalString(configuration_, lvl, callingLocation, this::buildLogLine, fullMsg,
                        currentLevel_);
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

    private void defaultOutputFinalString(Properties unused, Level lvl, StackTraceElement location,
                                          BuildLogLineInterface buildLine, String fullMessage, Level filter) {
        String logLine = buildLine.buildLogLine(location, lvl, fullMessage);
        if (lvl.ordinal() >= filter.ordinal()) {
            if (lvl.ordinal() >= Level.ERROR.ordinal())
                System.err.println(logLine);
            else
                System.out.println(logLine);
        }
    }

    private String dateFormat_ = "yyyy-MM-dd'T'kk:mm:ss.SSS'Z'";
    private String logLineFormat_ = "%D: %L: (%S): %M";
    private String exceptionSeparator_ = "\n";
    private Level currentLevel_ = Level.INFO;
    private List<OutputTargetInterface> targets_ = new ArrayList<>();
    private EarlyInterceptLog logMethod_ = null;
    private Properties configuration_;

    private static final int STACK_INDEX = 2;
}
