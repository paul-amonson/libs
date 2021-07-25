// Copyright (C) 2019 Paul Amonson
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
     * Construct a object to get a unique ID from the specified name with a default caching value of 1000.
     *
     * @param client The Redis client object to use in this object for communications.
     * @param db The Redis Server DB number (0-max of Redis server).
     * @param name The name of the unique ID to access on the redis server.
     * @throws IllegalArgumentException When the input arguments are incorrect.
     */
    public RedisUniqueId(Jedis client, int db, String name) {
        this(client, db, name, 8_192);
    }

    /**
     * Construct a object to get a unique ID from the specified name with a specified caching value.
     *
     * @param client The Jedis client object to use in this object for communications.
     * @param db The Redis Server DB number (0-max of server).
     * @param name The name of the unique ID to access on the redis server.
     * @param cacheSize The number of locally cached values before retrieving a new base. The more IDs you need per
     *                  second the larger this number should be. So 2^63 - 2^13 = 2^50 @ 8,192 cache size. This gives
     *                  1.126 X 10^15 numbers or 1,258,999,068,400,000 caches of IDs of 8,192 each. At 65,536 IDs per
     *                  second this a lifetime of ~4.5 million years. At 262,144 IDs per second this is ~1.1 million
     *                  years.
     * @throws IllegalArgumentException When the input arguments are incorrect.
     */
    public RedisUniqueId(Jedis client, int db, String name, int cacheSize) {
        if(client == null)
            throw new IllegalArgumentException("parameter 'client' cannot be null!");
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("parameter 'name' cannot be null or blank!");
        if(cacheSize < MIN_CACHE_SIZE)
            throw new IllegalArgumentException(String.format("parameter 'cacheSize' must be greater than or equal " +
                    "to %sms!", MIN_CACHE_SIZE));
        if(db < 0)
            throw new IllegalArgumentException("parameter 'db' must be greater than or equal to 0!");
        client_ = client;
        db_ = db;
        name_ = "uniqueId." + name;
        cacheSize_ = cacheSize;
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
        if((current_ % cacheSize_) == 0) {
            try (Transaction trans = client_.multi()) {
                trans.select(db_);
                Response<Long> response = trans.incrBy(name_, cacheSize_);
                trans.exec();
                current_ = response.get() - cacheSize_;
            }
        }
        return current_++;
    }

    private final Jedis client_;
    private final String name_;
    private final int db_;
    private       long current_ = 0L;
    private final int cacheSize_;

    private static final int MIN_CACHE_SIZE = 100;
}
