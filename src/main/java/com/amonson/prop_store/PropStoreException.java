// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

/**
 * Exception thrown by any implementations of the PropStore.
 */
@SuppressWarnings("serial")
public class PropStoreException extends Exception {
    PropStoreException() { super(); }
    PropStoreException(String msg) { super(msg); }
    PropStoreException(Throwable e) { super(e); }
    PropStoreException(String msg, Throwable e) { super(msg, e); }
}
