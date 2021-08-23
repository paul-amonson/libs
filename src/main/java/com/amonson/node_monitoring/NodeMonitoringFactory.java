// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Factory to create a message-based (network) node monitoring class using a heartbeat. The implementation independent
 * configuration properties are:
 *
 *      NodeExpirationTime - (def. 4000ms) After a node has no heartbeat for this time in ms, the node is
 *                           marked as missing.
 *      NodeAlivePeriod    - (def. 1900ms) This is the period of the heartbeat sent out by this node.
 *      ThreadsPoolCount   - (def. 3) This is the number of threads in the pool used by user callbacks.
 *
 * See the implementation class documentation for specific implementation specific configuration.
 *
 * Current implementations are:
 *      "zeromq" - A JeroMQ-based implementation.
 */
public class NodeMonitoringFactory {
    /**
     * Create or get the specific implementation. There can only be one per process and only one per host using
     * the port and port + 1 network ports.
     *
     * @param implementation The implementation to create, if a subsequent call in a process attempts a
     *                       new implementation it will only receive the original implementation.
     * @param myHostname This needs to be the network resolvable hostname of this node.
     * @param allHostnames This is a list of all hostnames in the cluster. It may contain "myHostname". All names
     *                    must be network resolvable. This may be null denoting a cluster of only this node.
     * @param port The base port used for node to node communication. The next numeric port (port + 1) is used
     *             for another process on the same node to communicate with this class use a REQ socket for this.
     *             Privileged ports are not allowed (ports &lt; 1024) .
     * @param nodeStateChangeHandler The callback which is called when a node becomes alive or is deemed missing.
     * @param logger The logger used for logging in this class.
     * @return The newly created instance or the previously created instance.
     * @throws IllegalArgumentException if any required arguments passed are null. These are: "myHostname" and
     *                                  "logger".
     */
    public static NodeMonitoring createSingletonInstance(String implementation, String myHostname,
                                                         List<String> allHostnames, int port,
                                                         NodeStateChangeHandler nodeStateChangeHandler, Logger logger,
                                                         Properties config) {
        if(implementation.equals("zeromq")) {
            if(singleton_ == null)
                singleton_ = new NodeMonitoringZeroMQ(myHostname, allHostnames, port, nodeStateChangeHandler, logger, config);
            else
                logger.warning("Returning an already created instance, the instance will not represent " +
                        "the new passed parameters!");
            return singleton_;
        } else
            throw new IllegalArgumentException("Unknown implementation name: " + implementation);
    }

    /**
     * Based on the implementation name for the {@link NodeMonitoring} cluster implementation, you can use the
     * same name to create a standalone client to send messages to localhost at port+1 to the cluster. In other words
     * specify the same port and implementation name in both server and client side and the client will talk to the
     * server at localhost.
     *
     * @param implementation The implementation to create, which must use the same implementation name as the
     *                      {@link NodeMonitoring} implementation.
     * @param myHostname This needs to be the network resolvable hostname of this node.
     * @param allHostnames This is a list of all hostnames in the cluster. It may contain "myHostname". All names
     *                    must be network resolvable. This may be null denoting a cluster of only this node.
     * @param port The base port used for node to node communication. The next numeric port (port+1) is used
     *             for another process using the NodeMonitoringClient on the same node to communicate with the server.
     *             Privileged ports are not allowed (ports &tl; 1024) .
     * @param logger The logger used for logging in this class.
     * @return The newly created instance of the {@link NodeMonitoringClient} to talk to the {@link NodeMonitoring}
     * cluster but only on a node with the server running.
     */
    public static NodeMonitoringClient createClient(String implementation, String myHostname,
                                                    List<String> allHostnames, int port, Logger logger) {
        if(implementation.equals("zeromq")) {
            return new NodeMonitoringClientZeroMQ(myHostname, allHostnames, port, logger);
        } else
            throw new IllegalArgumentException("Unknown implementation name: " + implementation);
    }

    private NodeMonitoringFactory() {}

    private static NodeMonitoring singleton_ = null;
}
