// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

/**
 * Functional interface for callbacks registered for specific topics. Called on incoming messages for this node.
 */
@FunctionalInterface
public interface MessageHandler {
    void handleMessage(Message message);
}
