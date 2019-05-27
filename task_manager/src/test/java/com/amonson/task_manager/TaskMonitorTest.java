// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.task_manager;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskMonitorTest {
    private void taskEnded(Task task) {
        task_ = task;
    }

    @Before
    public void setUp() throws Exception {
        pid_ = Long.toString(ProcessHandle.current().pid());
        task_ = null;
        monitor_ = new TaskMonitor(this::taskEnded);
    }

    @Test
    public void addTask() throws Exception {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "250", "Print me!"},
                Task.Type.Default, null);
        monitor_.addTask(task);
        Thread.sleep(500);
        assertNotNull(task_);
        monitor_.close();
        monitor_.close();
    }

    private TaskMonitor monitor_;
    private volatile Task task_;
    private String pid_;
}
