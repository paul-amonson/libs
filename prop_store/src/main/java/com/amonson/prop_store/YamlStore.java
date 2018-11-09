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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.*;

/**
 * PropStore implementation for YAML.
 */
class YamlStore extends PropStore {
    /**
     * Default constructor that takes a amp of configuration values for the property store.
     *
     * @param config The Properties of configuration parameters that may or may not be used by the derived classes.
     *              This may be null.
     */
    public YamlStore(Properties config) {
        super(config);
        int indent = 2;
        if(config != null)
            indent = Integer.parseInt(config.getProperty("com.amonson.prop_store.indent", "2"));
        DumperOptions options = new DumperOptions();
        options.setIndent(indent);
        options.setPrettyFlow(true);
        options.setExplicitStart(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        store_ = new Yaml(options);
    }

    /**
     * Convert the PropMap to the text Store format.
     *
     * @param map The map to Store.
     * @return The String version of the PropMap.
     */
    @Override
    public String toString(PropMap map) {
        return store_.dump(map);
    }

    /**
     * Convert the PropList to the text Store format.
     *
     * @param list The list to Store.
     * @return The String version of the PropList.
     */
    @Override
    public String toString(PropList list) {
        return store_.dump(list);
    }

    /**
     * Convert a String representation of the properties to a PropMap.
     *
     * @param storeText The String representation of the properties.
     * @return The parsed PropMap.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    @Override
    public PropMap fromStringToMap(String storeText) throws PropStoreException {
        return parseMap(store_.load(storeText));
    }

    /**
     * Convert a String representation of the properties to a PropList.
     *
     * @param storeText The String representation of the properties.
     * @return The parsed PropList.
     * @throws PropStoreException is thrown when the implementation cannot parse the text representation.
     */
    @Override
    public PropList fromStringToList(String storeText) throws PropStoreException {
        return parseArray(store_.load(storeText));
    }

    @SuppressWarnings("unchecked")
    private PropMap parseMap(Map<? extends String, ?> map) {
        PropMap result = new PropMap();
        for(String key: map.keySet()) {
            if(map.get(key) instanceof Map)
                result.put(key, parseMap((Map<String,Object>)map.get(key)));
            else if(map.get(key) instanceof List)
                result.put(key, parseArray((List<Object>)map.get(key)));
            else
                result.put(key, map.get(key));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private PropList parseArray(List<?> array) {
        PropList result = new PropList();
        for (Object anArray : array) {
            if (anArray instanceof Map)
                result.add(parseMap((Map<String, Object>)anArray));
            else if (anArray instanceof List)
                result.add(parseArray((List<Object>)anArray));
            else
                result.add(anArray);
        }
        return result;
    }

    private Yaml store_;
}
