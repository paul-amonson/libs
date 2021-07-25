package com.amonson.redis

import spock.lang.Specification

class RedisExceptionSpec extends Specification {
    def "Test ctors"() {
        expect: new RedisException("TEST") != null
        and: new RedisException("TEST", new Exception()) != null
    }
}
