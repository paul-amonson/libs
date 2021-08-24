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
     * @param rawJson The raw JSON received over the network.
     */
    void received(String rawJson);
}
