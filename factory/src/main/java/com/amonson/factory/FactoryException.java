// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.factory;

/**
 * Exception for FactoryGeneric Instances.
 */
@SuppressWarnings("serial")
public class FactoryException extends Exception {
    FactoryException(String message) { super(message); }
    FactoryException(String message, Throwable cause) { super(message, cause); }
}
