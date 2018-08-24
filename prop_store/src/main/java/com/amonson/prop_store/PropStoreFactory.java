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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Static class to register and create PropStore derived implementations for property serialization.
 */
public final class PropStoreFactory {
    private PropStoreFactory() {}

    /**
     * Retrieve an instance of the named implementation.
     *
     * @param name The name of the implementation to create.
     * @return THe new instance of the PropStore derived class.
     * @throws PropStoreFactoryException is thrown when the instance cannot be created or the name is not registered.
     */
    public static PropStore getStore(String name) {
        return getStore(name, null);
    }

    /**
     * Retrieve an instance of the named implementation with implementaion specific arguments..
     *
     * @param name The name of the implementation to create.
     * @param args The arguments in a map for the implementation, null is allowed.
     * @return THe new instance of the PropStore derived class.
     * @throws PropStoreFactoryException is thrown when the instance cannot be created or the name is not registered.
     */
    public static PropStore getStore(String name, Map<String, ?> args) {
        name = name.toLowerCase().trim();
        if(!supportedImplementations_.contains(name))
            throw new PropStoreFactoryException(String.format("Implementation '%s' is not supported!", name));
        switch(name) {
            case "json":
                return new JsonStore(args);
            case "yaml":
                return new YamlStore(args);
            default:
                return null; // This cannot happen but the compiler must be satisfied.
        }
    }

    /**
     * Gets the list of implementations supported.
     *
     * @return The collection of names supported by the factory.
     */
    public static Collection<String> getNames() {
        return supportedImplementations_;
    }

    @SuppressWarnings("serial")
    private static final Collection<String> supportedImplementations_ = new ArrayList<String>() {{
        add("json");
        add("yaml");
    }};
}
