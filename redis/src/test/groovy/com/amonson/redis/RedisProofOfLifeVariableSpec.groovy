package com.amonson.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.Transaction
import spock.lang.Specification

class RedisProofOfLifeVariableSpec extends Specification {
    def client_

    def underTest_
    def setup() {
        client_ = Mock(Jedis)
        client_.multi() >> Mock(Transaction)

        underTest_ = new RedisProofOfLifeVariable(client_, 0, "testKey")
    }

    def "Test Run"() {
        underTest_.run();
        underTest_.run();
        expect: underTest_.isRunning()
        underTest_.stop()
        underTest_.stop()
    }

    def "Test IsRunning"() {
        expect: underTest_.isRunning() == false
    }

    def "Test Stop"() {
        expect: true
    }

    def "Test ctor() with bad input"() {
        given:
        when: new RedisProofOfLifeVariable(client, db, name, period)
        then: thrown(IllegalArgumentException)
        where:
        client      | db | name | period
        null        | 0  | "A"  | 5000
        Mock(Jedis) | -1 | "A"  | 5000
        Mock(Jedis) | 0  | null | 5000
        Mock(Jedis) | 0  | ""   | 5000
        Mock(Jedis) | 0  | "A"  | 50
    }
}
