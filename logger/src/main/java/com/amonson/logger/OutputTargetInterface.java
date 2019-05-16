// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

/**
 * Class defining the final output string target.
 */
@FunctionalInterface
public interface OutputTargetInterface {
    /**
     * Method definition for an output target of a logger line.
     *
     * @param lvl The Logger.Level of the line being output.
     * @param logLine The actual predefined formatted line to output.
     * @param filter If the Logger filter is to be used this is the current filter level.
     */
    void outputFinalString(Level lvl, String logLine, Level filter);
}
