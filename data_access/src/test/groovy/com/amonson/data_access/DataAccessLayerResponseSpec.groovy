// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access

import com.amonson.prop_store.PropList
import spock.lang.Specification

class DataAccessLayerResponseSpec extends Specification {
    def "Test All"() {
        def ut = new DataAccessLayerResponse(STATUS, LIST)
        expect: ut.getResults() == RESULT1
        and:    ut.getStatus() == RESULT2
        where:
        STATUS       | LIST || RESULT1 | RESULT2
        null         | null || null    | null
        null         | list || list    | null
        status       | null || null    | status
        status       | list || list    | status
    }

    static PropList list = new PropList()
    static DataAccessLayerStatus status = DataAccessLayerStatus.IO_FAILURE
}
