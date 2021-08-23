// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

/**
 * Functional interface for callback that is called when a remote node changes state (MISSING or ACTIVE).
 */
@FunctionalInterface
public interface NodeStateChangeHandler {
    void nodeStateChanged(String node, RemoteNodeState state, long lastSeen);
}
