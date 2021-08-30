// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

import org.voltdb.client.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
 *      resource_file   - (Optional) Resource file to load into VoltDB.
 *      filename        - (Optional) Filename in file system to load into VoltDB.
 *      jar_files       - (Optional) Comma separated list of JAR files to load into VoltDB (Must be Java 8 JARs)
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

    /**
     * Using the configuration properties "resource_file", "filename", and "jar_files". initialize the schema and
     * stored Java procedures (JARs). Resource files are attempted before file in the filesystem. Loading JARs is
     * last.
     *
     * @return true if everything present is loaded ok, false something was attempted to load and failed.
     */
    public boolean initializeVoltDBAfterConnect() {
        if(!checkSchema()) {
            if (properties_.containsKey(RESOURCE_FILE)) {
                if (!loadSQLFromResource(properties_.getProperty(RESOURCE_FILE)))
                    return false;
            }
            if (properties_.containsKey(FILENAME)) {
                if (!loadSQLFromFile(properties_.getProperty(FILENAME)))
                    return false;
            }
            if (properties_.containsKey(JAR_FILES)) {
                return loadJarFiles(properties_.getProperty(JAR_FILES));
            }
        }
        return true;
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

    private boolean checkSchema() {
        try {
            ClientResponse response = client_.callProcedure("@SystemCatalog", "tables");
            if(response.getStatus() != ClientResponse.SUCCESS) {
                log_.warning("Call to '@SystemCatalog' built-in procedure failed with: " +
                        response.getStatusString());
                return false;
            }
            return response.getResults().length > 0;
        } catch (NoConnectionsException e) {
            log_.warning("No connection to VoltDB databases!");
            log_.throwing(getClass().getCanonicalName(), "checkSchema", e);
            return false;
        } catch (IOException e) {
            log_.warning("I/O exception when calling to VoltDB databases!");
            log_.throwing(getClass().getCanonicalName(), "checkSchema", e);
            return false;
        } catch (ProcCallException e) {
            log_.warning("Call to built in '@SystemCatalog' failed! Is the name correct?");
            log_.throwing(getClass().getCanonicalName(), "checkSchema", e);
            return false;
        }
    }

    private boolean loadJarFiles(String jarFiles) {
        String[] jars = jarFiles.split(",");
        for(String jarFilename: jars) {
            File jar = new File(jarFilename);
            if(!jar.canRead()) {
                log_.severe(String.format("Jar file '%s' is missing or cannot be read!", jar));
                return false;
            }
            try {
                ClientResponse response = client_.updateClasses(jar, "");
                if(response.getStatus() != ClientResponse.SUCCESS) {
                    log_.severe(String.format("Failed to call VoltDB: %s", response.getStatusString()));
                    return false;
                }
            } catch (IOException | ProcCallException e) {
                log_.severe(String.format("Failed to load the JAR file '%s' into VoltDB!", jar));
                log_.throwing(getClass().getCanonicalName(), "loadJarFiles", e);
                return false;
            }
        }
        return true;
    }

    private boolean loadSQLFromFile(String filename) {
        File file = new File(filename);
        if(!file.canRead()) {
            log_.severe(String.format("File '%s' does not exist or is not readable!", file));
            return false;
        }
        String sql;
        try {
            sql = new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            log_.severe(String.format("Failed to read SQL file '%s'", file));
            log_.throwing(getClass().getCanonicalName(), "loadSQLFromFile", e);
            return false;
        }
        try {
            ClientResponse response = callProcedureSync("@AdHoc", sql);
            if(response.getStatus() != ClientResponse.SUCCESS) {
                log_.severe(String.format("Failed to call VoltDB: %s", response.getStatusString()));
                return false;
            }
        } catch(IOException | ProcCallException e) {
            log_.severe(String.format("Failed to load the SQL file '%s' into VoltDB!", file));
            log_.throwing(getClass().getCanonicalName(), "loadSQLFromFile", e);
            return false;
        }
        return true;
    }

    private boolean loadSQLFromResource(String resourceName) {
        InputStream stream = ClassLoader.getSystemResourceAsStream(resourceName);
        if(stream == null) {
            log_.severe(String.format("ClassLoader failed to get resource stream for '%s'!", resourceName));
            return false;
        }

        String sql;
        try {
            sql = IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log_.severe(String.format("ClassLoader failed to get read the stream for '%s'!", resourceName));
            log_.throwing(getClass().getCanonicalName(), "loadSQLFromResource", e);
            return false;
        }
        try {
            ClientResponse response = callProcedureSync("@AdHoc", sql);
            if(response.getStatus() != ClientResponse.SUCCESS) {
                log_.severe(String.format("Failed to call VoltDB: %s", response.getStatusString()));
                return false;
            }
        } catch(IOException | ProcCallException e) {
            log_.severe(String.format("Failed to load the resource SQL '%s' into VoltDB!", resourceName));
            log_.throwing(getClass().getCanonicalName(), "loadSQLFromResource", e);
            return false;
        }
        return true;
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
    public static final String RESOURCE_FILE             = "resource_file";
    public static final String FILENAME                  = "filename";
    public static final String JAR_FILES                 = "jar_files";
}
