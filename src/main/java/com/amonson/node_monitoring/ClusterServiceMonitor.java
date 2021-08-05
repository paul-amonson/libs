// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * Class to create a service node in a cluster. Each node has 1 of 2 roles, primary or secondary.
 */
public class ClusterServiceMonitor {
    /**
     * Construct the monitoring object that participates in a cluster.
     *
     * @param myHostname This nodes hostname.
     * @param clusterNodes All the hostnames in the cluster of services.
     * @param callback Callback that receives either a primary or secondary state. Only one primary can exist
     *                in the cluster at a time.
     * @param logger Th native Java Logger to log messages.
     */
    public ClusterServiceMonitor(String myHostname, String[] clusterNodes, RoleChange callback, Logger logger) {
        this(myHostname, Arrays.asList(clusterNodes), callback, logger);
    }

    /**
     * Construct the monitoring object that participates in a cluster.
     *
     * @param myHostname This nodes hostname.
     * @param clusterNodes All the hostnames in the cluster of services.
     * @param callback Callback that receives either a primary or secondary state. Only one primary can exist
     *                in the cluster at a time.
     * @param logger Th native Java Logger to log messages.
     */
    public ClusterServiceMonitor(String myHostname, List<String> clusterNodes, RoleChange callback, Logger logger) {
        me_ = myHostname;
        for(String node: clusterNodes)
            nodes_.put(node, 0);
        nodes_.put(me_, rng_.nextInt(Integer.MAX_VALUE));
        primary_ = me_;
        callback_ = callback;
        log_ = logger;
        monitoring_ = new NodeMonitoring(me_, nodes_.keySet(), this::callback, log_);
        evaluatePrimary(true);
    }

    /**
     * Set the port for communication. This will be bound to in the process. Must be called before starting the cluster.
     *
     * @param port Must be &gt;1024 and &lt;65536.
     */
    public void setPort(int port) {
        monitoring_.setPort(port);
    }

    /**
     * Join the cluster.
     *
     * @param blocking If set to true this method will block, if false the process will run in the background.
     */
    public void startMonitoring(boolean blocking) {
        if(!monitoring_.isRunning()) {
            monitoring_.addHandler("ANNOUNCE", this::announceCallback);
            monitoring_.startMonitoring();
            announce();
            if (blocking) {
                try {
                    monitoring_.waitForMonitoring();
                } catch (InterruptedException e) {
                    log_.fine("An expected interrupt occured in the monitoring. Monitoring halted.");
                }
            }
        }
    }

    /**
     * Leave the cluster.
     */
    public void stopMonitoring() {
        if(monitoring_.isRunning())
            monitoring_.stopMonitoring();
    }

    /**
     * Test if the node in the cluster.
     *
     * @return true if the process is part of the cluster; false if it's not.
     */
    public boolean isRunning() {
        return monitoring_.isRunning();
    }

    /**
     * Get the current role manually.
     *
     * @return This processes current role.
     */
    public Role getRole() {
        return role_;
    }

    /**
     * Enum for the 2 possible roles.
     */
    public enum Role { Primary, Secondary }

    /**
     * Defined callback for the Role changes as the cluster changes.
     */
    @FunctionalInterface public interface RoleChange {
        /**
         * Called when the Role for this node changes.
         *
         * @param role The new Role.
         */
        void stateChanged(Role role);
    }

    synchronized private void announce() {
        monitoring_.sendMessage("ANNOUNCE", me_ + ":" + nodes_.get(me_));
    }

    private void callback(String node, boolean newState) {
        if(newState)
            announce();
        else {
            nodes_.put(node, 0);
            evaluatePrimary(false);
        }
    }

    private void announceCallback(String message) {
        String[] parts = message.split(":");
        int rank = Integer.parseInt(parts[1]);
        nodes_.put(parts[0], rank);
        if(rank == nodes_.get(me_)) {
            nodes_.put(me_, rng_.nextInt(Integer.MAX_VALUE));
            announce();
        }
        evaluatePrimary(false);
    }

    private void evaluatePrimary(boolean initial) {
        String candidate = me_;
        int candidateRank = nodes_.get(me_);
        for(Map.Entry<String,Integer> entry: nodes_.entrySet())
            if(entry.getValue() > candidateRank) {
                candidateRank = entry.getValue();
                candidate = entry.getKey();
            }
        if(initial)
            callCallback(Role.Primary);
        else if(!primary_.equals(candidate)) {
            if(primary_.equals(me_))
                callCallback(Role.Secondary);
            if(candidate.equals(me_))
                callCallback(Role.Primary);
            primary_ = candidate;
        }
    }

    private void callCallback(Role role) {
        role_ = role;
        if(callback_ != null)
            callback_.stateChanged(role);
    }

    private final Map<String, Integer> nodes_ = new HashMap<>();
    private final String me_;
    private final Logger log_;
    private final Random rng_ = new Random(Instant.now().toEpochMilli());
    private final RoleChange callback_;
    private       NodeMonitoring monitoring_;
    private       String primary_;
    private       Role role_;
}
