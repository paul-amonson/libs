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

package com.amonson.json;

import com.amonson.prop_store.*;
import java.util.Map;
import com.github.cliftonlabs.json_simple.*;

public class JsonStore extends PropStore {
    public JsonStore(Map<String, ?> config) {
        super(config);
        if(config != null) {
            Object obj = config.get("indent");
            if (obj instanceof String) indent_ = Integer.parseInt((String) obj);
            else if (obj instanceof Number) indent_ = ((Number) obj).intValue();
        }
    }

    @Override
    public String toString(PropMap map) {
        JsonObject json = new JsonObject(map);
        String str = json.toJson();
        if(indent_ > 0 && indent_ <= 8) str = Jsoner.prettyPrint(str, indent_);
        return str;
    }

    @Override
    public String toString(PropList array) {
        JsonArray json = new JsonArray(array);
        String str = json.toJson();
        if(indent_ > 0 && indent_ <= 8) str = Jsoner.prettyPrint(str, indent_);
        return str;
    }

    @Override
    public PropMap fromStringToMap(String storeText) throws PropStoreException {
        Object obj;
        try {
            obj = Jsoner.deserialize(storeText);
        } catch(JsonException e) {
            throw new PropStoreException("Failed to parse the text as JSON!", e);
        }
        if(obj instanceof JsonObject)
            return parseMap((JsonObject)obj);
        throw new PropStoreException("The JSON text was not a map!");
    }

    @Override
    public PropList fromStringToList(String storeText) throws PropStoreException {
        Object obj;
        try {
            obj = Jsoner.deserialize(storeText);
        } catch(JsonException e) {
            throw new PropStoreException("Failed to parse the text as JSON!", e);
        }
        if(obj instanceof JsonArray)
            return parseArray((JsonArray)obj);
        throw new PropStoreException("The JSON text was not a list!");
    }

    private PropMap parseMap(JsonObject map) {
        PropMap result = new PropMap();
        for(String key: map.keySet()) {
            if(map.get(key) instanceof JsonObject)
                result.put(key, parseMap((JsonObject)map.get(key)));
            else if(map.get(key) instanceof JsonArray)
                result.put(key, parseArray((JsonArray)map.get(key)));
            else
                result.put(key, map.get(key));
        }
        return result;
    }

    private PropList parseArray(JsonArray array) {
        PropList result = new PropList();
        for (Object anArray : array) {
            if (anArray instanceof JsonObject)
                result.add(parseMap((JsonObject)anArray));
            else if (anArray instanceof JsonArray)
                result.add(parseArray((JsonArray)anArray));
            else
                result.add(anArray);
        }
        return result;
    }

    private int indent_ = 0;
}
