// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import org.apache.logging.log4j.core.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Class for making a single node in a cluster the "Primary" node. This is done cooperatively.
 */
public class PrimarySecondaryServiceMonitor {

    /**
     * Create a monitor for deciding who in a cluster is has a Primary role and the rest are Secondary roles.
     *
     * @param monitoring The {@link NodeMonitoring} object that performs the basic messaging including which nodes
     *                  are alive and which are not.
     * @param handler The callback receiving the notification of you current role. This will happen on role change.
     */
    public PrimarySecondaryServiceMonitor(NodeMonitoring monitoring, PrimarySecondaryRoleChange handler) {
        if(monitoring == null)
            throw new IllegalArgumentException("The 'monitoring' parameter cannot be null!");
        if(handler == null)
            throw new IllegalArgumentException("The 'handler' parameter cannot be null!");
        if(!monitoring.isRunning())
            throw new RuntimeException("The underlying monitoring must be running before creating this class!");
        monitoring_ = monitoring;
        log_ = monitoring_.getLogger();
        monitoring_.addNodeStateChangeHandler(this::nodeStateChangeCallback);
        monitoring_.addOrReplaceMessageHandler(ANNOUNCE,this::announceHandler);
        me_ = monitoring_.getMyHostname();
        primary_ = me_;
        handler_ = handler;
        log_.debug("Greedily assuming I am the primary...");
        enableHandler(true);
        makePriority();
        evaluatePrimary(true);
        announce();
    }

    /**
     * Allow enabling and disabling of the Role callback. This is enabled in the constructor by default.
     *
     * @param enable true will enable role notification, false disables role notifications.
     */
    public void enableHandler(boolean enable) {
        if(enable)
            monitoring_.addOrReplaceMessageHandler(ANNOUNCE, this::announceHandler);
        else
            monitoring_.removeMessageHandler(ANNOUNCE);
        log_.info(String.format("%s Role Monitoring.", enable?"Enabled":"Disabled"));
    }

    private void makePriority() {
        map_.put(me_, rng_.nextInt(Integer.MAX_VALUE));
    }

    private void nodeStateChangeCallback(String node, RemoteNodeState newState, long lastSeen) {
        log_.debug(String.format("STATE CHANGE: %s: %s", newState, node));
        if(newState == RemoteNodeState.ACTIVE)
            announce();
        else {
            map_.put(node, 0);
            evaluatePrimary(false);
        }
    }

    private void announceHandler(Message message) {
        if(!message.getSender().equals(me_)) {
            int remotePriority = Integer.parseInt(message.getMessagePartsIterable().iterator().next());
            map_.put(message.getSender(), remotePriority);
            log_.debug(String.format("%s: %s=%d", ANNOUNCE, message.getSender(), remotePriority));
            boolean needToAnnounce = remotePriority == map_.get(me_);
            while(remotePriority == map_.get(me_))
                makePriority();
            if(needToAnnounce) {
                log_.debug("Detected priority collision, selecting new priority and re-announcing.");
                announce();
            }
            evaluatePrimary(false);
        }
    }

    private void announce() {
        Message msg = new  Message(ANNOUNCE, me_, "*", Long.toString(map_.get(me_)));
        monitoring_.sendMessage(msg);
        log_.debug(String.format("Sent %s message to everyone.", ANNOUNCE));
    }

    private void evaluatePrimary(boolean initial) {
        String candidate = me_;
        int candidateRank = map_.get(me_);
        for(Map.Entry<String,Integer> entry: map_.entrySet())
            if(entry.getValue() > candidateRank) {
                candidateRank = entry.getValue();
                candidate = entry.getKey();
            }
        log_.debug(String.format("Current primary='%s (%d)'; new candidate='%s (%d)'", primary_, map_.get(primary_),
                candidate, candidateRank));
        if(initial)
            handler_.stateChanged(PrimarySecondaryRole.Primary);
        else if(!primary_.equals(candidate)) {
            if(primary_.equals(me_))
                handler_.stateChanged(PrimarySecondaryRole.Secondary);
            if(candidate.equals(me_))
                handler_.stateChanged(PrimarySecondaryRole.Primary);
            primary_ = candidate;
        }
    }

    private final NodeMonitoring monitoring_;
    private final String me_;
    private final PrimarySecondaryRoleChange handler_;
    private final Logger log_;
    private final Map<String, Integer> map_ = new HashMap<>();
    private       Random rng_ = new Random(Instant.now().toEpochMilli()); // Not final for testing

    private String primary_;

    private static final String ANNOUNCE = "_ANNOUNCE_";
}
