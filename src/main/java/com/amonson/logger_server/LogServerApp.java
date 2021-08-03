// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.logger_server;

import com.amonson.logger.*;
import com.amonson.prop_store.PropMap;
import com.amonson.prop_store.PropStore;
import com.amonson.prop_store.PropStoreFactory;
import com.amonson.prop_store.PropStoreFactoryException;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.*;

/**
 * Application for a basic LogRecord Server (ZeroMQ SUB). It will dump to files based on the ZeroMQ topic names.
 */
public class LogServerApp implements Runnable {
    public static void main(String[] args) {
        try {
            new LogServerApp(args).run();
        } catch(PropStoreFactoryException e) {
            System.err.println("ERROR: Failed to create the JSON parser!");
            System.exit(2);
        } catch(ParseException | NumberFormatException e) {
            System.err.printf("ERROR: %s\n", e.getMessage());
            System.exit(1);
        } catch(IOException e) {
            System.err.println("ERROR: Failed to retrieve the resources!");
            System.exit(3);
        } catch(IllegalArgumentException e) {
            System.exit(0);
        }
        System.exit(0);
    }

    @Override
    public void run() {
        System.out.println("Starting logging server at " + url_);
        new ZeroMQLogSubscriber(url_, this::incomingMessage).run();
    }

    LogServerApp(String[] args) throws PropStoreFactoryException, ParseException, IOException {
        parser_ = PropStoreFactory.getStore("json");
        Options options = getOptions();
        DefaultParser parser = new DefaultParser();
        CommandLine cli = parseAndCapture(args, options, parser);
        String version = getClass().getPackage().getImplementationVersion();
        if(cli.hasOption('h')) {
            HelpFormatter format = new HelpFormatter();
            format.printHelp(String.format("java -jar logger_server-%s.jar [OPTIONS]", version), options);
            throw new IllegalArgumentException();
        } else if(cli.hasOption('V')) {
            System.out.printf("Version: %s\n", version);
            throw new IllegalArgumentException();
        }
    }

    private CommandLine parseAndCapture(String[] args, Options options, DefaultParser parser) throws ParseException {
        CommandLine cli = parser.parse(options, args);
        count_ = Integer.parseInt(cli.getOptionValue('c', "10"));
        limit_ = Integer.parseInt(cli.getOptionValue('l', "1024")) * 1_024; // Kb to b.
        url_ = String.format("tcp://*:%s", cli.getOptionValue('p', "64001"));
        folder_ = cli.getOptionValue('f', "/tmp");
        if(folder_.endsWith("/"))
            folder_ = folder_.substring(0, folder_.length() - 1);
        return cli;
    }

    private Options getOptions() {
        Options options = new Options();
        Option option = new Option("c", "count", true, "How many maximum log files to keep at one time.");
        option.setType(Integer.class);
        options.addOption(option);
        option = new Option("l", "limit", true, "How many kilobytes a single file to limit size.");
        option.setType(Integer.class);
        options.addOption(option);
        option = new Option("p", "port", true, "Port to listen on for incoming messages log.");
        option.setType(Integer.class);
        options.addOption(option);
        option = new Option("f", "folder", true, "Folder where all log files are stored.");
        option.setType(String.class);
        options.addOption(option);
        option = new Option("h", "help", false, "Help request.");
        options.addOption(option);
        option = new Option("V", "version", false, "Version request.");
        options.addOption(option);
        return options;
    }

    private void incomingMessage(String topic, LogRecord logRecord, String hostname, int pid) {
        if(!loggers_.containsKey(topic))
            createLogger(topic);
        if(loggers_.containsKey(topic)) {
            PropMap map = LogRecordSerialization.serializeLogRecord(logRecord);
            map.put("hostname", hostname);
            map.put("pid", pid);
            loggers_.get(topic).log(Level.FINEST, parser_.toString(map));
        }
    }

    private void createLogger(String topic) {
        try {
            String pattern = String.format("%s/%s-%%g.log", folder_, topic);
            Handler handler = new FileHandler(pattern, limit_, count_, true);
            Formatter formatter = new PassThruFormatter();
            Logger logger = NativeLoggerFactory.getNamedConfiguredLogger(topic, handler, formatter);
            loggers_.put(topic, logger);
        } catch(IOException e) { /* No logger for filename so these messages will be ignored. */ }
    }

    private int count_;
    private int limit_;
    private String url_;
    private String folder_;
    private final PropStore parser_;
    Map<String, Logger> loggers_ = new HashMap<>();
}
