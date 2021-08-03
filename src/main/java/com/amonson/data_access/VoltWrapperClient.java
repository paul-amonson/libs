// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

import org.voltdb.client.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Class to wrap a VoltDB Client class. It controls connections to multiple servers configured as a cluster.
 * Properties Possible:
 *      list_of_servers - (Required; def = "") Required comma separated list of VoltDB servers.
 *      username        - (Optional; def = "") Username or empty string for credentials.
 *      password        - (Optional; def = "") Password or empty string for credentials.
 *      port            - (Optional; def = "21212") Port for VoltDB servers.
 *      retry_delay     - (Optional; def = "2000") Delay between retries to a connection in milliseconds.
 */
class VoltWrapperClient extends ClientStatusListenerExt {
    /**
     * Constructs the object and internal client but does not connect.
     *
     * @param properties Properties to configure the internal client.
     * @param logger This is where logging messages go.
     */
    VoltWrapperClient(Properties properties, Logger logger) {
        assert properties != null && properties.containsKey(SERVERS);
        assert logger != null;
        log_ = logger;
        port_ = Integer.parseInt(properties.getProperty(PORT, Integer.toString(Client.VOLTDB_SERVER_PORT)));
        for(String server: properties.getProperty(SERVERS).split(","))
            connections_.put(server, false);
        properties_ = properties;
        client_ = ClientFactory.createClient(initClient());
    }

    /**
     * Starts a connection thread that will attempt keep connections to the hosts.
     */
    public void connect() {
        if(connectionThread_ != null)
            return;
        connectionThread_ = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                for (Map.Entry<String, Boolean> entry : connections_.entrySet())
                    if (!entry.getValue())
                        entry.setValue(connectInternal(entry.getKey()));
                try {
                    Thread.sleep(Long.parseLong(properties_.getProperty(CONNECTION_RETRY_DELAY_MS,
                            Long.toString(2_000L))));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        connectionThread_.start();
    }

    /**
     * Stops the connection thread that was attempting to keep connections to the hosts. Object cannot be reused
     * after disconnect is called on a connected client.
     */
    public void disconnect() {
        if(connectionThread_ == null)
            return;
        connectionThread_.interrupt();
        try {
            connectionThread_.join();
        } catch(InterruptedException e) { /* Ignore because we are closing anyway. */ }
        connectionThread_ = null;
        try {
            client_.drain();
        } catch(NoConnectionsException | InterruptedException e) { /* No connection then drain() is not necessary */ }
        try {
            client_.close();
        } catch(InterruptedException e) { /* No connection then drain() is not necessary */ }
    }

    /**
     * <p>Synchronously invoke a procedure. Blocks until a result is available. A {@link ProcCallException}
     * is thrown if the response is anything other then success.</p>
     *
     * @param procedureName <code>class</code> name (not qualified by package) of the procedure to execute.
     * @param params vararg list of procedure's parameter values.
     * @return {@link ClientResponse} instance of procedure call results.
     * @throws ProcCallException on any VoltDB specific failure.
     * @throws NoConnectionsException if this {@link Client} instance is not connected to any servers.
     * @throws IOException if there is a Java network or connection problem.
     */
    public ClientResponse callProcedureSync(String procedureName, Object... params)
            throws IOException, ProcCallException {
        return client_.callProcedure(procedureName, params);
    }

    /**
     * <p>Asynchronously invoke a replicated procedure, by providing a callback that will be invoked by the single
     * thread backing the client instance when the procedure invocation receives a response.
     * See the {@link Client} class documentation for information on the negative performance impact of slow or
     * blocking callbacks. If there is backpressure
     * this call will block until the invocation is queued. If configureBlocking(false) is invoked
     * then it will return immediately. Check the return value to determine if queueing actually took place.</p>
     *
     * @param callback {@link ProcedureCallback} that will be invoked with procedure results.
     * @param procedureName class name (not qualified by package) of the procedure to execute.
     * @param params vararg list of procedure's parameter values.
     * @throws NoConnectionsException if this {@link Client} instance is not connected to any servers.
     * @throws IOException if there is a Java network or connection problem.
     */
    public void callProcedureAsync(ProcedureCallback callback, String procedureName, Object... params)
            throws IOException {
        client_.callProcedure(callback, procedureName, params);
    }

    /**
     * Checks for any connection to the VoltDB cluster.
     *
     * @return <code>true</code> if there is at least one connection; <code>false</code> if there are no connections.
     */
    public boolean haveConnection() {
        return client_.getConnectedHostList().size() > 0;
    }

    /**
     * Waits for any connection to the VoltDB cluster with a timeout.
     * @param timeoutMilliseconds How many milliseconds to wait for a connection.
     * @return <code>true</code> if a connection was made within the timeout period or already existed,
     * <code>false</code> if no connection during timeout period.
     */
    public boolean waitForConnection(long timeoutMilliseconds) {
        long now = Instant.now().toEpochMilli();
        long futureTimeout = now + timeoutMilliseconds;
        while(!haveConnection() && now < futureTimeout) { // Wait for at least one connection
            try {
                Thread.sleep(100); // Busy wait intentional.
            } catch(InterruptedException e) { /* Ignore this, should not happen */ }
            now = Instant.now().toEpochMilli();
        }
        return now < futureTimeout;
    }

    @Override
    public void connectionLost(String hostname, int port, int connectionsLeft, DisconnectCause cause) {
        connections_.put(hostname, false);
        if(connectionsLeft == 0)
            log_.severe("All connections to the VoltDB servers have been lost.");
        else
            log_.warning(String.format("Connection to '%s' was disconnected for reason '%s', there are %d " +
                            "connections left.", hostname, cause.toString(), connectionsLeft));
    }

    @Override
    public void connectionCreated(String hostname, int port, AutoConnectionStatus status) {
        if(status == AutoConnectionStatus.SUCCESS && !connections_.containsKey(hostname))
            connections_.put(hostname, true);
        else
            connections_.put(hostname, false);
    }

    private boolean connectInternal(String server) {
        try {
            client_.createConnection(server, port_);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private ClientConfig initClient() {
        ClientConfig config;
        config = new ClientConfig(properties_.getProperty(USERNAME, ""), properties_.getProperty(PASSWORD, ""), this);
        config.enableAutoTune();
        config.setReconnectOnConnectionLoss(true);
        config.setClientAffinity(true);
        config.setTopologyChangeAware(true);
        return config;
    }

    private final Logger              log_;
    private final Properties          properties_;
    private final Map<String,Boolean> connections_ = new ConcurrentHashMap<>();
    private       Client              client_;
    private final int                 port_;
    private       Thread              connectionThread_ = null;

    public static final String USERNAME                  = "username";
    public static final String PASSWORD                  = "password";
    public static final String PORT                      = "port";
    public static final String CONNECTION_RETRY_DELAY_MS = "retry_delay";
    public static final String SERVERS                   = "list_of_servers";
}
