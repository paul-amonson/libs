// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

/**
 * Defined callback for the Role changes as the cluster changes.
 */
@FunctionalInterface
public interface PrimarySecondaryRoleChange {
    /**
     * Called when the Role for this node changes.
     *
     * @param role The new Role.
     */
    void stateChanged(PrimarySecondaryRole role);
}
