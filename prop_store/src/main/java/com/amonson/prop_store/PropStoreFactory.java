// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.prop_store;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
    public static PropStore getStore(String name) throws PropStoreFactoryException {
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
    public static PropStore getStore(String name, Properties args) throws PropStoreFactoryException {
        if(name == null)
            throw new IllegalArgumentException("The 'name' argument cannot be null!");
        name = name.toLowerCase().trim();
        if(name.trim().equals(""))
            throw new IllegalArgumentException("The 'name' argument cannot be empty!");
        if(!supportedImplementations_.containsKey(name))
            throw new PropStoreFactoryException(String.format("Implementation '%s' is not supported!", name));
        try {
            return supportedImplementations_.get(name).getConstructor(Properties.class).newInstance(args);
        } catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PropStoreFactoryException("Failed to construct PropStore; is the ctor", e); // This cannot happen!
        }
    }

    /**
     * Used to add user parsers/generators derived from PropStore.
     *
     * @param name The name of the parser to use. Cannot already exist in registry. Cannot be null.
     * @param classObject The class implementation derived from PropStore. Cannot be null.
     * @return True if the implementation was added to the registry. False if the 'name' already exists.
     * @throws PropStoreFactoryException When the implementation cannot be added because of a missing ctor. The ctor
     * for a PropStore derived class should take a java.util.Properties object.
     */
    public static boolean registerNewStore(String name, Class<? extends PropStore> classObject)
            throws PropStoreFactoryException {
        if(name == null)
            throw new IllegalArgumentException("The 'name' argument cannot be null!");
        name = name.toLowerCase().trim();
        if(name.equals(""))
            throw new IllegalArgumentException("The 'name' argument cannot be empty!");
        if(classObject == null) throw new IllegalArgumentException("The 'classObject' argument cannot be null!");
        try {
            classObject.getConstructor(Properties.class).newInstance(new Properties());
            return supportedImplementations_.putIfAbsent(name, classObject) == null;
        } catch(NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new PropStoreFactoryException("PropStore implementation doesn't appear to have a valid ctor!", e);
        }
    }

    /**
     * Gets the list of implementations supported.
     *
     * @return The collection of names supported by the factory.
     */
    public static Collection<String> getNames() {
        return supportedImplementations_.keySet();
    }

    /**
     * Setup supplied internal implementations.
     */
    static Map<String, Class<? extends PropStore>> supportedImplementations_ =
            new HashMap<>() {{
        put("json", JsonStore.class);
        put("yaml", YamlStore.class);
    }};
}
