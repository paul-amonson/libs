// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.crypto;

import com.amonson.prop_store.*;
import org.junit.jupiter.api.*;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class KeyDataTest {
    @BeforeEach
    public void setup() {
        KeyData.RNG_ALGORITHM = "NativePRNGNonBlocking";
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
