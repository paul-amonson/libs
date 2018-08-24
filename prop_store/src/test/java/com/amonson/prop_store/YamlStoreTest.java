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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class YamlStoreTest {
    @Test
    public void fromString1() throws PropStoreException {
        YamlStore store = new YamlStore(null);
        PropList list = store.fromStringToList(YAML1);
        assertEquals(7, list.size());
        String yml = store.toString(list);
        assertNotNull(yml);
    }

    @Test
    public void fromString2() throws PropStoreException {
        YamlStore store = new YamlStore(null);
        PropMap map = store.fromStringToMap(YAML2);
        assertEquals(1, map.size());
        String yml = store.toString(map);
        assertNotNull(yml);
    }

    @Test
    public void ctor1() throws PropStoreException {
        Map<String, Object> config = new HashMap<>();
        config.put("indent", 4);
        YamlStore store = new YamlStore(config);
    }

    @Test
    public void ctor2() throws PropStoreException {
        Map<String, Object> config = new HashMap<>();
        config.put("indent", "4");
        YamlStore store = new YamlStore(config);
    }

    @Test
    public void ctor3() throws PropStoreException {
        Map<String, Object> config = new HashMap<>();
        config.put("other", 4);
        YamlStore store = new YamlStore(config);
    }

    static private String YAML1 = "" +
            "---\n" +
            "- step:  &id001                  # defines anchor label &id001\n" +
            "    instrument:      Lasik 2000\n" +
            "    pulseEnergy:     5.4\n" +
            "    pulseDuration:   12\n" +
            "    repetition:      1000\n" +
            "    spotSize:        1mm\n" +
            "\n" +
            "- step: &id002\n" +
            "    instrument:      Lasik 2000\n" +
            "    pulseEnergy:     5.0\n" +
            "    pulseDuration:   10\n" +
            "    repetition:      500\n" +
            "    spotSize:        2mm\n" +
            "- step: *id001                   # refers to the first step (with anchor &id001)\n" +
            "- step: *id002                   # refers to the second step\n" +
            "- step:\n" +
            "    <<: *id001\n" +
            "    spotSize: 2mm                # redefines just this key, refers rest from &id001\n" +
            "- step: *id002\n" +
            "\n" +
            "- numbers:\n" +
            "    a: A\n" +
            "    b: B\n" +
            "    c: C\n" +
            "    d:\n" +
            "      - 0\n" +
            "      - 1\n" +
            "      - 2\n" +
            "      - [3]";

    static private String YAML2 = "---\nroot:\n" +
            "- step:  &id001                  # defines anchor label &id001\n" +
            "    instrument:      Lasik 2000\n" +
            "    pulseEnergy:     5.4\n" +
            "    pulseDuration:   12\n" +
            "    repetition:      1000\n" +
            "    spotSize:        1mm\n" +
            "\n" +
            "- step: &id002\n" +
            "    instrument:      Lasik 2000\n" +
            "    pulseEnergy:     5.0\n" +
            "    pulseDuration:   10\n" +
            "    repetition:      500\n" +
            "    spotSize:        2mm\n" +
            "- step: *id001                   # refers to the first step (with anchor &id001)\n" +
            "- step: *id002                   # refers to the second step\n" +
            "- step:\n" +
            "    <<: *id001\n" +
            "    spotSize: 2mm                # redefines just this key, refers rest from &id001\n" +
            "- step: *id002\n" +
            "\n";
}
