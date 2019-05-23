// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class PropStoreTest {
    static final class MockPropStore extends PropStore {
        public MockPropStore(Properties config) {
            super(config);
        }

        @Override
        public String toString(PropMap map) {
            return "";
        }

        @Override
        public String toString(PropList array) {
            return "";
        }

        @Override
        public PropMap fromStringToMap(String storeText) {
            return new PropMap();
        }

        @Override
        public PropList fromStringToList(String storeText) {
            return new PropList();
        }
    }

    @Before
    public void setUp() throws Exception {
        try (Writer stream = new FileWriter(file_)) {
            stream.write("{\n  \"life\": 42\n}\n");
        }
    }

    @After
    public void tearDown() {
        file_.delete();
    }

    @Test
    public void readTests() throws Exception {
        PropStore store = new MockPropStore(null);
        store.readMap(file_.toString());
        store.readList(file_.toString());
    }

    @Test
    public void writeTests() throws Exception {
        PropStore store = new MockPropStore(null);
        PropList array = new PropList();
        PropMap map = new PropMap();
        store.writeTo(file_.toString(), array);
        store.writeTo(file_.toString(), map);
    }

    @Test(expected = FileNotFoundException.class)
    public void readTestsNegative1() throws Exception {
        PropStore store = new MockPropStore(null);
        store.readMap(badFile_.toString());
    }

    @Test(expected = FileNotFoundException.class)
    public void readTestsNegative2() throws Exception {
        PropStore store = new MockPropStore(null);
        store.readList(badFile_.toString());
    }

    @Test(expected = IOException.class)
    public void writeTestsNegative1() throws Exception {
        PropStore store = new MockPropStore(null);
        PropMap map = new PropMap();
        store.writeTo(badFile_.toString(), map);
    }

    @Test(expected = IOException.class)
    public void writeTestsNegative2() throws Exception {
        PropStore store = new MockPropStore(null);
        PropList array = new PropList();
        store.writeTo(badFile_.toString(), array);
    }

    private File file_ = new File("/tmp/test_file.txt");
    private File badFile_ = new File("/tmp/folder/does/not/exist/test_file2.txt");
}
