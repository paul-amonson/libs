// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.volt_wrapper;

/**
 * Callback for asynchronous queries.
 */
@FunctionalInterface
public interface DataAccessLayerCallback {
    void callback(DataAccessLayerResponse response);
}
