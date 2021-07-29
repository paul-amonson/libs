// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.volt_wrapper;

import com.amonson.prop_store.PropList;

/**
 * Response for a query in a generic DataAccessLayer implementation.
 */
public class DataAccessLayerResponse {
    /**
     * Created in this package only.
     *
     * @param status The status of the query.
     * @param results The results of a successful query or null if unsuccessful.
     */
    DataAccessLayerResponse(DataAccessLayerStatus status, PropList results) {
        status_ = status;
        results_ = results;
    }

    /**
     * Get the status of the query.
     *
     * @return The status of the query.
     */
    public DataAccessLayerStatus getStatus() { return status_; }

    /**
     * Get the results of the query.
     *
     * @return The results of the query.
     */
    public PropList getResults() { return results_; }

    private final DataAccessLayerStatus status_;
    private final PropList results_;
}
