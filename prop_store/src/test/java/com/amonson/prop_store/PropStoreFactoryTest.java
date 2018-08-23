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

import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropStoreFactoryTest {
    public static class MockPropStore extends PropStore {
        public MockPropStore(Map<String,?> config) {
            super(config);
        }

        @Override
        public String toString(PropMap map) {
            return null;
        }

        @Override
        public String toString(PropList array) {
            return null;
        }

        @Override
        public PropMap fromStringToMap(String storeText) {
            return null;
        }

        @Override
        public PropList fromStringToList(String storeText) {
            return null;
        }
    }
    public class MockPropStore2 extends PropStore {
        public MockPropStore2(Map<String,?> config) {
            super(config);
        }

        @Override
        public String toString(PropMap map) {
            return null;
        }

        @Override
        public String toString(PropList array) {
            return null;
        }

        @Override
        public PropMap fromStringToMap(String storeText) {
            return null;
        }

        @Override
        public PropList fromStringToList(String storeText) {
            return null;
        }
    }

    @Test
    public void allTests() {
        try {
            PropStoreFactory.instance("test1");
            fail();
        } catch(PropStoreFactoryException e) { /* PASS */ }
        assertTrue(PropStoreFactory.register("test1", MockPropStore.class, null));
        assertFalse(PropStoreFactory.register("test1", MockPropStore.class, null));
        assertNotNull(PropStoreFactory.instance("test1"));
        assertNotNull(PropStoreFactory.instance("test1"));
        assertTrue(PropStoreFactory.register("test2", MockPropStore2.class, null));
        try {
            PropStoreFactory.instance("test2");
            fail();
        } catch(PropStoreFactoryException e) { /* PASS */ }
    }

    @Test
    public void registeredImplementations() {
        PropStoreFactory.resetFactory();
        Collection<String> impls = PropStoreFactory.registeredImplementations();
        assertEquals(0, impls.size());
    }
}
