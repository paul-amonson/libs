// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

/**
 * Default description for class DataAccessLayerException
 */
@SuppressWarnings("serial")
public class DataAccessLayerException extends Exception {

    /**
     * Create the status based exception.
     * @param status The enum reason for the failure.
     */
    DataAccessLayerException(DataAccessLayerStatus status) { super(String.format("Failed with status: %s", status)); }

    /**
     * Create the status based exception.
     * @param status The enum reason for the failure.
     * @param message The caller message for the failure.
     */
    DataAccessLayerException(DataAccessLayerStatus status, String message) { super(message); }

    /**
     * Create the status based exception.
     * @param status The enum reason for the failure.
     * @param cause The cause (underlying exception) of the failure.
     */
    DataAccessLayerException(DataAccessLayerStatus status, Throwable cause) { super(cause); }

    /**
     * Create the status based exception.
     * @param status The enum reason for the failure.
     * @param message The caller message for the failure.
     * @param cause The cause (underlying exception) of the failure.
     */
    DataAccessLayerException(DataAccessLayerStatus status, String message, Throwable cause) { super(message, cause); }
}
