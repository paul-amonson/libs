// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.task_manager;

/**
 * Description for class TestApp
 */
public class TestApp {
    public static void main(String[] args) throws Exception {
        long pid = Long.valueOf(args[0]);
        System.out.println("App Starting...");
        long value = Long.valueOf(args[1]);
        if(value < 0L)
            throw new Exception("TEST");
        else
            Thread.sleep(value);
        System.out.println(args[2]);
        if(ProcessHandle.current().pid() != pid)
            System.exit(0);
    }
}
