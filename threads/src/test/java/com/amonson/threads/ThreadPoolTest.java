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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class ThreadPoolTest implements WorkItemCallback, ThreadPoolRunnable {
    @Before
    public void setUp() {
        result_ = null;
        exceptionResult_ = null;
        throwException_ = false;
    }

    @Override
    public void finishedWorkItem(Object result, Exception ifThrown) {
        result_ = (String)result;
        exceptionResult_ = ifThrown;
    }

    @Override
    public Object run(AtomicBoolean signalToStop, Object[] arguments) {
        if(throwException_)
            throw new RuntimeException("Test Exception");
        return "My Result";
    }

    @Test
    public void test1() throws Exception {
        try (ThreadPool pool = new ThreadPool()) {
            pool.addWork(this, this,"one", "two", "three");
            Thread.sleep(80);
        }
        assertEquals("My Result", result_);
        assertNull(exceptionResult_);
    }

    @Test
    public void test2() throws Exception {
        try (ThreadPool pool = new ThreadPool(1)) {
            pool.addWork(this, "one", "two", "three");
            Thread.sleep(80);
        }
        assertNull(result_);
        assertNull(exceptionResult_);
    }

    @Test
    public void test3() throws Exception {
        try (ThreadPool pool = new ThreadPool("TestPool")) {
            throwException_ = true;
            pool.addWork(this, this);
            Thread.sleep(80);
        }
        assertNull("My Result", result_);
        assertNotNull(exceptionResult_);
    }

    @Test
    public void test4() throws Exception {
        try (ThreadPool pool = new ThreadPool()) {
            pool.addWork(this);
            Thread.sleep(80);
        }
        assertNull(result_);
        assertNull(exceptionResult_);
    }

    private String result_;
    private Exception exceptionResult_;
    private boolean throwException_;
}
