// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.util.HashMap;
import java.util.Properties;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class PropStoreFactoryTest {
    static class TestStoreOK extends PropStore {
        public TestStoreOK(Properties config) {
            super(config);
        }

        @Override
        public String toString(PropMap map) {
            return null;
        }

        @Override
        public String toString(PropList list) {
            return null;
        }

        @Override
        public PropMap fromStringToMap(String storeText) throws PropStoreException {
            return null;
        }

        @Override
        public PropList fromStringToList(String storeText) throws PropStoreException {
            return null;
        }
    }

    static class TestStoreBAD extends PropStore {
        TestStoreBAD(Properties config) {
            super(config);
        }

        @Override
        public String toString(PropMap map) {
            return null;
        }

        @Override
        public String toString(PropList list) {
            return null;
        }

        @Override
        public PropMap fromStringToMap(String storeText) throws PropStoreException {
            return null;
        }

        @Override
        public PropList fromStringToList(String storeText) throws PropStoreException {
            return null;
        }
    }

    @BeforeEach
    public void setUp() {
        PropStoreFactory.supportedImplementations_ = new HashMap<String, Class<? extends PropStore>>() {{
            put("json", JsonStore.class);
            put("yaml", YamlStore.class);
        }};
    }

    @Test
    public void allTests() throws Exception {
        assertNotNull(PropStoreFactory.getStore(" YamL "));
        assertNotNull(PropStoreFactory.getStore("JSON"));
        assertEquals(2, PropStoreFactory.getNames().size());
        assertTrue(PropStoreFactory.getNames().contains("json"));
        assertTrue(PropStoreFactory.getNames().contains("yaml"));
    }

    @Test
    public void badName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            PropStoreFactory.getStore("");
        });
    }

    @Test
    public void nullName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            PropStoreFactory.getStore(null);
        });
    }

    @Test
    public void negative() throws Exception {
        assertThrows(PropStoreFactoryException.class, () -> {
            PropStoreFactory.getStore("test1");
        });
    }

    @Test
    public void registerNewStore() throws Exception {
        assertTrue(PropStoreFactory.registerNewStore("testStore", TestStoreOK.class));
        assertFalse(PropStoreFactory.registerNewStore("testStore", TestStoreOK.class));
    }

    @Test
    public void registerNewStoreNegative() throws Exception {
        assertThrows(PropStoreFactoryException.class, () -> {
            PropStoreFactory.registerNewStore("testStore", TestStoreBAD.class);
            PropStoreFactory.supportedImplementations_.remove("testStore");
        });
    }

    @Test
    public void registerNewStoreBadName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            PropStoreFactory.registerNewStore("", TestStoreOK.class);
        });
    }

    @Test
    public void registerNewStoreNullName() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            PropStoreFactory.registerNewStore(null, TestStoreOK.class);
        });
    }

    @Test
    public void registerNewStoreBadClass() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            PropStoreFactory.registerNewStore("wontStore", null);
        });
    }
}
