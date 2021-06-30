// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.config_location;

import com.amonson.prop_store.*;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Description for class ConfigLoader which finds and opens configuration to multiple sources. These sources are:
 *
 * <blockquote><table border=1>
 *     <tr style="background:lightgrey;font-weight:bold"><td>Name</td><td>Location</td></tr>
 *     <tr><td>system</td><td>/etc/<i>application_name</i>.d/</td></tr>
 *     <tr><td>custom</td><td>/<i>custom_path</i>/ Note: this overrides use of the application_name in the path</td></tr>
 *     <tr><td>user</td><td>${HOME}/.config/<i>application_name</i>.d/</td></tr>
 *     <tr><td>redis</td><td>Redis Server</td></tr>
 * </table></blockquote>
 *
 * <p>There is a definite order to the search for a specific configuration locations and and values can be overridden:</p>
 *
 * <ol>
 *     <li>user: This allows the user of an application to override all or some of the default configuration.</li>
 *     <li>system: This allows the system install to override any multi-system redis server configuration.</li>
 *     <li>redis: This allows the multi-system redis server to be the default source for configuration. Format must be JSON.</li>
 *     <li>custom: This specifies a specific folder with the configuration files in it.</li>
 * </ol>
 *
 * <p>Configuration files can be in one of 2 formats, JSON or YAML. The files must end with either a ".json" or ".yml".</p>
 *
 * Redis configuration if used must be found in /etc/<i>application_name</i>.{json|yml}.
 */
public class ConfigLoader {
    /**
     * Create a configuration file locator object for finding application configuration files or other streams.
     *
     * @param applicationName This is the application name that is used as the base filename for the indirect
     *                        configuration file in /etc. i.e. <b>/etc/<applicationName>.conf</b>.
     * @param logger Logger created from {@link java.util.logging.LogManager}.
     * @throws PropStoreFactoryException when the PropStore fails to load and parse the configuration file.
     * @throws IllegalArgumentException if any of the input arguments are null or applicationName is blank.
     */
    public ConfigLoader(String applicationName, Logger logger) throws PropStoreFactoryException {
        if(applicationName == null || applicationName.isBlank())
            throw new IllegalArgumentException("applicationName must not be null or empty");
        if(!Pattern.compile("[a-zA-Z]+[-_a-zA-Z0-9]*").matcher(applicationName).matches())
            throw new IllegalArgumentException("ApplicationName must be a valid name starting with a alpha character " +
                    "and containing alpha-numeric characters or a dash or underscore.");
        if(logger == null)
            throw new IllegalArgumentException("You must pass a valid logger to this class.");
        applicationName_ = applicationName;
        log_ = logger;
        userDir_ = System.getProperty("user.home") + "/.config/" + applicationName_ + ".d";
        systemDir_ = "/etc/" +  applicationName_ + ".d";
        jsonParser_ = PropStoreFactory.getStore("json");
        yamlParser_ = PropStoreFactory.getStore("yaml");
        baseConfig_ = new PropMap();
        getConfigurationByNameFromFile(ETC_DIR + "/" + applicationName_, baseConfig_);
        customDir_ = baseConfig_.getStringOrDefault(CUSTOM_DIR_KEY, null);
    }

    /**
     * Get the PropMap parsed from the specified configuration name.
     *
     * @param name The base name for the configuration to retrieve, this does not include the extension like
     *            ".json" or ".yml".
     * @return The map of the merged configurations. If there was no configuration found the PropMap will be
     * empty but not null.
     */
    public PropMap getConfigurationByName(String name) {
        PropMap config = new PropMap();
        getConfigurationByNameFromRedis(name, config);
        getConfigurationByNameFromSystem(name, config);
        getConfigurationByNameFromCustom(name, config);
        getConfigurationByNameFromUser(name, config);
        return config;
    }

    private void getConfigurationByNameFromRedis(String name, PropMap config) {
        createRedisClient();
        if(redisClient_ != null) {
            if(redisClient_.hexists(applicationName_, name)) {
                try {
                    jsonParser_.fromStringToMap(redisClient_.hget(applicationName_, name));
                } catch(PropStoreException e) {
                    // Treat bad data as no data but log it.
                    log_.warning(e.getMessage());
                }
            }
        }
        closeRedisClient();
    }

    private void getConfigurationByNameFromCustom(String name, PropMap config) {
        if(customDir_ != null)
            getConfigurationByNameFromFile(customDir_ + "/" + name, config);
    }

    private void getConfigurationByNameFromUser(String name, PropMap config) {
        getConfigurationByNameFromFile(userDir_ + "/" + name, config);
    }

    private void getConfigurationByNameFromSystem(String name, PropMap config) {
        getConfigurationByNameFromFile(systemDir_ + "/" + name, config);
    }

    private void getConfigurationByNameFromFile(String prefix, PropMap config) {
        File file = new File(prefix + ".json");
        PropStore parser_ = jsonParser_;
        if(!file.canRead()) {
            file = new File(prefix + ".yml");
            if(!file.canRead())
                return;
            parser_ = yamlParser_;
        }
        try (InputStream stream = new FileInputStream(file)) {
            mergeProp(parser_.readMap(stream), config);
        } catch(IOException | PropStoreException e) {
            log_.warning(e.getMessage());
        }
    }

    private void mergeProp(PropMap newProps, PropMap target) { // On type mismatch newProps takes precedence.
        for(String key: newProps.keySet())
            if(newProps.get(key) instanceof PropMap && target.containsKey(key) && target.get(key) instanceof PropMap)
                mergeProp(newProps.getMap(key), target.getMap(key));
            else
                target.put(key, newProps.get(key)); // Arrays are not merged, only replaced.
    }

    private void closeRedisClient() {
        if(redisClient_ != null) {
            redisClient_.close();
            redisClient_ = null;
        }
    }

    private void createRedisClient() {
        if(redisClient_ == null && baseConfig_.getString(REDIS_SERVER_KEY) != null) {
            HostAndPort authority = new HostAndPort(baseConfig_.getString(REDIS_SERVER_KEY),
                    baseConfig_.getInteger(REDIS_PORT_KEY));
            redisClient_ = factory_.create(authority);
            if(redisClient_ != null) {
                if (baseConfig_.getStringOrDefault(REDIS_SECRET_KEY, null) != null)
                    redisClient_.auth(baseConfig_.getString(REDIS_SECRET_KEY));
                redisClient_.connect();
                redisClient_.select(baseConfig_.getIntegerOrDefault(REDIS_DB_NUMBER, 0));
            }
        }
    }

    private Jedis createInternalClient(HostAndPort authority) {
        try {
            return new Jedis(authority);
        } catch(JedisConnectionException e) {
            log_.warning(e.getMessage());
            return null;
        }
    }

    private final String applicationName_;
    private final Logger log_;
    private       String userDir_; // not marked final for UT.
    private       String systemDir_; // not marked final for UT.
    private       String customDir_; // not marked final for UT.
    private final PropMap baseConfig_;
    private final PropStore jsonParser_;
    private final PropStore yamlParser_;
    private       Jedis redisClient_;
    private       JedisFactory factory_ = this::createInternalClient; // not marked final for UT.

    private static       String ETC_DIR = "/etc"; // not marked final for UT.
    private static final String CUSTOM_DIR_KEY = "custom_dir";
    private static final String REDIS_SERVER_KEY = "redis_server";
    private static final String REDIS_PORT_KEY = "redis_port";
    private static final String REDIS_SECRET_KEY = "redis_secret";
    private static final String REDIS_DB_NUMBER = "redis_db";

    @FunctionalInterface // Allows mocking of the Jedis object creation.
    interface JedisFactory {
        Jedis create(HostAndPort hp);
    }
}
