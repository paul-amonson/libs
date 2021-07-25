// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.redis;

/**
 * Description for class RedisException
 */
public class RedisException extends Exception {
    RedisException(String msg) { super(msg); }
    RedisException(String msg, Throwable cause) { super(msg, cause); }

    private static final long serialVersionUID = 638743;
}
