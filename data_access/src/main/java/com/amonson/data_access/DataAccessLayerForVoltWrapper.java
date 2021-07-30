// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

import com.amonson.prop_store.PropList;
import com.amonson.prop_store.PropMap;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * VoltDB Implementation of DataAccessLayer.
 */
public class DataAccessLayerForVoltWrapper extends DataAccessLayer {
    DataAccessLayerForVoltWrapper(Properties configurationValues, Logger logger) {
        super(configurationValues, logger);
        client_ = new VoltWrapperClient(configurationValues, logger);
    }

    /**
     * Perform a synchronous query of the DataAccessLayer implementation.
     *
     * @param name   Procedure name for the query.
     * @param params Parameters for the query.
     * @return The DataAccessLayerResponse for the attempted query.
     */
    @Override
    public DataAccessLayerResponse query(String name, Object... params) {
        try {
            ClientResponse response = client_.callProcedureSync(name, params);
            return convertClientResponse(response);
        } catch (IOException e) {
            log_.throwing(this.getClass().getCanonicalName(), "query", e);
            return new DataAccessLayerResponse(DataAccessLayerStatus.IO_FAILURE, null);
        } catch (ProcCallException e) {
            log_.throwing(this.getClass().getCanonicalName(), "query", e);
            return new DataAccessLayerResponse(DataAccessLayerStatus.PROCEDURE_FAILURE, null);
        }
    }

    /**
     * Perform an asynchronous query of the DataAccessLayer implementation.
     *
     * @param callback The callback for asynchronous completion of the query.
     * @param name     Procedure name for the query.
     * @param params   Parameters for the query.
     */
    @Override
    public void query(DataAccessLayerCallback callback, String name, Object... params) {
        assert callback != null:"The 'callback' parameter cannot be null!";
        try {
            client_.callProcedureAsync((response) -> clientCallback(response, callback), name, params);
        } catch (IOException e) {
            log_.throwing(this.getClass().getCanonicalName(), "query", e);
            callback.callback(new DataAccessLayerResponse(DataAccessLayerStatus.IO_FAILURE, null));
        }
    }

    /**
     * Perform a synchronous query of the DataAccessLayer implementation expecting a single long response.
     *
     * @param name   Procedure name for the query.
     * @param params Parameters for the query.
     * @return The expected long for the attempted query.
     * @throws DataAccessLayerException if the single response was not a single long.
     */
    @Override
    public long queryForLong(String name, Object... params) throws DataAccessLayerException {
        try {
            ClientResponse response = client_.callProcedureSync(name, params);
            if(response.getStatus() != ClientResponse.SUCCESS || response.getResults().length != 1)
                throw new DataAccessLayerException(DataAccessLayerStatus.SERVER_ERROR,
                        "Failed response, no results, or more than one result!");
            response.getResults()[0].advanceRow();
            if(response.getResults()[0].getColumnCount() != 1)
                throw new DataAccessLayerException(DataAccessLayerStatus.SERVER_ERROR,
                        "Failed response more than one column returned!");
            return response.getResults()[0].asScalarLong();
        } catch(IOException e) {
            throw new DataAccessLayerException(DataAccessLayerStatus.SERVER_ERROR);
        } catch (ProcCallException e) {
            throw new DataAccessLayerException(DataAccessLayerStatus.PROCEDURE_FAILURE);
        }
    }

    /**
     * Connect the object to the data source(s).
     */
    @Override
    public void connect() {
        client_.connect();
    }

    /**
     * Disconnect the object from the data source(s).
     */
    @Override
    public void disconnect() {
        client_.disconnect();
    }

    /**
     * Does the object have a connection?
     *
     * @return true if at least one connection is present; false otherwise.
     */
    @Override
    public boolean isConnected() {
        return client_.haveConnection();
    }

    private void clientCallback(ClientResponse clientResponse, DataAccessLayerCallback callback) throws Exception {
        byte status = clientResponse.getStatus();
        if(status != ClientResponse.SUCCESS) {
            DataAccessLayerStatus das = connectionErrors_.contains(status) ? DataAccessLayerStatus.NO_CONNECTION :
                    DataAccessLayerStatus.SERVER_ERROR;
            callback.callback(new DataAccessLayerResponse(das, null));
        } else
            callback.callback(convertClientResponse(clientResponse));
    }

    private DataAccessLayerResponse convertClientResponse(ClientResponse clientResponse) {
        if(clientResponse.getStatus() != ClientResponse.SUCCESS)
            return new DataAccessLayerResponse(DataAccessLayerStatus.SERVER_ERROR, null);
        PropList results = new PropList();
        for (int resultIndex = 0; resultIndex < clientResponse.getResults().length; resultIndex++) {
            PropList result = new PropList();
            VoltTable table = clientResponse.getResults()[resultIndex];
            for (int rowIndex = 0; rowIndex < clientResponse.getResults()[resultIndex].getRowCount(); rowIndex++) {
                table.advanceRow();
                PropMap row = new PropMap();
                for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
                    String name = table.getColumnName(columnIndex);
                    try {
                        Object value = voltTypeToType(table, columnIndex);
                        row.put(name, value);
                    } catch(UnsupportedOperationException e) {
                        log_.warning(String.format("Unsupported type '%s' was converted to a 'null' value for " +
                                        "column '%s'!", table.getColumnType(columnIndex), name));
                        row.put(name, null);
                    }
                }
                result.add(row);
            }
            results.add(result);
        }
        return new DataAccessLayerResponse(DataAccessLayerStatus.SUCCESS, results);
    }

    private Object voltTypeToType(VoltTable table, int columnIndex) {
        VoltType type = table.getColumnType(columnIndex);
        switch(type) {
            case STRING:
                return table.getString(columnIndex);
            case DECIMAL:
                return table.getDecimalAsBigDecimal(columnIndex);
            case BIGINT:
            case SMALLINT:
            case INTEGER:
            case TINYINT:
                return table.getLong(columnIndex);
            case FLOAT:
                return table.getDouble(columnIndex);
            case TIMESTAMP:
                return table.getTimestampAsLong(columnIndex);
            default:
                throw new UnsupportedOperationException(String.format("VoltType '%s' is not supported by the " +
                        "implementation of the DataAccessLayer!", type));
        }
    }

    private VoltWrapperClient client_;
    private final static List<Byte> connectionErrors_ = Arrays.asList(ClientResponse.SERVER_UNAVAILABLE,
            ClientResponse.CONNECTION_LOST, ClientResponse.CONNECTION_TIMEOUT);
}
