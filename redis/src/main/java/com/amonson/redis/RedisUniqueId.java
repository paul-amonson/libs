// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

/**
 * Class to retrieve a unique ID by name from a Redis server.
 */
public class RedisUniqueId {
    /**
     * Construct a object to get a unique ID from the specified name with a specified caching value.
     *
     * @param client The Jedis client object to use in this object for communications.
     * @param db The Redis Server DB number (0-max of server).
     * @param name The name of the unique ID to access on the redis server.
     * @throws IllegalArgumentException When the input arguments are incorrect.
     */
    public RedisUniqueId(Jedis client, int db, String name) {
        if(client == null)
            throw new IllegalArgumentException("parameter 'client' cannot be null!");
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("parameter 'name' cannot be null or blank!");
        if(db < 0)
            throw new IllegalArgumentException("parameter 'db' must be greater than or equal to 0!");
        client_ = client;
        db_ = db;
        name_ = "uniqueId." + name;
    }

    /**
     * Reset the redis unique named value to 0
     */
    public void reset() {
        try (Transaction trans = client_.multi()) {
            trans.select(db_);
            trans.del(name_);
            trans.exec();
            current_ = 0L;
        }
    }

    /**
     * Get the next unique ID.
     *
     * @return The ID from the server or the local cache.
     */
    public long getNext() {
        if((current_ % CACHE_SIZE) == 0) {
            try (Transaction trans = client_.multi()) {
                trans.select(db_);
                Response<Long> response = trans.incrBy(name_, CACHE_SIZE);
                trans.exec();
                current_ = response.get() - CACHE_SIZE;
            }
        }
        return current_++;
    }

    private final Jedis client_;
    private final String name_;
    private final int db_;
    private       long current_ = 0L;

    private static final int CACHE_SIZE = 16_384;
}
