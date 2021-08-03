// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.util.Properties;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class YamlStoreTest {
    @Test
    public void fromString1() throws PropStoreException {
        YamlStore store = new YamlStore(null);
        PropList list = store.fromStringToList(YAML1);
        Assertions.assertEquals(7, list.size());
        String yml = store.toString(list);
        Assertions.assertNotNull(yml);
    }

    @Test
    public void fromString2() throws PropStoreException {
        YamlStore store = new YamlStore(null);
        PropMap map = store.fromStringToMap(YAML2);
        Assertions.assertEquals(1, map.size());
        String yml = store.toString(map);
        Assertions.assertNotNull(yml);
    }

    @Test
    public void ctor1() throws PropStoreException {
        Properties config = new Properties();
        config.setProperty("indent", "4");
        YamlStore store = new YamlStore(config);
    }

    @Test
    public void ctor2() throws PropStoreException {
        Properties config = new Properties();
        config.setProperty("indent", "4");
        YamlStore store = new YamlStore(config);
    }

    @Test
    public void ctor3() throws PropStoreException {
        Properties config = new Properties();
        config.setProperty("other", "4");
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
