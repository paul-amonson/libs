// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.crypto;

import com.amonson.prop_store.*;
import org.junit.jupiter.api.*;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class KeyDataTest {
    @BeforeEach
    public void setup() {
        byte[] zero = new byte[KeyData.BYTES];
        byte[] ones = new byte[KeyData.BYTES];
        byte[] iv = new byte[16];
        for(int i = 0; i < KeyData.BYTES; i++) {
            zero[i] = 0;
            iv[i % 16] = 0;
            ones[i] = 1;
        }
        one = Base64.getEncoder().encodeToString(ones);
        sZero = Base64.getEncoder().encodeToString(zero);
        sIv = Base64.getEncoder().encodeToString(iv);
        keyObj = new KeyData(sIv, sZero);
    }

    @Test
    public void ctor_1() {
        Assertions.assertEquals(sIv, keyObj.IV());
        Assertions.assertEquals(sZero, keyObj.key());
        Assertions.assertEquals(String.format("IV='AAAAAAAAAAAAAAAAAAAAAA=='; Key='%s'",goldenKeys.get(KeyData.BITS)), keyObj.toString());
        KeyData data2 = new KeyData(keyObj);
        Assertions.assertEquals("AAAAAAAAAAAAAAAAAAAAAA==", data2.IV());
        Assertions.assertEquals(goldenKeys.get(KeyData.BITS), data2.key());
        Assertions.assertEquals(keyObj, data2);
        Assertions.assertNotEquals("some_string", keyObj);
        PropMap map = data2.toPropMap();
        assertEquals("AAAAAAAAAAAAAAAAAAAAAA==", map.getString("A"));
        assertEquals(goldenKeys.get(KeyData.BITS), map.getString("B"));
        KeyData.fromPropMap(map);
        data2.key_ = one;
        Assertions.assertNotEquals(keyObj, data2);
        data2.iv_ = one;
        Assertions.assertNotEquals(keyObj, data2);
    }

    @Test
    public void ctor_2() {
        try {
            KeyData data = KeyData.newKeyData();
            Assertions.assertEquals(16, data.IVAsBytes().length);
            Assertions.assertEquals(KeyData.BYTES, data.keyAsBytes().length);
        } catch(NoSuchAlgorithmException e) {
            Assertions.fail("This test failed due to lack of strong key generation support in the JRE!");
        }
    }

    private KeyData keyObj;
    private String sZero;
    private String sIv;
    private String one;
    private Map<Integer,String> goldenKeys = new HashMap<>() {{
        put(128, "AAAAAAAAAAAAAAAAAAAAAA==");
        put(256, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
    }};
}
