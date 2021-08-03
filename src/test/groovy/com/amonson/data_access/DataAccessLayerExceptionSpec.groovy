// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access

import spock.lang.Specification

class DataAccessLayerExceptionSpec extends Specification {
    def "Test ctors"() {
        def s1 = new DataAccessLayerException(DataAccessLayerStatus.IO_FAILURE).getMessage()
        def s2 = new DataAccessLayerException(DataAccessLayerStatus.IO_FAILURE, "message").getMessage()
        def s3 = new DataAccessLayerException(DataAccessLayerStatus.IO_FAILURE, new Exception()).getMessage()
        def s4 = new DataAccessLayerException(DataAccessLayerStatus.IO_FAILURE, "message", new Exception()).getMessage()

        expect: s1 == "Failed with status: IO_FAILURE"
        and:    s2 == "message"
        and:    s3 == "java.lang.Exception"
        and:    s4 == "message"
    }
}
