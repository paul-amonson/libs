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
     * Get the results of the query. This mimics an array of results of rows. So usually it's an array of
     * 0 or 1 "tables" containing an array of rows where each row is an object with named key (column name) and value.
     * [ // Array of results (PropList)
     *    [ // rows of specific result (PropList)
     *       {"column_name": value, "column_name2": value, ...}, // PropMap
     *       {"column_name": value, "column_name2": value, ...}, // PropMap
     *       ...
     *    ],
     *    [ // rows of specific result (PropList)
     *       {"column_name": value, "column_name2": value, ...}, // PropMap
     *       {"column_name": value, "column_name2": value, ...}, // PropMap
     *       ...
     *    ]
     * ]
     * @return The results of the query.
     */
    public PropList getResults() { return results_; }

    private final DataAccessLayerStatus status_;
    private final PropList results_;
}
