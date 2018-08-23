// Copyright 2018 Paul Amonson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.amonson.prop_store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropArrayTest {
    @Test
    public void putAndGetPrimitivesTests() throws Exception {
        PropList array = new PropList();
        array.add(null);
        array.add("String");
        array.add(true);
        array.add((short)254);
        array.add(254);
        array.add(254L);
        array.add(new BigInteger("254"));
        array.add((float)1.0);
        array.add(1.0);
        array.add(new BigDecimal((double)2.0));
        array.add(new PropMap());
        array.add(new PropList());
        array.add(12, null);
        array.add(12, "String");
        array.add(12, true);
        array.add(12, (short)254);
        array.add(12, 254);
        array.add(12, 254L);
        array.add(12, new BigInteger("254"));
        array.add(12, (float)1.0);
        array.add(12, 1.0);
        array.add(12, new BigDecimal((double)2.0));
        array.add(12, new PropMap());
        array.add(12, new PropList());
        ArrayList<String> list = new ArrayList<>();
        list.add("One");
        list.add("Two");
        list.add("Three");
        PropList array2 = new PropList(list);
        array.addAll(12, list);
        array.set(20, new PropMap());
        assertEquals(27, array.size());
        assertTrue(array.isNull(0));
        assertEquals("String", array.getString(1));
        assertTrue(array.getBoolean(2));
        array.getMap(10);
        array.getArray(11);
        assertEquals(1, (short)array.getShort(7));
        assertEquals(1, (int)array.getInteger(7));
        assertEquals(1, (long)array.getLong(7));
        assertEquals(1.0, (float)array.getFloat(7), 0.00001);
        assertEquals(1.0, (double)array.getDouble(7), 0.00001);
        assertEquals(1.0, array.getBigDecimal(8).doubleValue(), 0.00001);
        assertEquals(1L, array.getBigInteger(8).longValue());
        assertNull(array.getShort(0));
        assertNull(array.getInteger(0));
        assertNull(array.getLong(0));
        assertNull(array.getFloat(0));
        assertNull(array.getDouble(0));
        assertNull(array.getBigDecimal(0));
        assertNull(array.getBigInteger(0));
    }
}
