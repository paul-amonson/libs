// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.util.Properties;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class JsonStoreTest {
    @Test
    public void roundTripMap() throws Exception {
        String originalJson = "{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}\n";
        JsonStore store = new JsonStore(new Properties() {{
            setProperty("com.amonson.prop_store.indent", "3");
        }});
        PropMap data = store.fromStringToMap(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripMap2() throws Exception {
        String originalJson = "{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}\n";
        JsonStore store = new JsonStore(new Properties());
        PropMap data = store.fromStringToMap(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripMap3() throws Exception {
        String originalJson = "{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}\n";
        JsonStore store = new JsonStore(new Properties());
        PropMap data = store.fromStringToMap(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripMap4() throws Exception {
        String originalJson = "{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}\n";
        JsonStore store = new JsonStore(new Properties() {{
            setProperty("com.amonson.prop_store.indent", "-1");
        }});
        PropMap data = store.fromStringToMap(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripMap5() throws Exception {
        String originalJson = "{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}\n";
        JsonStore store = new JsonStore(new Properties() {{
            setProperty("com.amonson.prop_store.indent", "9");
        }});
        PropMap data = store.fromStringToMap(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripMapNegative() throws Exception {
        String originalJson = "{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}\n";
        JsonStore store = new JsonStore(null);
        Assertions.assertThrows(PropStoreException.class, () -> {
            store.fromStringToList(originalJson);
        });
    }

    @Test
    public void roundTripArray() throws Exception {
        String originalJson = "[{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}]\n";
        JsonStore store = new JsonStore(null);
        PropList data = store.fromStringToList(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripArray2() throws Exception {
        String originalJson = "[{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}]\n";
        JsonStore store = new JsonStore(new Properties() {{
            setProperty("com.amonson.prop_store.indent", "9");
        }});
        PropList data = store.fromStringToList(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripArray3() throws Exception {
        String originalJson = "[{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}]\n";
        JsonStore store = new JsonStore(new Properties() {{
            setProperty("com.amonson.prop_store.indent", "-1");
        }});
        PropList data = store.fromStringToList(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripArray4() throws Exception {
        String originalJson = "[{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}]\n";
        JsonStore store = new JsonStore(new Properties() {{
            setProperty("com.amonson.prop_store.indent", "2");
        }});
        PropList data = store.fromStringToList(originalJson);
        Assertions.assertNotNull(store.toString(data));
    }

    @Test
    public void roundTripArrayNegative() throws Exception {
        String originalJson = "[{\n" +
                "  \"number\": 1.0,\n" +
                "  \"string\": \"test string\",\n" +
                "  \"map\": {\n" +
                "    \"list\": [ 3.0, [4.0] ]\n" +
                "  },\n" +
                "  \"list\": [\"list string\", 2.0, {\"array\": []}, [ \"\" ]]\n" +
                "}]\n";
        JsonStore store = new JsonStore(null);
        Assertions.assertThrows(PropStoreException.class, () -> {
            store.fromStringToMap(originalJson);
        });
    }

    @Test
    public void badJson1() throws Exception {
        String originalJson = "{]";
        JsonStore store = new JsonStore(null);
        Assertions.assertThrows(PropStoreException.class, () -> {
            store.fromStringToMap(originalJson);
        });
    }

    @Test
    public void badJson2() throws Exception {
        String originalJson = "{]";
        JsonStore store = new JsonStore(null);
        Assertions.assertThrows(PropStoreException.class, () -> {
            store.fromStringToList(originalJson);
        });
    }
}
