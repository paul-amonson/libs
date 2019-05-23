// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

/**
 * Thrown by the PropStoreFactory.getStore or PropStoreFactory.registerNewStore.
 */
@SuppressWarnings("serial")
public class PropStoreFactoryException extends Exception {
    PropStoreFactoryException(String msg, Throwable e) { super(msg, e); }
    PropStoreFactoryException(String msg) { super(msg); }
}
