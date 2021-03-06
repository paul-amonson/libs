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
        assertNull(map.getString("Null"));
        assertEquals("Test String", map.getString("String"));
        assertEquals("true", map.getString("Boolean"));
        assertTrue(map.getBoolean("Boolean"));
        assertNull(map.getBoolean("Boolean2"));
        assertNull(map.getBoolean("Null"));
        assertEquals("254", map.getString("Long"));
        assertNull(map.getString("NotFound"));
        assertTrue(map.isNull("Null"));
        assertFalse(map.isNull("String"));
        assertEquals((short)254, (short)map.getShort("Short"));
        assertEquals(254, (int)map.getInteger("Integer"));
        assertEquals(254L, (long)map.getLong("Long"));
        assertEquals(new BigInteger("254"), map.getBigInteger("BigInt"));
        assertEquals(1.0, (float)map.getFloat("Float"), 0.0001);
        assertEquals(1.0, (double)map.getDouble("Double"), 0.0001);
        assertEquals(new BigDecimal(2.0), map.getBigDecimal("BigDec"));
        assertEquals(0, map.getMap("Map").size());
        assertEquals(0, map.getArray("Array").size());
        assertNull(map.getShort("Short2"));
        assertNull(map.getInteger("Integer2"));
        assertNull(map.getLong("Long2"));
        assertNull(map.getBigInteger("BigInt2"));
        assertNull(map.getFloat("Float2"));
        assertNull(map.getDouble("Double2"));
        assertNull(map.getBigDecimal("BigDec2"));
        assertNull(map.getMap("Map2"));
        assertNull(map.getArray("Array2"));
        assertNull(map.getShort("Null"));
        assertNull(map.getInteger("Null"));
        assertNull(map.getLong("Null"));
        assertNull(map.getBigInteger("Null"));
        assertNull(map.getFloat("Null"));
        assertNull(map.getDouble("Null"));
        assertNull(map.getBigDecimal("Null"));
        assertNull(map.getMap("Null"));
        assertNull(map.getArray("Null"));

        HashMap<String, String> originalMap = new HashMap<>();
        originalMap.put("s1", "1");
        originalMap.put("s2", "2");
        originalMap.put("s3", "3");
        originalMap.put("s4", "4");
        originalMap.put("s5", "5");
        PropMap map2 = new PropMap(originalMap);
        try {
            map.put("Unsupported", new HashSet());
            fail();
        } catch(ClassCastException e) { /* PASS */ }
        try {
            map.put("", "Empty");
            fail();
        } catch(IllegalArgumentException e) { /* PASS */ }
        try {
            map.put(null, "Null");
            fail();
        } catch(NullPointerException e) { /* PASS */ }
        try {
            map.merge("key", null, null);
            fail();
        } catch(UnsupportedOperationException e) { /* PASS */ }
        try {
            map.replaceAll(null);
            fail();
        } catch(UnsupportedOperationException e) { /* PASS */ }
        PropMap map3 = new PropMap();
        map3.putAll(new HashMap<String,String>() {{ put("a", "val"); put("b", "val"); }});
        assertEquals("val", map3.get("a"));
        assertEquals("val", map3.get("b"));
    }
}
