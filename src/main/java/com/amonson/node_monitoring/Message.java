// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.node_monitoring;

import java.util.*;

/**
 * Message class for messages handled by the {@link NodeMonitoringZeroMQ} class.
 */
public class Message {
    /**
     * Create a Message instance with topic, sender, target(s) and optional payload parts.
     *
     * @param topic The topic used to send the message. If the topic is not registered by peers the message
     *             will be lost.
     * @param sender This is your hostname, network resolvable. Use of non-resolvable names here will result
     *              in lost messages.
     * @param targets The list of targets to send the message. The collection cannot be null or empty.
     * @param messageParts The list of message parts of the payload. It is acceptable to not have any payload parts.
     * @throws IllegalArgumentException if topic and sender are null or empty, also if targets is null or length 0.
     */
    public Message(String topic, String sender, Iterable<String> targets, String... messageParts) {
        this(topic, sender, targets, Arrays.asList(messageParts));
    }

    /**
     * Create a Message instance with topic, sender, target(s) and optional payload parts.
     *
     * @param topic The topic used to send the message. If the topic is not registered by peers the message
     *             will be lost.
     * @param sender This is your hostname, network resolvable. Use of non-resolvable names here will result
     *              in lost messages.
     * @param targets The list of targets to send the message. The string is a comma separated list with no spaces.
     * @param messageParts The list of message parts of the payload. It is acceptable to not have any payload parts.
     * @throws IllegalArgumentException if topic, sender or targets are null or empty.
     */
    public Message(String topic, String sender, String targets, String... messageParts) {
        this(topic, sender, targets, Arrays.asList(messageParts));
    }

    /**
     * Create a Message instance with topic, sender, target(s) and optional payload parts.
     *
     * @param topic The topic used to send the message. If the topic is not registered by peers the message
     *             will be lost.
     * @param sender This is your hostname, network resolvable. Use of non-resolvable names here will result
     *              in lost messages.
     * @param targets The list of targets to send the message. The string is a comma separated list with no spaces.
     * @param messageParts The list of message parts of the payload. It is acceptable to use 0 parts in the collection.
     * @throws IllegalArgumentException if topic, sender or targets are null or empty.
     */
    public Message(String topic, String sender, String targets, Iterable<String> messageParts) {
        this(topic, sender);
        if(targets == null || targets.isBlank())
            throw new IllegalArgumentException("The 'targets' cannot be null or empty!");
        targets_.addAll(Arrays.asList(targets.split(",")));
        if(messageParts != null)
            for(String part: messageParts)
                frames_.add(part);
    }

    /**
     * Create a Message instance with topic, sender, target(s) and optional payload parts.
     *
     * @param topic The topic used to send the message. If the topic is not registered by peers the message
     *             will be lost.
     * @param sender This is your hostname, network resolvable. Use of non-resolvable names here will result
     *              in lost messages.
     * @param targets The list of targets to send the message. The collection cannot be null or empty.
     * @param messageParts The list of message parts of the payload. It is acceptable to use 0 parts in the collection.
     * @throws IllegalArgumentException if topic and sender are null or empty, also if targets is null or length 0.
     */
    public Message(String topic, String sender, Iterable<String> targets, Iterable<String> messageParts) {
        this(topic, sender);
        if(targets == null)
            throw new IllegalArgumentException("The 'targets' cannot be null!");
        for(String target: targets)
            targets_.add(target);
        if(targets_.size() == 0)
            throw new IllegalArgumentException("The 'targets' cannot be empty!");
        if(messageParts != null)
            for(String part: messageParts)
                frames_.add(part);
    }

    /**
     * Used by package local client to create a Message with not targets or message parts.
     *
     * @param topic The topic used to send the message. If the topic is not registered by peers the message
     *             will be lost.
     * @param sender This is your hostname, network resolvable. Use of non-resolvable names here will result
     *              in lost messages.
     * @throws IllegalArgumentException if topic and sender are null or empty, also if targets is null or length 0.
     */
    Message(String topic, String sender) {
        if(topic == null || topic.isBlank())
            throw new IllegalArgumentException("The 'topic' cannot be null or empty!");
        if(sender == null || sender.isBlank())
            throw new IllegalArgumentException("The 'from' cannot be null or empty!");
        topic_ = topic;
        sender_ = sender;
    }

    /**
     * Check if the hostname node passed is in the targets list of this instance.
     *
     * @param node The hostname to check for inclusion in the targets list.
     * @return true indicates the passed node is present in the targets list, false otherwise.
     */
    public boolean targetsContains(String node) { return targets_.contains(node); }

    /**
     * Get the targets list as a string.
     *
     * @return The comma separated list of targets in this instance.
     */
    public String getTargetsAsString() {
        if(targets_.size() == 1)
            return targets_.iterator().next();
        else
            return String.join(",", targets_);
    }

    /**
     * Functional interface for callback forEachTargetDo() target name in this instance.
     */
    @FunctionalInterface public interface FECallback {
        /**
         * Internal call to callback.
         *
         * @param target The target the callback is called for.
         * @param arg The arg passed to the forEachTargetDo() method.
         */
        void call(String target, Object arg);
    }

    /**
     * Loop over targets calling a callback.
     *
     * @param action The callback called for each target in this instance.
     */
    public void forEachTargetDo(FECallback action, Object arg) {
        for(String target: targets_)
            action.call(target, arg);
    }

    /**
     * Get the message payload parts as a String[].
     *
     * @return The String[] of all payload message parts.
     */
    public String[] getMessageParts() {
        String[] results = new String[frames_.size()];
        return frames_.toArray(results);
    }

    /**
     * Get the payload message parts as a string iterable.
     *
     * @return The iterable for the list of payload message parts.
     */
    public Iterable<String> getMessagePartsIterable() {
        return frames_;
    }

    /**
     * Add one or more targets to the targets list as a collection.
     *
     * @param newTargets The collection of new targets.
     */
    public void addTargets(Collection<String> newTargets) { targets_.addAll(newTargets); }

    /**
     * Add one or more targets to the targets list as a String[].
     *
     * @param newTargets The String[] of new targets.
     */
    public void addTargets(String... newTargets) { Collections.addAll(targets_, newTargets); }

    /**
     * Replace targets with one or more targets to the targets list as a collection.
     *
     * @param newTargets The collection of new targets.
     */
    public void replaceTargets(Collection<String> newTargets) { targets_.clear(); addTargets(newTargets); }

    /**
     * Replace targets with one or more targets to the targets list as a String[].
     *
     * @param newTargets The String[] of new targets.
     */
    public void replaceTargets(String... newTargets) { targets_.clear(); addTargets(newTargets); }

    /**
     * Add one or more parts to the payload message parts list as a String[].
     *
     * @param newParts The String[] of new targets.
     */
    public void addMessageParts(String... newParts) { frames_.addAll(Arrays.asList(newParts)); }

    /**
     * Add one or more parts to the payload message parts list as a collection.
     *
     * @param newParts The collection of new targets.
     */
    public void addMessageParts(Collection<String> newParts) { frames_.addAll(newParts); }

    /**
     * Get the message's topic.
     *
     * @return The topic passed in the ctor()
     */
    public String getTopic() { return topic_; }

    /**
     * Get the message's sender.
     *
     * @return The sender passed in the ctor()
     */
    public String getSender() { return sender_; }

    /**
     * Convert the entire message to a string for display. NOTE: do not use in tight loops as this is not efficient.
     *
     * @return The string representation of the message.
     */
    @Override public String toString() {
        return String.format("Topic='%s'; Sender='%s'; Targets='%s', Frames='%s'", topic_, sender_,
                getTargetsAsString(), String.join("|", getMessageParts()));
    }

    private final String topic_;
    private final String sender_;
    private final Set<String> targets_ = new HashSet<>();
    private final ArrayList<String> frames_ = new ArrayList<>();
}
