// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger;

import java.util.logging.LogRecord;

/**
 * Handler definition called when a new LogRecord is received on the ZeroMQLogSubscriber.
 */
@FunctionalInterface
public interface ReceivedLogMessageHandler {
    /**
     * Method signature of callback with data from ZeroMQPublishHandler.
     *
     * @param topic The topic this message was received on.
     * @param record The received LogRecord of the logger.
     * @param hostname The originating hostname of the system of log message source.
     * @param remoteProcessId The process ID (PID) of the process on the originating system.
     */
    void received(String topic, LogRecord record, String hostname, int remoteProcessId);
}
