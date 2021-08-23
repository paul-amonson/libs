// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

/**
 * Node states reported by the NodeStateChangeHandler callback.
 */
public enum RemoteNodeState {
    /**
     * The remote node is now missing.
     */
    MISSING,
    /**
     * The remote node is now active.
     */
    ACTIVE
}
