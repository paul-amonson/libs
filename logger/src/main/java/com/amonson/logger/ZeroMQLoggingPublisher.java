// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import org.zeromq.ZSocket;
import zmq.ZMQ;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Objects;
import java.util.Properties;
import java.net.InetAddress;

/**
 * Description for class ZeroMQLoggingPublisher
 */
@Deprecated(forRemoval = true, since = "1.3.1")
public class ZeroMQLoggingPublisher implements EarlyInterceptLog, AutoCloseable, Closeable {
    /**
     * Create a EarlyInterceptLog version that publishes to a ZeroMQ SUB server. This uses a DEFAULT_TOPIC and passes
     * <b>true</b> to the callTargets parameter of the complete constructor.
     *
     * @param zeroMQUrl The ZeroMQ formatted url to publish data to. This us usually a SUB socket
     */
    public ZeroMQLoggingPublisher(String zeroMQUrl) {
        this(zeroMQUrl, DEFAULT_TOPIC, true);
    }

    /**
     * Create a EarlyInterceptLog version that publishes to a ZeroMQ SUB server. This passes <b>true</b> to the
     * callTargets parameter of the complete constructor.
     *
     * @param zeroMQUrl The ZeroMQ formatted url to publish data to. This us usually a SUB socket
     * @param topic The PUB topic all log messages are published to.
     */
    public ZeroMQLoggingPublisher(String zeroMQUrl, String topic) {
        this(zeroMQUrl, topic, true);
    }

    /**
     * Create a EarlyInterceptLog version that publishes to a ZeroMQ SUB server.
     *
     * @param zeroMQUrl The ZeroMQ formatted url to publish data to. This us usually a SUB socket
     * @param callTargets This determine whether subsequent output targets (like the default console). <b>true</b>
     *        indicated to make the output target calls and <b>false</b> indicated to skipp the calls.
     */
    public ZeroMQLoggingPublisher(String zeroMQUrl, boolean callTargets) {
        this(zeroMQUrl, DEFAULT_TOPIC, callTargets);
    }

    /**
     * Create a EarlyInterceptLog version that publishes to a ZeroMQ SUB server.
     *
     * @param zeroMQUrl The ZeroMQ formatted url to publish data to. This us usually a SUB socket
     * @param topic The PUB topic all log messages are published to.
     * @param callTargets This determine whether subsequent output targets (like the default console). <b>true</b>
     *        indicated to make the output target calls and <b>false</b> indicated to skipp the calls.
     */
    public ZeroMQLoggingPublisher(String zeroMQUrl, String topic, boolean callTargets) {
        topic_ = Objects.requireNonNullElse(topic, "default");
        callTargets_ = callTargets;
        publish_ = CREATOR.create(zeroMQUrl);
    }

    /**
     * Method to intercept logging process early to completely customize the logger.
     *
     * @param config          The configuration passed to the logger.
     * @param lvl             The Level of this log message.
     * @param filterLevel     The current logger filter level of the logger.
     * @param callingLocation Either null or the calling stack location of an exception.
     * @param msg             The logged message format string.
     * @param args            The arguments for the format string.
     * @return true to call output targets or false to skip calling output targets.
     */
    @Override
    public boolean log(Properties config, Level lvl, Level filterLevel, StackTraceElement callingLocation, String msg,
                       Object... args) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        stringValue(json, "timestamp", Instant.now().toString()).append(',');
        stringValue(json, "file", callingLocation.getFileName()).append(',');
        json.append('"').append("line").append('"').append(":").append('"').append(callingLocation.getLineNumber()).
                append('"').append(',');
        stringValue(json, "module", callingLocation.getModuleName()).append(',');
        stringValue(json, "module_version", callingLocation.getModuleVersion()).append(',');
        stringValue(json, "class", callingLocation.getClassName()).append(',');
        stringValue(json, "method", callingLocation.getMethodName()).append(',');
        stringValue(json, "level", lvl.toString()).append(',');
        stringValue(json, "filter", filterLevel.toString()).append(',');
        stringValue(json, "message", String.format(msg, args)).append(',');
        stringValue(json, "topic", topic_);
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            json.append(',');
            stringValue(json, "hostname", inetAddress.getHostName()).append(',');
            stringValue(json, "ip", inetAddress.getHostAddress());
        } catch(UnknownHostException e) { /* Skip hostname and ip if the host is not properly setup.*/ }
        json.append("}");
        publish_.sendStringUtf8(topic_, ZMQ.ZMQ_MORE);
        publish_.sendStringUtf8(json.toString());
        return callTargets_;
    }

    /**
     * Closes the socket then context.
     *
     * @throws IOException When the close of either close operation fails.
     */
    @Override
    public void close() throws IOException {
        publish_.close();
    }

    private StringBuilder stringValue(StringBuilder builder, String name, String value) {
        return builder.append('"').append(name).append('"').append(":").append('"').append(value).append('"');
    }

    private static ZSocket createPublishSocket(String zeroMQUrl) {
        ZSocket socket = new ZSocket(ZMQ.ZMQ_PUB);
        socket.connect(zeroMQUrl);
        return socket;
    }

    private final String topic_;
    private final boolean callTargets_;
    private final ZSocket publish_;
    private static final String DEFAULT_TOPIC = "default";

    static ZeroMQSocketCreator CREATOR = ZeroMQLoggingPublisher::createPublishSocket;
}
