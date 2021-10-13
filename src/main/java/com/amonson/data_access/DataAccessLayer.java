// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

import org.apache.logging.log4j.core.Logger;

import java.util.Properties;

/**
 * Interface for DataAccess, agnostic to implementation.
 */
public abstract class DataAccessLayer {
    protected DataAccessLayer(Properties configurationProperties, Logger logger) {
        log_ = logger;
        configurationProperties_ = configurationProperties;
    }

    /**
     * Perform a synchronous query of the DataAccessLayer implementation.
     *
     * @param name Procedure name for the query.
     * @param params Parameters for the query.
     * @return The DataAccessLayerResponse for the attempted query.
     */
    public abstract DataAccessLayerResponse query(String name, Object... params);

    /**
     * Perform a asynchronous query of the DataAccessLayer implementation.
     *
     * @param callback The callback for asynchronous completion of the query.
     * @param name Procedure name for the query.
     * @param params Parameters for the query.
     */
    public abstract void query(DataAccessLayerCallback callback, String name, Object... params);

    /**
     * Perform a synchronous query of the DataAccessLayer implementation expecting a single long response.
     *
     * @param name Procedure name for the query.
     * @param params Parameters for the query.
     * @return The DataAccessLayerResponse for the attempted query.
     * @throws DataAccessLayerException on any all underlying implementation call errors.
     */
    public abstract long queryForLong(String name, Object... params) throws DataAccessLayerException;

    /**
     * Connect the object to the data source(s).
     */
    public abstract void connect();

    /**
     * Populate any schemas, stored procedures, etc... use by the implementation.
     *
     * @return true on success, false on failure.
     */
    public abstract boolean initializeAfterConnect();

    /**
     * Disconnect the object from the data source(s).
     */
    public abstract void disconnect();

    /**
     * Does the object have a connection?
     *
     * @return true if at least one connection is present; false otherwise.
     */
    public abstract boolean isConnected();

    protected final Logger log_;
    protected final Properties configurationProperties_;
}
