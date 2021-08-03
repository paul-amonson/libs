package com.amonson.data_access

import spock.lang.Specification

import java.util.logging.Logger

class DataAccessLayerSpec extends Specification {
    class Concrete extends DataAccessLayer {
        Concrete(Properties props, Logger logger) { super(props, logger) }
        @Override DataAccessLayerResponse query(String name, Object... params) { return null }
        @Override void query(DataAccessLayerCallback callback, String name, Object... params) {}
        @Override long queryForLong(String name, Object... params) throws DataAccessLayerException { return 0 }
        @Override void connect() {}
        @Override void disconnect() {}
        @Override boolean isConnected() { return false }
    }

    def "Test ctor"() {
        expect: new Concrete(Mock(Properties), Mock(Logger)) != null
    }
}
