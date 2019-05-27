// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.task_manager;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A monitor to "watch" Tasks for completion. The following system properties are used:
 *
 * <b>com.amonson.task_manager.monitor_pause</b> - the wait time between checks in the monitor loop (def: 250ms).
 * <b>com.amonson.task_manager.shutdown_timeout</b> - the timeout when the monitor is stopping (def: 5000ms).
 */
public class TaskMonitor implements Closeable, AutoCloseable {
    /**
     * Functional interface for a completion callback for a task.
     */
    @FunctionalInterface
    public interface Callback {
        void taskEnded(Task task);
    }

    /**
     * Constructs a TaskMonitor with a specific callback which will be called on Task completion.
     *
     * @param callback The callback to call when the Task finishes.
     */
    public TaskMonitor(Callback callback) {
        callback_ = callback;
        monitoringThread_ = new Thread(() -> {
            while(!halt_.get()) {
                try { Thread.sleep(MONITOR_LOOP); } catch(InterruptedException e) { /* Ignore */ }
                if(!halt_.get()) {
                    for(Task task: tasks_)
                        checkTask(task);
                    for(Task remove: removeList_)
                        tasks_.remove(remove);
                    if(Thread.currentThread().isInterrupted()) halt_.set(true);
                }
            }
        });
        monitoringThread_.start();
    }

    /**
     * Add the task to the monitor.
     *
     * @param task The running Task to add to the monitor.
     */
    public void addTask(Task task) {
        tasks_.add(task);
    }

    /**
     * Stop the monitoring thread.
     *
     * @throws IOException Actually does not throw an exception, this is required by AutoClosable and Closable.
     */
    @Override
    public void close() throws IOException {
        if(!halt_.get()) {
            halt_.set(true);
            try {
                monitoringThread_.join(CLOSE_TIMEOUT);
            } catch (InterruptedException e) { /* Do nothing */ }
        }
    }

    private void checkTask(Task task) {
        if(!task.isRunning()) {
            if(callback_ != null) callback_.taskEnded(task);
            removeList_.add(task);
        }
    }

    private ConcurrentLinkedQueue<Task> tasks_ = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Task> removeList_ = new ConcurrentLinkedQueue<>();
    private Thread monitoringThread_;
    private Callback callback_;
    private AtomicBoolean halt_ = new AtomicBoolean(false);

    private static long MONITOR_LOOP = Integer.parseInt(System.getProperty(
            "com.amonson.task_manager.monitor_pause", "50"));
    private static long CLOSE_TIMEOUT = Integer.parseInt(System.getProperty(
            "com.amonson.task_manager.shutdown_timeout", "5000"));
}
