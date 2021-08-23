// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

/**
 * A class implementation that allows a cluster (one per node) of applications to monitor each other and send
 * one-way messages to nodes in the cluster from other processes on nodes in the cluster (localhost access only
 * using the included client called {@link com.amonson.node_monitoring.NodeMonitoringClient}). Use the factory to
 * create either the monitoring service or the client.
 */
package com.amonson.node_monitoring;
