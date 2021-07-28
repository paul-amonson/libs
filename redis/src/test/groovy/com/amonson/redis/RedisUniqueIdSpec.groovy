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
        response_.get() >>> [ 16384L, 16384L ]

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
        when: new RedisUniqueId(client, db, name)
        then: thrown(IllegalArgumentException)
        where:
        client      | db | name
        null        | 0  | "testkey"
        Mock(Jedis) | -1 | "testKey"
        Mock(Jedis) | 0  | null
        Mock(Jedis) | 0  | ""
    }
}
