// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import java.util.logging.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple factory wrapper for the built-in java logger (java.util.logging).
 */
public final class NativeLoggerFactory {
    private NativeLoggerFactory() {} // Disable instance creation

    /**
     * Create a new logger, disabling the default parent, adding a default handler and a default formatter. This logger
     * is NOT added to any LogManager if you desire to retrieve this logger in other parts of the codebase add this to
     * a LogManager. The default handler used is the ConsoleHandler. The default formatter is the DefaultLineFormatter.
     *
     * @param name The name of the logger to create.
     * @return The new constructed and configured logger.
     */
    public static Logger getNamedConfiguredLogger(String name) {
        return getNamedConfiguredLogger(name, new ConsoleHandler(), new DefaultLineFormatter());
    }

    /**
     * Create a new logger, disabling the default parent, adding a default handler and a formatter. This logger is NOT
     * added to any LogManager if you desire to retrieve this logger in other parts of the codebase add this to a
     * LogManager. The default handler used is the ConsoleHandler.
     *
     * @param name The name of the logger to create.
     * @param formatter The String formatter for the handler.
     * @return The new constructed and configured logger.
     */
    public static Logger getNamedConfiguredLogger(String name, Formatter formatter) {
        return getNamedConfiguredLogger(name, new ConsoleHandler(), formatter);
    }

    /**
     * Create a new logger, disabling the default parent, adding a handler and a default formatter. This logger is NOT
     * added to any LogManager if you desire to retrieve this logger in other parts of the codebase add this to a
     * LogManager. The default formatter is the DefaultLineFormatter.
     *
     * @param name The name of the logger to create.
     * @param handler The Handler that "publishes" the logged message.
     * @return The new constructed and configured logger.
     */
    public static Logger getNamedConfiguredLogger(String name, Handler handler) {
        return getNamedConfiguredLogger(name, handler, new DefaultLineFormatter());
    }

    /**
     * Create a new logger, disabling the default parent, adding a handler and a formatter. This logger is NOT added
     * to any LogManager if you desire to retrieve this logger in other parts of the codebase add this to a LogManager.
     *
     * @param name The name of the logger to create.
     * @param handler The Handler that "publishes" the logged message.
     * @param formatter The String formatter for the handler.
     * @return The new constructed and configured logger.
     */
    public static Logger getNamedConfiguredLogger(String name, Handler handler, Formatter formatter) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        logger.getHandlers()[0].setLevel(Level.FINEST);
        logger.getHandlers()[0].setFormatter(formatter);
        logger.setLevel(Level.FINEST);
        return logger;
    }
}
