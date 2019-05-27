// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.task_manager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Task that can be run as a process or as an isolated thread.
 */
public class Task {
    /**
     * Possible types of task execution (Process=Default or Thread).
     */
    public enum Type {
        Default,
        Process,
        Thread
    }

    /**
     * Execute the task either on a Thread or Process. This constructs a running Task.
     *
     * @param mainClass The Class object ofthe class containing the "public static void main(String[] args)".
     * @param args The arguments to pass to the application.
     * @param type The type of execution (either Thread or Process).
     * @param userData Arbitrary user data object stored in the Task object. This is not passed to the thread/process.
     * @return The Task object of the running task.
     */
    public static Task execute(Class<?> mainClass, String[] args, Type type, Object userData) {
        if(type == Type.Default) type = Type.Process; // Current default.
        if(type == Type.Process) {
            Task task = new Task(mainClass, args, userData);
            task.doProcessExecute();
            return task;
        } else {
            Task task = new Task(mainClass, args, userData);
            task.doThreadExecute();
            return task;
        }
    }

    /**
     * Test if the Task is running.
     *
     * @return true if the Task is still running, false if the Task has stopped.
     */
    public boolean isRunning() {
        if(process_ != null)
            return process_.isAlive();
        else
            return thread_.isAlive();
    }

    /**
     * Stops the Task. Uses .destroy() on a process or .interrupt() on a thread.
     */
    public void stopRunning() {
        if(isRunning()) {
            if(process_ != null)
                process_.destroy();
            else
                thread_.interrupt();
        }
    }

    /**
     * Block until the Task stops.
     *
     * @return true if the wait exits normally (stopped) or false if it did not stop normally.
     */
    public boolean waitForCompletion() {
        if(!isRunning()) return true;
        if(process_ != null)
            try { process_.waitFor(); return true; } catch(InterruptedException e) { return false; }
        else if(thread_ != null)
            try { thread_.join(); return true; } catch(InterruptedException e) { return false; }
        return false; // Should not get here but the future may have other execution methods...
    }

    /**
     * Block until stopped or a timeout expires.
     *
     * @param milliSeconds The milliseconds to wait for stopping.
     * @return true if the Task stopped in less than the timeout, false otherwise.
     */
    public boolean waitForCompletion(long milliSeconds) {
        if(!isRunning()) return true;
        if(process_ != null)
            try { return process_.waitFor(milliSeconds, TimeUnit.MILLISECONDS); }
            catch(InterruptedException e) { return false; }
        else if(thread_ != null)
            try { thread_.join(milliSeconds, 0); return !thread_.isAlive(); }
            catch(InterruptedException e) { return false; }
        return false; // Should not get here but the future may have other execution methods...
    }

    /**
     * Get the return code from the application task. This will block and wait if not already stopped.
     *
     * @return An integer return code. For a process this is the code returned to the shell.
     */
    public int getResult() {
        if(waitForCompletion() && exception_.get() == null) {
            if(process_ != null)
                return process_.exitValue();
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Get the return code from the application task. This will block and wait if not already stopped until a timeout
     * in ms.
     *
     * @param milliSeconds The milliseconds to wait for  completion.
     * @return An integer return code. For a process this is the code returned to the shell. This will be
     * Integer.MIN_VALUE if the timeout expires.
     */
    public int getResult(long milliSeconds) {
        if(waitForCompletion(milliSeconds) && exception_.get() == null) {
            if(process_ != null)
                return process_.exitValue();
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Retrieve the user data supplied in execute().
     *
     * @return The data passed by the user and stored in the Task.
     */
    public Object getUserData() { return userData_; }

    /**
     * Get a exception if the Thread.start() or ProcessBuilder.start() threw an exception. Will wait for completion.
     *
     * @return The Throwable thrown or null if no exception was thrown.
     */
    public Throwable getException() {
        waitForCompletion();
        return exception_.get();
    }

    /**
     * Get a exception if the Thread.start() or ProcessBuilder.start() threw an exception. Will wait for completion.
     *
     * @param milliSeconds The timeout in ms to wait for completion.
     * @return The Throwable thrown or null if not exception or the timeout expired.
     */
    public Throwable getException(long milliSeconds) {
        waitForCompletion(milliSeconds);
        return exception_.get();
    }

    private Task(Class<?> mainClass, String[] args, Object userData) {
        class_ = mainClass;
        arguments_ = args;
    }

    private void doProcessExecute() {
        ProcessBuilder builder = new ProcessBuilder();
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        String classPath = System.getProperty("java.class.path");
        String[] cmds = new String[4 + arguments_.length];
        cmds[0] = java;
        cmds[1] = "-cp";
        cmds[2] = classPath;
        cmds[3] = class_.getCanonicalName();
        System.arraycopy(arguments_, 0, cmds, 4, arguments_.length);
        builder.command(cmds);
        try {
            process_ = builder.start();
        } catch(IOException e) {
            exception_.set(e);
        }
    }

    private void doThreadExecute() {
        thread_ = new Thread(() -> {
            try {
                Method main = class_.getMethod("main", String[].class);
                result_.set((Integer)main.invoke(null, (Object)arguments_));
            } catch(Exception e) {
                exception_.set(e);
            }
        });
        thread_.start();
    }

    private Class<?> class_;
    private String[] arguments_;
    private Thread thread_ = null;
    private AtomicInteger result_ = new AtomicInteger(0);
    private AtomicReference<Throwable> exception_ = new AtomicReference<>(null);
    private Process process_ = null;
    private Object userData_ = null;
}
