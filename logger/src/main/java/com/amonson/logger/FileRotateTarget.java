// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import java.io.*;
import java.time.Instant;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

/**
 * Description for class FileRotateTarget
 */
@Deprecated(forRemoval = true, since = "1.3.1")
public class FileRotateTarget implements OutputTargetInterface {
    public FileRotateTarget(File filename) {
        if(filename == null)
            throw new RuntimeException("Must specify a real filename for this output target class");
        filename_ = filename;
    }

    /**
     * Method definition for an output target of a logger line.
     *
     * @param config          The configuration passed to the logger.
     * @param loggedLevel     The Logger.Level of the line being output.
     * @param location        The location of the code that logged the line.
     * @param buildLineMethod The callback that will format the full log line with the Logger settings.
     * @param fullMessage     The actual predefined formatted line to output.
     * @param filter          If the Logger filter is to be used this is the current filter level.
     *
     * <b>Configuration properties:</b><br>
     * <ul>
     *   <li>com.amonson.logger.FileRotateTarget.maxRotateSize - Size in bytes when passed will force log rotation.</li>
     *   <li>com.amonson.logger.FileRotateTarget.compress - true/false compress the rotated logs with GZip.</li>
     * </ul>
     */
    @Override
    public void outputFinalString(Properties config, Level loggedLevel, StackTraceElement location,
                                  BuildLogLineInterface buildLineMethod, String fullMessage, Level filter) {
        processConfiguration(config);
        if(loggedLevel.ordinal() >= filter.ordinal()) {
            synchronized(this) {
                try (FileWriter writer = new FileWriter(filename_, true)) {
                    writer.write(String.format("%s\n", fullMessage));
                } catch(IOException e) { /* File out failed, silently rely on other OutputTargetInterface objects */ }
                if(filename_.length() > rotateFileMaxSize_)
                    rotateLog();
            }
        }
    }

    private void rotateLog() {
        String timestamp = Instant.now().toString().replace(' ', 'T');
        File newName = new File(filename_.toString() + "." + timestamp);
        if(!filename_.renameTo(newName))
            return;
        if(compress_)
            copyFileAndCompress(newName);
        filename_.delete();
    }

    private void copyFileAndCompress(File file) {
        File compressed = new File(file.toString() + ".gz");
        try (InputStream input = new FileInputStream(file)) {
            try (OutputStream output = new FileOutputStream(compressed)) {
                try (OutputStream gzip = new GZIPOutputStream(output)) {
                    input.transferTo(gzip);
                }
            }
        } catch(IOException e) {
            return;
        }
        file.delete();
    }

    private void processConfiguration(Properties config) {
        if(configuration_ == null && config != null) {
            configuration_ = config;
            rotateFileMaxSize_ = Long.parseLong(configuration_.getProperty(getClass().getCanonicalName() +
                    ".maxRotateSize", Long.toString(rotateFileMaxSize_)));
            compress_ = Boolean.parseBoolean(configuration_.getProperty(getClass().getCanonicalName() +
                    ".compress", Boolean.toString(compress_)));
        }
    }

    private Properties configuration_ = null;
    private File filename_;
    private long rotateFileMaxSize_ = 20000000; // Default 20MB
    private boolean compress_ = true; // compress rotated logs.
}
