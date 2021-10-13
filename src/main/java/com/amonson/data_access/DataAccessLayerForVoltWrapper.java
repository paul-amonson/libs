// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

import com.amonson.prop_store.PropList;
import com.amonson.prop_store.PropMap;
import org.apache.logging.log4j.core.Logger;
import org.voltdb.VoltTable;
import org.voltdb.VoltType;
import org.voltdb.client.ClientResponse;
import org.voltdb.client.ProcCallException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * <p>VoltDB Implementation of DataAccessLayer. Use the implementation string "voltdb" in the {@link DataAccessLayerFactory}.</p>
 * <blockquote><table style="width: 70%; border-collapse: collapse; border: 2pt solid black">
 *  <caption style="font-weight: bold">Properties Used in This Implementation</caption>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">list_of_servers</td><td style="padding: 3pt; border: 1pt solid black"><i>(Required; def = "")</i> Required comma separated list of VoltDB servers.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">username</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional; def = "")</i> Username or empty string for credentials.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">password</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional; def = "")</i> Password or empty string for credentials.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">port</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional; def = "21212")</i> Port for VoltDB servers.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">retry_delay</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional; def = "2000")</i> Delay between retries to a connection in milliseconds.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">resource_file</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional)</i> Resource file to load into VoltDB.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">filename</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional)</i> Filename in file system to load into VoltDB.</td></tr>
 *  <tr style="background: #e0e0e0"><td style="padding: 3pt; border: 1pt solid black; width: 120pts">jar_files</td><td style="padding: 3pt; border: 1pt solid black"><i>(Optional)</i> Comma separated list of JAR files to load into VoltDB (Must be Java 8 JARs)</td></tr>
 * </table></blockquote>
 */
public class DataAccessLayerForVoltWrapper extends DataAccessLayer {
    public DataAccessLayerForVoltWrapper(Properties configurationValues, Logger logger) {
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
            log_.catching(e);
            return new DataAccessLayerResponse(DataAccessLayerStatus.IO_FAILURE, null);
        } catch (ProcCallException e) {
            log_.catching(e);
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
            log_.catching(e);
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
     * Populate nay schemas, stored procedures, etc... use by the implementation.
     *
     * @return true on success, false on failure.
     */
    @Override
    public boolean initializeAfterConnect() {
        return client_.initializeVoltDBAfterConnect();
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
                        log_.catching(e);
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
