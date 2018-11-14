// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

/**
 * Exception thrown by any implementations of the PropStore.
 */
@SuppressWarnings("serial")
public class PropStoreException extends Exception {
    public PropStoreException() { super(); }
    public PropStoreException(String msg) { super(msg); }
    public PropStoreException(Throwable e) { super(e); }
    public PropStoreException(String msg, Throwable e) { super(msg, e); }
}
