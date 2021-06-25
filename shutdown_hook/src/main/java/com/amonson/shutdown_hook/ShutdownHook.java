// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.shutdown_hook;

import com.amonson.logger.Logger;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class to use for Java application at shutdown that will close AutoCloseable objects in the reverse order that they
 * were added.
 */
@Deprecated(forRemoval = true, since = "1.3.0")
public class ShutdownHook {
    /**
     * Create a ShutdownHook singleton with a Logger.
     *
     * @param logger The logger. If null, then nothing will be logged at shutdown.
     */
    public static ShutdownHook createShutdownHook(Logger logger) {
        if(singleton_ == null)
            singleton_ = new ShutdownHook(logger);
        return singleton_;
    }

    /**
     * Push an AutoCloseable resource object onto the shutdown stack.
     *
     * @param closeable The AutoCloseable resource to cleanup after application shutdown is in progress.
     */
    final public void pushAutoCloseableResource(AutoCloseable closeable) {
        if(closeable == null) return;
        if(!shutdownInProgress_.get()) {
            if (!closeables_.contains(closeable))
                closeables_.push(closeable);
        }
    }

    /**
     * Remove an AutoCloseable resource object from the shutdown stack.
     *
     * @param closeable  The AutoCloseable resource to cleanup after application shutdown is in progress.
     */
    final public void removeAutoCloseableResource(AutoCloseable closeable) {
        if(!shutdownInProgress_.get())
            closeables_.remove(closeable);
    }

    void addHook(Thread hookThread) {
        Runtime.getRuntime().addShutdownHook(hookThread);
    }

    ShutdownHook(Logger logger) {
        addHook(new Thread(() -> {
            shutdownInProgress_.set(true);
            while(!closeables_.isEmpty()) {
                AutoCloseable toClose = closeables_.pop();
                try {
                    toClose.close();
                } catch(Exception e) {
                    if(logger != null)
                        logger.except(e, "Closing object '%s' failed with exception",
                                toClose.getClass().getCanonicalName());
                }
            }
        }));
    }

            final  ConcurrentLinkedDeque<AutoCloseable> closeables_ = new ConcurrentLinkedDeque<>();
    private final  AtomicBoolean shutdownInProgress_ = new AtomicBoolean(false);
    private static ShutdownHook singleton_ = null;
}
