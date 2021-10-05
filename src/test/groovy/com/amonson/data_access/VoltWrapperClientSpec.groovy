// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access

import org.voltdb.VoltTable
import org.voltdb.VoltType
import org.voltdb.client.Client
import org.voltdb.client.ClientResponse
import org.voltdb.client.ClientStatusListenerExt
import spock.lang.Specification

import java.util.logging.Logger

class VoltWrapperClientSpec extends Specification {
    def list = new ArrayList<InetSocketAddress>()
    Properties props_
    VoltWrapperClient underTest
    ClientResponse catResponse_
    void setup() {
        new File("./build/tmp/test.sql").text = """-- Dummy test file... 
"""
        new File("./build/tmp/test.jar").text = ""
        props_ = new Properties()
        props_.setProperty("list_of_servers", "server1,server2")
        props_.setProperty("resource_file", "test.sql")
        props_.setProperty("filename", "./build/tmp/test.sql")
        props_.setProperty("jar_files", "./build/tmp/test.jar")
        underTest = new VoltWrapperClient(props_, Mock(Logger))
        underTest.client_ = Mock(Client)
        underTest.client_.getConnectedHostList() >> list
        catResponse_ = Mock(ClientResponse)
        catResponse_.getStatus() >> ClientResponse.SUCCESS
        VoltTable[] vt = new VoltTable[] { new VoltTable(new VoltTable.ColumnInfo("Dummy", VoltType.STRING)) }
        catResponse_.getResults() >> vt

        underTest.client_.callProcedure("@SystemCatalog", "tables") >> catResponse_
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
        underTest.client_.createConnection(spock.lang.Specification._ as String, spock.lang.Specification._ as Integer) >> { list.add(null); list.add(null) }

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
        underTest.client_.createConnection(spock.lang.Specification._ as String, spock.lang.Specification._ as Integer) >> { throw new IOException() }
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

    def "Test initialization"() {
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: underTest.initializeVoltDBAfterConnect()
    }

    def "Test initialization no resource 1"() {
        props_.remove("resource_file")
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: underTest.initializeVoltDBAfterConnect()
    }

    def "Test initialization no file 1"() {
        props_.remove("filename")
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: underTest.initializeVoltDBAfterConnect()
    }

    def "Test initialization no jar 1"() {
        props_.remove("jar_files")
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: underTest.initializeVoltDBAfterConnect()
    }

    def "Test initialization missing resource"() {
        props_.put("resource_file", "/tmp/missing.sql")
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: !underTest.initializeVoltDBAfterConnect()
    }

    def "Test initialization missing file"() {
        props_.put("filename", "/tmp/missing.sql")
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: !underTest.initializeVoltDBAfterConnect()
    }

    def "Test initialization missing jar"() {
        props_.put("jar_files", "/tmp/missing.jar")
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.SUCCESS
        underTest.client_.callProcedure("@AdHoc", _ as String) >> response
        underTest.client_.updateClasses(_ as File, _ as String) >> response
        expect: !underTest.initializeVoltDBAfterConnect()
    }
}
