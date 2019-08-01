// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

/**
 * Callback for OutputTargetInterface callbacks to build the output line if desired.
 */
@FunctionalInterface
interface BuildLogLineInterface {
    /**
     * Method called to build a log line using the logger settings.
     *
     * @param trace The location of the logger function call.
     * @param lvl The logging level of this logged line.
     * @param msg The base message for the logged line.
     * @return The built logged line.
     */
    String buildLogLine(StackTraceElement trace, Level lvl, String msg);
}
