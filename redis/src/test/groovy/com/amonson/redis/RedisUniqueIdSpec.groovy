package com.amonson.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.Response
import redis.clients.jedis.Transaction
import spock.lang.Specification

class RedisUniqueIdSpec extends Specification {
    def jedis_
    def transaction_
    def response_

    def underTest_
    void setup() {
        jedis_ = Mock(Jedis)
        transaction_ = Mock(Transaction)
        response_ = Mock(Response<Long>)
        jedis_.multi() >> transaction_
        transaction_.incrBy(_ as String, _ as Long) >> response_
        transaction_.del(_ as String) >> Mock(Response)
        response_.get() >>> [ 8192L, 8192L ]

        underTest_ = new RedisUniqueId(jedis_, 0, "testKey")
    }

    def "Test Reset"() {
        underTest_.getNext() // 0
        underTest_.getNext() // 1
        underTest_.getNext() // 2
        underTest_.reset()
        expect: underTest_.getNext() == 0L
    }

    def "Test GetNext"() {
        expect: underTest_.getNext() == 0L
    }

    def "Test ctor() with bad inputs"() {
        when: new RedisUniqueId(client, db, name, size)
        then: thrown(IllegalArgumentException)
        where:
        client      | db | name      | size
        null        | 0  | "testkey" | 1000
        Mock(Jedis) | -1 | "testKey" | 1000
        Mock(Jedis) | 0  | null      | 1000
        Mock(Jedis) | 0  | ""        | 1000
        Mock(Jedis) | 0  | "testKey" | 10
    }
}
