// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.volt_wrapper;

/**
 * Generic status flags for a query.
 */
public enum DataAccessLayerStatus {
    /**
     * Successful query.
     */
    SUCCESS,
    /**
     * Query failed due to IO Error.
     */
    IO_FAILURE,
    /**
     * Query failed due to server error or malformed request.
     */
    SERVER_ERROR,
    /**
     * Query failed due to no connection.
     */
    NO_CONNECTION
}
