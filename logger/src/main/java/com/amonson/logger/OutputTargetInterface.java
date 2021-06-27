// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import java.util.Properties;

/**
 * Class defining the final output string target.
 */
@FunctionalInterface
@Deprecated(forRemoval = true, since = "1.3.1")
public interface OutputTargetInterface {
    /**
     * Method definition for an output target of a logger line.
     *
     * @param config The configuration passed to the logger.
     * @param loggedLevel The Logger.Level of the line being output.
     * @param location The location of the code that logged the line.
     * @param buildLineMethod The callback that will format the full log line with the Logger settings.
     * @param fullMessage The actual predefined formatted line to output.
     * @param filter If the Logger filter is to be used this is the current filter level.
     */
    void outputFinalString(Properties config, Level loggedLevel, StackTraceElement location,
                           BuildLogLineInterface buildLineMethod, String fullMessage, Level filter);
}
