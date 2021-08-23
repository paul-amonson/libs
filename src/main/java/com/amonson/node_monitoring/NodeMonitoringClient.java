// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

/**
 * NodeMonitoringClient talks to the NodeMonitoring implementation
 */
public interface NodeMonitoringClient {
    boolean sendMessage(Message message);
    String getMyHostname();
    void close() throws Exception;
}
