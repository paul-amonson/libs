// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import java.util.List;
import java.util.logging.Logger;

/**
 * This interface is designed to handle one way message passing to multiple nodes in a multi-node cluster of nodes
 * including a heartbeat feature.
 *
 * Implementations of this interface must have a constructor with the following signature:
 *
 *      &lt;class_name&gt;(String myHostname,
 *                         List<String> allHostnames,
 *                         int port,
 *                         NodeStateChangeHandler nodeStateChangeHandler,
 *                         Logger logger,
 *                         Properties config);
 *
 * See {@link NodeMonitoringFactory} for a description of each parameter.
 */
public interface NodeMonitoring {
    /**
     * A thread safe method to send to one or more nodes on the cluster "network".
     *
     * @param message The message object to send as multiple ZeroMQ frames. No message is sent if this node is
     *               not already listening for messages.
     */
    void sendMessage(Message message);

    /**
     * Start listening and allowing sending. If called multiple times subsequent calls are ignored silently.
     *
     * @param blocking This method will block if true is passed, otherwise a new thread will be created.
     */
    void start(boolean blocking);

    /**
     * Stop listening and sending, Is ignores silently if the class is not listening.
     */
    void stop();

    /**
     * If start is called with a false value for blocking then use this to block until the new thread stops.
     * It will exit immediately if true was used in start.
     */
    void waitForExitWhenOnThread();

    /**
     * Get if the monitoring is running.
     * @return true is the monitoring is running; false otherwise.
     */
    boolean isRunning();

    /**
     * Get the logger.
     *
     * @return The logger used by node monitoring.
     */
    Logger getLogger();

    /**
     * Add or replace an existing topic message handler.
     *
     * @param topic The topic or key to register for handling. and non-null string with no \0's in the string is valid.
     * @param handler The callback for an incoming message with the specified topic. This will be called on a thread to
     *                protect the communication loop from blocking callbacks. The handler callback should not block for
     *                extended periods in any case.
     * @return The old handler or null if there is no previous handler.
     * @throws AssertionError If either of the passed arguments are null.
     */
    MessageHandler addOrReplaceMessageHandler(String topic, MessageHandler handler);

    /**
     * Add a new topic message handler. Will not save the handler is one already exists.
     *
     * @param topic The topic or key to register for handling. and non-null string with no \0's in the string is valid.
     * @param handler The callback for an incoming message with the specified topic. This will be called on a thread to
     *                protect the communication loop from blocking callbacks. The handler callback should not block for
     *                extended periods in any case.
     * @throws AssertionError If either of the passed arguments are null.
     */
    void addMessageHandler(String topic, MessageHandler handler);

    /**
     * Remove a handler for the passed topic.
     *
     * @param topic The topic to remove the handler for.
     * @return The removed handler if one existed or null of the topic has no handler.
     * @throws AssertionError If the topic is null.
     */
    MessageHandler removeMessageHandler(String topic);

    /**
     * Add a new handler for node state change notifications.
     *
     * @param newHandler The new handler to add.
     * @return See semantics of {@link List} add() method.
     */
    boolean addNodeStateChangeHandler(NodeStateChangeHandler newHandler);

    /**
     * Remove a handler from node state change notifications.
     *
     * @param newHandler The handler to remove.
     * @return See semantics of {@link List} remove() method.
     */
    boolean removeNodeStateChangeHandler(NodeStateChangeHandler newHandler);

    /**
     * Get the hostname from construction.
     *
     * @return This nodes hostname.
     */
    String getMyHostname();
}
