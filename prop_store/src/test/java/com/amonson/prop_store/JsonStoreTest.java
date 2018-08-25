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

import com.amonson.prop_store.JsonStore;
import com.amonson.prop_store.PropList;
import com.amonson.prop_store.PropMap;
import com.amonson.prop_store.PropStoreException;
import java.util.HashMap;

import org.junit.Test;

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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", 3);
        }});
        PropMap data = store.fromStringToMap(originalJson);
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", "2");
        }});
        PropMap data = store.fromStringToMap(originalJson);
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", null);
        }});
        PropMap data = store.fromStringToMap(originalJson);
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", -1);
        }});
        PropMap data = store.fromStringToMap(originalJson);
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", 9);
        }});
        PropMap data = store.fromStringToMap(originalJson);
        String json = store.toString(data);
    }

    @Test(expected = PropStoreException.class)
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
        store.fromStringToList(originalJson);
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
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", 9);
        }});
        PropList data = store.fromStringToList(originalJson);
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", -1);
        }});
        PropList data = store.fromStringToList(originalJson);
        String json = store.toString(data);
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
        JsonStore store = new JsonStore(new HashMap<String,Object>() {{
            put("indent", 2);
        }});
        PropList data = store.fromStringToList(originalJson);
        String json = store.toString(data);
    }

    @Test(expected = PropStoreException.class)
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
        store.fromStringToMap(originalJson);
    }

    @Test(expected = PropStoreException.class)
    public void badJson1() throws Exception {
        String originalJson = "{]";
        JsonStore store = new JsonStore(null);
        store.fromStringToMap(originalJson);
    }

    @Test(expected = PropStoreException.class)
    public void badJson2() throws Exception {
        String originalJson = "{]";
        JsonStore store = new JsonStore(null);
        store.fromStringToList(originalJson);
    }
}