// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.factory;

import com.amonson.logger.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A generic class for creating factories for a Interface type.
 */
public class FactoryGeneric<Interface> {
    /**
     * Create an instance of an Interface implementation.
     *
     * @param instanceName The name associated with the class to create.
     * @param properties The Properties that may contain parameters for the instance constructor.
     * @param logger The logger passed to the instance constructor.
     * @return The constructed instance.
     * @throws FactoryException if the class is not registered or cannot be constructed.
     */
    public final Interface getInstance(String instanceName, Properties properties, Logger logger)
            throws FactoryException {
        if(!registeredClasses_.containsKey(instanceName))
            throw new FactoryException(String.format("The instance called '%s' has not been registered",
                    instanceName));
        try {
            Constructor<? extends Interface> ctor = registeredClasses_.get(instanceName).
                    getDeclaredConstructor(Properties.class, Logger.class);
            return ctor.newInstance(properties, logger);
        } catch(NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new FactoryException(String.format("Failed to create instance of '%s'", instanceName), e);
        }
    }

    /**
     * Create an singleton instance of an Interface implementation.
     *
     * @param instanceName  The name associated with the class to create or return.
     * @param properties The Properties that may contain parameters for the instance constructor when called.
     * @param logger The logger passed to the instance constructor.
     * @return The constructed instance or the previously create instance.
     * @throws FactoryException if the class is not registered or cannot be constructed.
     */
    public final Interface getSingletonInstance(String instanceName, Properties properties, Logger logger)
            throws FactoryException {
        if(singletonInstances_.containsKey(instanceName))
            return singletonInstances_.get(instanceName);
        Interface instance = getInstance(instanceName, properties, logger);
        singletonInstances_.put(instanceName, instance);
        return instance;
    }

    /**
     * Register a class with the factory.
     *
     * @param instanceName The name to associate with the class type.
     * @param classType The class to create when specified by the above instanceName.
     * @return true if added, false otherwise.
     */
    public final boolean registerClass(String instanceName, Class<? extends Interface> classType) {
        return registeredClasses_.putIfAbsent(instanceName, classType) == null;
    }

    /**
     * Unregister a class with the factory.
     *
     * @param instanceName The name associated with the class type.
     * @param classType The class associated with the above instanceName.
     * @return true if removed, false otherwise.
     */
    public final boolean unregisterClass(String instanceName, Class<? extends Interface> classType) {
        return registeredClasses_.remove(instanceName, classType);
    }

    final private Map<String,Class<? extends Interface>> registeredClasses_ = new HashMap<>();
    final private Map<String, Interface> singletonInstances_ = new HashMap<>();
}
