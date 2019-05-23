// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

/**
 * Interface for early capture of logging to customize the Logger.
 */
@FunctionalInterface
public interface EarlyInterceptLog {
    /**
     * Method to intercept logging process early to completely customize the logger.
     *
     * @param lvl The Level of this log message.
     * @param callingLocation Either null or the calling stack location of an exception.
     * @param msg The logged message format string.
     * @param args The arguments for the format string.
     */
    void log(Level lvl, StackTraceElement callingLocation, String msg, Object... args);
}
