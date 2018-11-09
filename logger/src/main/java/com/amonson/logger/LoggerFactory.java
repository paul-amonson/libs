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

package com.amonson.logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Static factory for creating Logger implementations.
 */
public final class LoggerFactory {
    private LoggerFactory() {}

    /**
     * Retrieves a singleton instance of a Logger class per name.
     *
     * @param name The named Logger to create/return.
     * @param implementation The name of the Logger implementation to create/return.
     * @param kwargs Optional arguments for the initialize method of the logger. By default this is ignored.
     * @return The Logger or null if a Logger cannot be created or the name is unknown.
     */
    public static Logger getNamedLogger(String name, String implementation, Object... kwargs) {
        String key = String.format("%s:%s", name, implementation);
        if(instances_.containsKey(key)) {
            lastSuccessfulLogger_ = key;
            return instances_.get(key);
        }
        if(implementations_.containsKey(implementation)) {
            Logger result = newInstance(implementations_.get(implementation), kwargs);
            if(result != null) {
                result.initialize(kwargs);
                instances_.put(key, result);
            }
            lastSuccessfulLogger_ = key;
            return result;
        }
        return null;
    }

    /**
     * Retrieves a singleton instance of a Logger class based on the last name and implementation.
     *
     * @return The Logger or null if a Logger cannot be created or the name is unknown.
     */
    public static Logger getLogger() {
        if(lastSuccessfulLogger_ == null)
            throw new RuntimeException("LoggerFactory.getNamedLogger must be called " +
                    "prior to using LoggerFactory.getLogger!");
        String[] parts = lastSuccessfulLogger_.split(":");
        return getNamedLogger(parts[0], parts[1]);
    }


    /**
     * Add an implementation of Logger to the factory. This will NOT replace implementations already inserted with the
     * given name.
     *
     * @param implementation The name of the implementation for the Logger derived class passed into this method.
     * @param loggerClass The class of the Logger derived class.
     * @return True if the Logger class is added and false if not added.
     */
    public static boolean addImplementation(String implementation, Class<? extends Logger> loggerClass) {
        if(!implementations_.containsKey(implementation)) {
            implementations_.put(implementation, loggerClass);
            return true;
        }
        return false;
    }

    private static Logger newInstance(Class<? extends Logger> loggerClass, Object... args) {
        try {
            return loggerClass.newInstance();
        } catch(IllegalAccessException | InstantiationException e) {
            return null;
        }
    }

    private static Map<String, Logger> instances_ = new HashMap<>();
    @SuppressWarnings("serial")
    private static Map<String, Class<? extends Logger>> implementations_ = new HashMap<String, Class<? extends Logger>>() {{
        put("console", com.amonson.logger.ConsoleLoggerImpl.class);
    }};
    static String  lastSuccessfulLogger_ = null;
}
