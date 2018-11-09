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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.*;

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

    @Before
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

    @Test(expected = IllegalArgumentException.class)
    public void badName() throws Exception {
        PropStoreFactory.getStore("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullName() throws Exception {
        PropStoreFactory.getStore(null);
    }

    @Test(expected = PropStoreFactoryException.class)
    public void negative() throws Exception {
        PropStoreFactory.getStore("test1");
    }

    @Test
    public void registerNewStore() throws Exception {
        assertTrue(PropStoreFactory.registerNewStore("testStore", TestStoreOK.class));
        assertFalse(PropStoreFactory.registerNewStore("testStore", TestStoreOK.class));
    }

    @Test(expected = PropStoreFactoryException.class)
    public void registerNewStoreNegative() throws Exception {
        PropStoreFactory.registerNewStore("testStore", TestStoreBAD.class);
        PropStoreFactory.supportedImplementations_.remove("testStore");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerNewStoreBadName() throws Exception {
        PropStoreFactory.registerNewStore("", TestStoreOK.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerNewStoreNullName() throws Exception {
        PropStoreFactory.registerNewStore(null, TestStoreOK.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerNewStoreBadClass() throws Exception {
        PropStoreFactory.registerNewStore("wontStore", null);
    }
}
