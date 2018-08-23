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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Static class to register and create PropStore derived implementations for property serialization.
 */
public final class PropStoreFactory {
    private PropStoreFactory() {}

    /**
     * Register a class to be an implementation of a PropStore.
     *
     * @param name The name of the new implementation for property store serialization.This will not overwrite an
     *            existing entry with the same name.
     * @param classObject The Class of the implementation which must be derived from PropStore.
     * @param creationArgs The arguments used in creating the instances for the given name. This is implementation
     *                     specific.
     * @return true if the class and arguments were added; false otherwise.
     */
    public static boolean register(String name, Class<? extends PropStore> classObject, Map<String, ?> creationArgs) {
        argumentsMap_.putIfAbsent(name, creationArgs);
        return classMap_.putIfAbsent(name, classObject) == null;
    }

    /**
     * Retrieve an instance of the named implementation.
     *
     * @param name The name of the implementation to create.
     * @return THe new instance of the PropStore derived class.
     * @throws PropStoreFactoryException is thrown when the instance cannot be created or the name is not registered.
     */
    public static PropStore instance(String name) {
        if(classMap_.containsKey(name)) {
            if(instanceMap_.containsKey(name))
                return instanceMap_.get(name);
            else {
                try {
                    Class<? extends PropStore> theClass = classMap_.get(name);
                    Constructor<? extends PropStore> ctor = theClass.getConstructor(Map.class);
                    PropStore result = ctor.newInstance(argumentsMap_.get(name));
                    instanceMap_.put(name, result);
                    return result;
                } catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new PropStoreFactoryException(e);
                }
            }
        } else
            throw new PropStoreFactoryException(String.format("Failed to find the PropStore named '%s'!", name));
    }

    /**
     * Gets the list of registered implementations.
     *
     * @return The list of names registered with the factory.
     */
    public static Collection<String> registeredImplementations() {
        return classMap_.keySet();
    }

    /**
     * Remove all registered classes, arguments, and remembered instances.
     */
    public static void resetFactory() {
        classMap_.clear();
        instanceMap_.clear();
        argumentsMap_.clear();
    }

    private static Map<String, Class<? extends PropStore>> classMap_ = new HashMap<>();
    private static Map<String, PropStore> instanceMap_ = new HashMap<>();
    private static Map<String, Map<String, ?>> argumentsMap_ = new HashMap<>();
}
