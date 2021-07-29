// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.volt_wrapper;

/**
 * Interface for DataAccess, agnostic to implementation.
 */
public interface DataAccessLayer {
    /**
     * Perform a synchronous query of the DataAccessLayer implementation.
     *
     * @param name Procedure name for the query.
     * @param params Parameters for the query.
     * @return The DataAccessLayerResponse for the attempted query.
     */
    DataAccessLayerResponse query(String name, Object... params);

    /**
     * Perform a asynchronous query of the DataAccessLayer implementation.
     *
     * @param callback The callback for asynchronous completion of the query.
     * @param name Procedure name for the query.
     * @param params Parameters for the query.
     * @return The DataAccessLayerResponse for the attempted query.
     */
    void query(DataAccessLayerCallback callback, String name, Object... params);

    /**
     * Perform a synchronous query of the DataAccessLayer implementation expecting a single long response.
     *
     * @param name Procedure name for the query.
     * @param params Parameters for the query.
     * @return The DataAccessLayerResponse for the attempted query.
     */
    long queryForLong(String name, Object... params);

    /**
     * Connect the object to the data source(s).
     */
    void connect();

    /**
     * Disconnect the object from the data source(s).
     */
    void disconnect();

    /**
     * Does the object have a connection?
     *
     * @return true if at least one connection is present; false otherwise.
     */
    boolean isConnected();
}
