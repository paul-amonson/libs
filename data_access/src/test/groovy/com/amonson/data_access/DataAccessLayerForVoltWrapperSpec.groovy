// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access

import org.voltdb.VoltTable
import org.voltdb.VoltType
import org.voltdb.client.ClientResponse
import org.voltdb.client.ProcCallException
import org.voltdb.client.ProcedureCallback
import spock.lang.Specification

import java.time.Instant
import java.util.logging.Logger

class DataAccessLayerForVoltWrapperSpec extends Specification {
    VoltWrapperClient client

    def underTest
    void setup() {
        Properties props = new Properties()
        props.setProperty("list_of_servers", "")
        underTest = new DataAccessLayerForVoltWrapper(props, Mock(Logger))
        client = Mock(VoltWrapperClient)
        underTest.client_ = client
    }

    def "Test connect and disconnect"() {
        underTest.connect()
        underTest.disconnect()
        expect: !underTest.isConnected()
    }

    def "Test query"() {
        ClientResponse response = Mock(ClientResponse)
        VoltTable table = new VoltTable(new VoltTable.ColumnInfo[] {
                new VoltTable.ColumnInfo("c1", VoltType.STRING),
                new VoltTable.ColumnInfo("c2", VoltType.BIGINT),
                new VoltTable.ColumnInfo("c3", VoltType.FLOAT),
                new VoltTable.ColumnInfo("c4", VoltType.TIMESTAMP),
                new VoltTable.ColumnInfo("c5", VoltType.DECIMAL),
                new VoltTable.ColumnInfo("c5", VoltType.VARBINARY),
        })
        table.addRow("string", 8_589_934_592L, (double)42.1234,
                Instant.now().toEpochMilli() * 1_000L, (BigDecimal)27.1234, new byte[4])
        response.getStatus() >> ClientResponse.SUCCESS
        response.getResults() >> [ table ]
        underTest.client_.callProcedureSync(_ as String, _ as Object[]) >> response
        DataAccessLayerResponse dalResponse = underTest.query("procedure", 1, "string")
        expect: dalResponse.getStatus() == DataAccessLayerStatus.SUCCESS
    }

    def "Test query negative"() {
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.CONNECTION_LOST
        response.getResults() >> [ ]
        underTest.client_.callProcedureSync(_ as String, _ as Object[]) >> response
        DataAccessLayerResponse dalResponse = underTest.query("procedure", 1, "string")
        expect: dalResponse.getStatus() == DataAccessLayerStatus.SERVER_ERROR
    }

    def "Test query negative 2"() {
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.CONNECTION_LOST
        response.getResults() >> [ ]
        underTest.client_.callProcedureSync(_ as String, _ as Object[]) >> { throw new IOException("TEST") }
        DataAccessLayerResponse dalResponse = underTest.query("procedure", 1, "string")
        expect: dalResponse.getStatus() == DataAccessLayerStatus.IO_FAILURE
    }

    def "Test query negative 3"() {
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.CONNECTION_LOST
        response.getResults() >> [ ]
        underTest.client_.callProcedureSync(_ as String, _ as Object[]) >> { throw new ProcCallException(response) }
        DataAccessLayerResponse dalResponse = underTest.query("procedure", 1, "string")
        expect: dalResponse.getStatus() == DataAccessLayerStatus.PROCEDURE_FAILURE
    }

    def "Test async query"() {
        ClientResponse response = Mock(ClientResponse)
        VoltTable table = new VoltTable(new VoltTable.ColumnInfo[] {
                new VoltTable.ColumnInfo("c1", VoltType.STRING),
                new VoltTable.ColumnInfo("c2", VoltType.BIGINT),
                new VoltTable.ColumnInfo("c3", VoltType.FLOAT),
                new VoltTable.ColumnInfo("c4", VoltType.TIMESTAMP),
                new VoltTable.ColumnInfo("c5", VoltType.DECIMAL),
                new VoltTable.ColumnInfo("c5", VoltType.VARBINARY),
        })
        table.addRow("string", 8_589_934_592L, (double)42.1234,
                Instant.now().toEpochMilli() * 1_000L, (BigDecimal)27.1234, new byte[4])
        response.getStatus() >> ClientResponse.SUCCESS
        response.getResults() >> [ table ]
        DataAccessLayerResponse finalResponse
        underTest.client_.callProcedureAsync(_ as ProcedureCallback, _ as String, _ as Object[]) >> { cb, proc, obj ->
            cb.clientCallback(response)
        }
        underTest.query((r) -> {
            finalResponse = r
        }, "procedure", 1, "string")
        expect: finalResponse != null
        and:    finalResponse.getStatus() == DataAccessLayerStatus.SUCCESS
    }

    def "Test async query negative 1"() {
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.OPERATIONAL_FAILURE
        response.getResults() >> [ ]
        DataAccessLayerResponse finalResponse
        underTest.client_.callProcedureAsync(_ as ProcedureCallback, _ as String, _ as Object[]) >> { cb, proc, obj ->
            cb.clientCallback(response)
        }
        underTest.query((r) -> {
            finalResponse = r
        }, "procedure", 1, "string")
        expect: finalResponse != null
        and:    finalResponse.getStatus() == DataAccessLayerStatus.SERVER_ERROR
    }

    def "Test async query negative 2"() {
        ClientResponse response = Mock(ClientResponse)
        response.getStatus() >> ClientResponse.OPERATIONAL_FAILURE
        response.getResults() >> [ ]
        DataAccessLayerResponse finalResponse
        underTest.client_.callProcedureAsync(_ as ProcedureCallback, _ as String, _ as Object[]) >> { cb, proc, obj ->
            throw new IOException("TEST")
        }
        underTest.query((r) -> {
            finalResponse = r
        }, "procedure", 1, "string")
        expect: finalResponse != null
        and:    finalResponse.getStatus() == DataAccessLayerStatus.IO_FAILURE
    }

    def "Test queryLong"() {
        ClientResponse response = Mock(ClientResponse)
        VoltTable table = new VoltTable(new VoltTable.ColumnInfo[] {
                new VoltTable.ColumnInfo("value", VoltType.BIGINT)
        })
        table.addRow(8_589_934_592L)
        response.getStatus() >> ClientResponse.SUCCESS
        response.getResults() >> [ table ]
        DataAccessLayerResponse finalResponse
        underTest.client_.callProcedureSync(_ as String, _ as Object[]) >> response
        expect: underTest.queryForLong("procedure", 1, "string") == 8_589_934_592L
    }
}
