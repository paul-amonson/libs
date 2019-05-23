// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import org.junit.Test;

public class PropStoreExceptionTest {
    @Test
    public void tests() {
        new PropStoreException();
        new PropStoreException("");
        new PropStoreException(new Exception());
        new PropStoreException("", new Exception());
    }
}
