// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

/**
 * Enum for logger levels.
 */
public enum Level {
    /**
     * Debug level for attempting to debug problems in an application.
     */
    DEBUG,
    /**
     * General information like state changing, process progress, etc...
     */
    INFO,
    /**
     * Something is wrong but was recovered.
     */
    WARN,
    /**
     * Something is wrong and was not recovered.
     */
    ERROR,
    /**
     * Something is wrong and no recovery is possible and the application may close.
     */
    CRITICAL
}
