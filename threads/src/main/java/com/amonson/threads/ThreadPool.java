// Copyright 2018 Paul Amonson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
//        You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.amonson.threads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A general thread pool to execute work asynchronously.
 */
public class ThreadPool implements Runnable, AutoCloseable {
    /**
     * Construct and start a thread pool to do work asynchronously; Defaults to Thread.activeCount().
     */
    public ThreadPool() { this(ThreadPool.class.getCanonicalName(), Thread.activeCount()); }

    /**
     * Construct and start a thread pool to do work asynchronously; Defaults to Thread.activeCount().
     *
     * @param poolName A name for the pool (each thread will begin with this name followed by a number).
     */
    public ThreadPool(String poolName) { this(poolName, Thread.activeCount()); }

    /**
     * Construct and start a thread pool to do work asynchronously; Defaults to Thread.activeCount().
     *
     * @param threadCount The count of threads to use in the pool. Recommended that no more than
     *                    2 * Thread.activeCount() be used.
     */
    public ThreadPool(int threadCount) { this(ThreadPool.class.getCanonicalName(), threadCount); }

    /**
     * Construct and start a thread pool to do work asynchronously.
     *
     * @param poolName A name for the pool (each thread will begin with this name followed by a number).
     * @param threadCount The count of threads to use in the pool. Recommended that no more than
     *                    2 * Thread.activeCount() be used.
     */
    public ThreadPool(String poolName, int threadCount) {
        for(int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(this, String.format("%s-%d", poolName, i + 1));
            pool_.add(thread);
            thread.start();
        }
    }

    @Override
    public void run() {
        while(!stopSignal_.get()) {
            WorkItem item =  workItems_.poll();
            if(item != null) {
                Object result = null;
                Exception workItemException = null;
                try {
                    result = item.work.run(stopSignal_, item.arguments);
                } catch(Exception e) {
                    workItemException = e;
                }
                if (item.finishedCallback != null)
                    item.finishedCallback.finishedWorkItem(result, workItemException);
            } else
                try { Thread.sleep(5); } catch(InterruptedException e) { /* Ignore */ }
        }
    }

    /**
     * Call to close the thread pool and join all threads. AutoClosable support.
     */
    @Override
    public void close() {
        stopSignal_.set(true);
        for(Thread thread: pool_)
            try { thread.join(); } catch(InterruptedException e) { /* Ignore it.*/ }
    }

    /**
     * Add work to be executed in the pool.
     *
     * @param work The ThreadPoolRunnable derived class that will be run in the pool.
     */
    public void addWork(ThreadPoolRunnable work) {
        addWork(work, null, (Object[])null);
    }

    /**
     * Add work to be executed in the pool.
     *
     * @param work The ThreadPoolRunnable derived class that will be run in the pool.
     * @param arguments Argument for work executed.
     */
    public void addWork(ThreadPoolRunnable work, Object... arguments) {
        addWork(work, null, arguments);
    }

    /**
     * Add work to be executed in the pool.
     *
     * @param work The ThreadPoolRunnable derived class that will be run in the pool.
     * @param callback This will be called with the result of the ThreadPoolRunnable method is returned in the pool.
     */
    public void addWork(ThreadPoolRunnable work, WorkItemCallback callback) {
        addWork(work, callback, (Object[])null);
    }

    /**
     * Add work to be executed in the pool.
     *
     * @param work The ThreadPoolRunnable derived class that will be run in the pool.
     * @param callback This will be called with the result of the ThreadPoolRunnable method is returned in the pool.
     * @param arguments Argument for work executed.
     */
    public void addWork(ThreadPoolRunnable work, WorkItemCallback callback, Object... arguments) {
        WorkItem item = new WorkItem(work, callback, arguments);
        workItems_.add(item);
    }

    private Collection<Thread> pool_ = new ArrayList<>();
    private AtomicBoolean stopSignal_ = new AtomicBoolean(false);
    private ConcurrentLinkedQueue<WorkItem> workItems_ = new ConcurrentLinkedQueue<>();

    private static final class WorkItem {
        WorkItem(ThreadPoolRunnable runnable, WorkItemCallback callback, Object[] args) {
            work = runnable;
            finishedCallback = callback;
            arguments = args;
        }

        Object[] arguments;
        ThreadPoolRunnable work;
        WorkItemCallback finishedCallback;
    }
}
