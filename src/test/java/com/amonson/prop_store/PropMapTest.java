// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PropMapTest {
    @Test
    public void putAndGetPrimitivesTests() throws Exception {
        PropMap map = new PropMap();
        map.put("Null", null);
        map.put("String", "String");
        map.replace("String", "Test");
        map.replace("String", "Test", "Test String");
        map.put("Boolean", true);
        map.put("Short", (short)254);
        map.put("Integer", 254);
        map.put("Long", 254L);
        map.put("BigInt", new BigInteger("254"));
        map.put("Float", (float)1.0);
        map.put("Double", 1.0);
        map.put("BigDec", new BigDecimal((double)2.0));
        map.put("Map", new PropMap());
        map.put("Array", new PropList());
        Assertions.assertNull(map.getString("Null"));
        Assertions.assertEquals("Test String", map.getString("String"));
        Assertions.assertEquals("true", map.getString("Boolean"));
        Assertions.assertTrue(map.getBoolean("Boolean"));
        Assertions.assertNull(map.getBoolean("Boolean2"));
        Assertions.assertNull(map.getBoolean("Null"));
        Assertions.assertEquals("254", map.getString("Long"));
        Assertions.assertNull(map.getString("NotFound"));
        Assertions.assertTrue(map.isNull("Null"));
        Assertions.assertFalse(map.isNull("String"));
        Assertions.assertEquals((short)254, (short)map.getShort("Short"));
        Assertions.assertEquals(254, (int)map.getInteger("Integer"));
        Assertions.assertEquals(254L, (long)map.getLong("Long"));
        Assertions.assertEquals(new BigInteger("254"), map.getBigInteger("BigInt"));
        Assertions.assertEquals(1.0, (float)map.getFloat("Float"), 0.0001);
        Assertions.assertEquals(1.0, (double)map.getDouble("Double"), 0.0001);
        Assertions.assertEquals(new BigDecimal(2.0), map.getBigDecimal("BigDec"));
        Assertions.assertEquals(0, map.getMap("Map").size());
        Assertions.assertEquals(0, map.getArray("Array").size());
        Assertions.assertNull(map.getShort("Short2"));
        Assertions.assertNull(map.getInteger("Integer2"));
        Assertions.assertNull(map.getLong("Long2"));
        Assertions.assertNull(map.getBigInteger("BigInt2"));
        Assertions.assertNull(map.getFloat("Float2"));
        Assertions.assertNull(map.getDouble("Double2"));
        Assertions.assertNull(map.getBigDecimal("BigDec2"));
        Assertions.assertNull(map.getMap("Map2"));
        Assertions.assertNull(map.getArray("Array2"));
        Assertions.assertNull(map.getShort("Null"));
        Assertions.assertNull(map.getInteger("Null"));
        Assertions.assertNull(map.getLong("Null"));
        Assertions.assertNull(map.getBigInteger("Null"));
        Assertions.assertNull(map.getFloat("Null"));
        Assertions.assertNull(map.getDouble("Null"));
        Assertions.assertNull(map.getBigDecimal("Null"));
        Assertions.assertNull(map.getMap("Null"));
        Assertions.assertNull(map.getArray("Null"));

        HashMap<String, String> originalMap = new HashMap<>();
        originalMap.put("s1", "1");
        originalMap.put("s2", "2");
        originalMap.put("s3", "3");
        originalMap.put("s4", "4");
        originalMap.put("s5", "5");
        PropMap map2 = new PropMap(originalMap);
        try {
            map.put("Unsupported", new HashSet());
            Assertions.fail();
        } catch(ClassCastException e) { /* PASS */ }
        try {
            map.put("", "Empty");
            Assertions.fail();
        } catch(IllegalArgumentException e) { /* PASS */ }
        try {
            map.put(null, "Null");
            Assertions.fail();
        } catch(NullPointerException e) { /* PASS */ }
        try {
            map.merge("key", null, null);
            Assertions.fail();
        } catch(UnsupportedOperationException e) { /* PASS */ }
        try {
            map.replaceAll(null);
            Assertions.fail();
        } catch(UnsupportedOperationException e) { /* PASS */ }
        PropMap map3 = new PropMap();
        map3.putAll(new HashMap<String,String>() {{ put("a", "val"); put("b", "val"); }});
        Assertions.assertEquals("val", map3.get("a"));
        Assertions.assertEquals("val", map3.get("b"));
    }
}
