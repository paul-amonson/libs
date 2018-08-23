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

package com.amonson.crypto;

import com.amonson.prop_store.*;
import org.junit.Test;
import org.junit.Before;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.Assert.*;

public class KeyDataTest {
    @Before
    public void setup() {
        byte[] zero = new byte[16];
        byte[] ones = new byte[16];
        for(int i = 0; i < 16; i++) {
            zero[i] = 0;
            ones[i] = 1;
        }
        one = Base64.getEncoder().encodeToString(ones);
        sZero = Base64.getEncoder().encodeToString(zero);
        keyObj = new KeyData(sZero, sZero);
    }

    @Test
    public void ctor_1() {
        assertEquals(sZero, keyObj.IV());
        assertEquals(sZero, keyObj.key());
        assertEquals("IV='AAAAAAAAAAAAAAAAAAAAAA=='; Key='AAAAAAAAAAAAAAAAAAAAAA=='", keyObj.toString());
        KeyData data2 = new KeyData(keyObj);
        assertEquals("AAAAAAAAAAAAAAAAAAAAAA==", data2.IV());
        assertEquals("AAAAAAAAAAAAAAAAAAAAAA==", data2.key());
        assertTrue(keyObj.equals(data2));
        assertFalse(keyObj.equals("some_string"));
        PropMap map = data2.toPropMap();
        assertEquals("AAAAAAAAAAAAAAAAAAAAAA==", map.getString("A"));
        assertEquals("AAAAAAAAAAAAAAAAAAAAAA==", map.getString("B"));
        KeyData.fromPropMap(map);
        data2.key_ = one;
        assertFalse(keyObj.equals(data2));
        data2.iv_ = one;
        assertFalse(keyObj.equals(data2));
    }

    @Test
    public void ctor_2() {
        try {
            KeyData data = new KeyData();
            assertEquals(16, data.IVAsBytes().length);
            assertEquals(16, data.keyAsBytes().length);
        } catch(NoSuchAlgorithmException e) {
            fail("This test failed due to lack of strong key generation support in the JRE!");
        }
    }

    private KeyData keyObj;
    private String sZero;
    private String one;
}
