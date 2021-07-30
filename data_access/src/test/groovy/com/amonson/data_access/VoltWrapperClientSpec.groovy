// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access

import org.voltdb.client.Client
import org.voltdb.client.ClientStatusListenerExt
import spock.lang.Specification

import java.util.logging.Logger

class VoltWrapperClientSpec extends Specification {
    def list = new ArrayList<InetSocketAddress>()
    def underTest
    void setup() {
        Properties props = new Properties()
        props.setProperty("list_of_servers", "server1,server2")
        underTest = new VoltWrapperClient(props, Mock(Logger))
        underTest.client_ = Mock(Client)
        underTest.client_.getConnectedHostList() >> list
    }

    static Properties props
    static Logger logger
    def "Test ctor negative"() {
        props = new Properties()
        if(ADD_SERVER)
            props.setProperty("list_of_servers", "")
       logger = Mock(Logger)
        when: new VoltWrapperClient(PROPS, LOGGER)
        then: thrown(AssertionError)
        where:
            PROPS | LOGGER | ADD_SERVER
            null  | logger | false
            props | null   | false
            props | logger | true
    }

    def "Test async and sync callProcedure"() {
        underTest.client_.createConnection(_ as String, _ as Integer) >> { list.add(null); list.add(null) }

        boolean waitRv = underTest.waitForConnection(130L)
        underTest.connect()
        underTest.connect()
        underTest.haveConnection()
        boolean wait2Rv = underTest.waitForConnection(30_000L)
        underTest.callProcedureAsync((cr) -> {}, "procedure")
        underTest.callProcedureSync("procedure")
        underTest.disconnect()
        underTest.disconnect()
        expect: !waitRv
        and:    wait2Rv
    }

    def "Test connect negative"() {
        underTest.client_.createConnection(_ as String, _ as Integer) >> { throw new IOException() }
        underTest.connectInternal("server1")
        expect: true
    }

    def "Test callbacks"() {
        underTest.connectionLost("server1", 50000, 1, ClientStatusListenerExt.DisconnectCause.TIMEOUT)
        underTest.connectionLost("server1", 50000, 0, ClientStatusListenerExt.DisconnectCause.TIMEOUT)
        underTest.connectionCreated("server1", 50000, ClientStatusListenerExt.AutoConnectionStatus.UNABLE_TO_CONNECT)
        underTest.connectionCreated("server3", 50000, ClientStatusListenerExt.AutoConnectionStatus.SUCCESS)
        expect: true
    }
}
