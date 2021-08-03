// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access

import spock.lang.Specification

import java.util.logging.Logger

class DataAccessLayerFactorySpec extends Specification {
    def "Test ctor"() {
        expect: new DataAccessLayerFactory(Mock(Logger)) != null
    }
}
