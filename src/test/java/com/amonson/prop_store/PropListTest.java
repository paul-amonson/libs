// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PropListTest {
    @Test
    public void putAndGetPrimitivesTests() throws Exception {
        PropList list = new PropList();
        list.add(null);
        list.add("String");
        list.add(true);
        list.add((short)254);
        list.add(254);
        list.add(254L);
        list.add(new BigInteger("254"));
        list.add((float)1.0);
        list.add(1.0);
        list.add(new BigDecimal((double)2.0));
        list.add(new PropMap());
        list.add(new PropList());
        list.add(12, null);
        list.add(12, "String");
        list.add(12, true);
        list.add(12, (short)254);
        list.add(12, 254);
        list.add(12, 254L);
        list.add(12, new BigInteger("254"));
        list.add(12, (float)1.0);
        list.add(12, 1.0);
        list.add(12, new BigDecimal((double)2.0));
        list.add(12, new PropMap());
        list.add(12, new PropList());
        ArrayList<String> collection = new ArrayList<>();
        collection.add("One");
        collection.add("Two");
        collection.add("Three");
        PropList array2 = new PropList(collection);
        list.addAll(12, collection);
        list.set(20, new PropMap());
        Assertions.assertEquals(27, list.size());
        Assertions.assertTrue(list.isNull(0));
        Assertions.assertEquals("String", list.getString(1));
        Assertions.assertTrue(list.getBoolean(2));
        list.getMap(10);
        list.getArray(11);
        Assertions.assertEquals(1, (short)list.getShort(7));
        Assertions.assertEquals(1, (int)list.getInteger(7));
        Assertions.assertEquals(1, (long)list.getLong(7));
        Assertions.assertEquals(1.0, (float)list.getFloat(7), 0.00001);
        Assertions.assertEquals(1.0, (double)list.getDouble(7), 0.00001);
        Assertions.assertEquals(1.0, list.getBigDecimal(8).doubleValue(), 0.00001);
        Assertions.assertEquals(1L, list.getBigInteger(8).longValue());
        Assertions.assertNull(list.getShort(0));
        Assertions.assertNull(list.getInteger(0));
        Assertions.assertNull(list.getLong(0));
        Assertions.assertNull(list.getFloat(0));
        Assertions.assertNull(list.getDouble(0));
        Assertions.assertNull(list.getBigDecimal(0));
        Assertions.assertNull(list.getBigInteger(0));
        list.addAll(collection);
    }
}
