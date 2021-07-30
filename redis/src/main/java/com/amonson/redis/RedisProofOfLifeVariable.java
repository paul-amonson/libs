// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.SetParams;

import java.sql.Date;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This creates variable on the redis server with a timeout value of twice the period (default 5000 ms). Every
 * period the variable is updated with a timestamp value. If this object is stopped then after at most the
 * period times 2 the variable will expire on the redis server and delete itself.
 */
public class RedisProofOfLifeVariable implements Runnable {
    /**
     * Construct an object with the Redis client and specified name with the default period of 5000ms.
     *
     * @param client The Redis client object to use in this object for communications.
     * @param db The Jedis DB to use fo the specified name.
     * @param name The name of the variable on the redis server.
     * @throws IllegalArgumentException When any argument is incorrect.
     */
    public RedisProofOfLifeVariable(Jedis client, int db, String name) {
        this(client, db, name, 5000);
    }

    /**
     * Construct an object with the Jedis client and specified name and period.
     *
     * @param client The Redis client object to use in this object for communications.
     * @param db The Redis DB index to select.
     * @param name The name of the variable on the redis server.
     * @param period The period in milliseconds of the redis server variable update, the redis variable expires
     *              in twice this value. Cannot be less than 500 ms.
     * @throws IllegalArgumentException When any argument is incorrect.
     */
    public RedisProofOfLifeVariable(Jedis client, int db, String name, int period) {
        if(client == null)
            throw new IllegalArgumentException("parameter 'client' cannot be null!");
        if(db < 0)
            throw new IllegalArgumentException("parameter 'db' must be greater than or equal to 0!");
        if(name == null || name.isBlank())
            throw new IllegalArgumentException("parameter 'name' cannot be null or blank!");
        if(period < MINIMUM_PERIOD)
            throw new IllegalArgumentException("cannot set period to less than 500!");
        client_ = client;
        db_ = db;
        name_ = "proofOfLife." + name;
        period_ = period;
        params_.ex(period_ / HALF_A_SECOND);
    }

    /**
     * Start the thread that starts the periodic update on the redis server. Will fail silently if the server
     * is already running.
     */
    @Override
    public void run() {
        if(thread_ == null) {
            thread_ = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Transaction trans = client_.multi();
                        trans.select(db_);
                        trans.set(name_, Date.from(Instant.now()).toString(), params_);
                        trans.exec();
                    } catch(JedisConnectionException | JedisDataException e) {
                        errorCount_.incrementAndGet();
                    }
                    try {
                        Thread.sleep(period_);
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            thread_.start();
        }
    }

    /**
     * Check to see if the thread is running the period updates.
     *
     * @return true if the thread is running, false otherwise.
     */
    public boolean isRunning() {
        return thread_ != null && thread_.isAlive();
    }

    /**
     * Stops a running proof of life thread doing the period update. Will fail silently if the server is not running.
     */
    public void stop() {
        if(isRunning()) {
            thread_.interrupt();
            try {
                thread_.join();
            } catch(InterruptedException e) { /* Ignore */ }
        }
    }

    private final Jedis client_;
    private final int db_;
    private final String name_;
    private final AtomicLong errorCount_ = new AtomicLong(0L);
    private final SetParams params_ = new SetParams();
    private final int period_;
    private       Thread thread_ = null;

    private static final int MINIMUM_PERIOD = 500;
    private static final int HALF_A_SECOND = 500;
}
