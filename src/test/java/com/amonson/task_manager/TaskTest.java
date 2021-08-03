// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.task_manager;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    @BeforeEach
    public void setUp() throws Exception {
        pid_ = Long.toString(ProcessHandle.current().pid());
    }

    @Test
    public void execute() {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "1000", "Print me!"}, Task.Type.Default,
                null);
        Assertions.assertNull(task.getUserData());
        assertTrue(task.isRunning());
        assertTrue(task.waitForCompletion());
        assertEquals(0, task.getResult());
        assertFalse(task.isRunning());
    }

    @Test
    public void execute2() {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "1000", "Print me!"}, Task.Type.Thread,
                null);
        assertTrue(task.isRunning());
        assertTrue(task.waitForCompletion());
        assertFalse(task.isRunning());
    }

    @Test
    public void execute3() {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "1000", "Print me!"}, Task.Type.Process,
                null);
        assertTrue(task.isRunning());
        assertFalse(task.waitForCompletion(333));
        assertTrue(task.isRunning());
        assertEquals(Integer.MIN_VALUE, task.getResult(333));
        assertEquals(0, task.getResult());
    }

    @Test
    public void execute4() {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "1000", "Print me!"}, Task.Type.Process,
                null);
        assertTrue(task.isRunning());
        assertFalse(task.waitForCompletion(333));
        assertTrue(task.isRunning());
        assertEquals(Integer.MIN_VALUE, task.getResult(333));
        task.stopRunning();
    }

    @Test
    public void execute5() throws Exception {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "1000", "Print me!"}, Task.Type.Thread,
                null);
        assertTrue(task.isRunning());
        assertFalse(task.waitForCompletion(333));
        assertTrue(task.isRunning());
        assertEquals(Integer.MIN_VALUE, task.getResult(333));
        task.stopRunning();
        Thread.sleep(500);
        task.stopRunning();
    }

    @Test
    public void execute6() throws Exception {
        Task task = Task.execute(TestApp.class, new String[] {pid_, "-1", "Print me!"}, Task.Type.Thread,
                null);
        assertTrue(task.isRunning());
        assertTrue(task.waitForCompletion(333));
        assertFalse(task.isRunning());
        assertEquals(Integer.MIN_VALUE, task.getResult(333));
        Throwable exception = task.getException();
        Assertions.assertNotNull(exception);
        task.getException(500);
    }

    private String pid_;
}
