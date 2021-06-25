// Copyright (C) 2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import com.amonson.prop_store.PropStore;
import com.amonson.prop_store.PropStoreException;
import com.amonson.prop_store.PropStoreFactory;
import com.amonson.prop_store.PropStoreFactoryException;
import org.zeromq.ZSocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZeroMQLoggingPublisherTest {
    private static ZSocket testCreator(String zeroMQUrl) {
        return socket_;
    }

    @BeforeAll
    public static void setUpClass() {
        if(socket_ == null)
            socket_ = mock(ZSocket.class);
        ZeroMQLoggingPublisher.CREATOR = ZeroMQLoggingPublisherTest::testCreator;
    }

    @BeforeEach
    public void setUp() {
        log_.clear();
        when(socket_.sendStringUtf8(anyString())).thenAnswer(invocation -> {
            String message = ((String)invocation.getArguments()[0]);
            if(!message.isBlank())
                log_.add(message);
            return message.getBytes().length;
        });
        when(socket_.sendStringUtf8(anyString(), anyInt())).thenAnswer(invocation -> {
            String topic = ((String)invocation.getArguments()[0]);
            if(!topic.isBlank())
                log_.add(topic);
            return topic.getBytes().length;
        });
    }

    @Test
    public void ctors() throws IOException {
        new ZeroMQLoggingPublisher("url").close();
        new ZeroMQLoggingPublisher("url", "topic").close();
        new ZeroMQLoggingPublisher("url", false).close();
        new ZeroMQLoggingPublisher("url", "topic", false).close();
    }

    @Test
    public void logging() throws PropStoreException, PropStoreFactoryException, IOException {
        try (ZeroMQLoggingPublisher logger = new ZeroMQLoggingPublisher("url")) {
            logger.log(null, Level.ERROR, Level.WARN, Thread.currentThread().getStackTrace()[0], "message(%d)", 0);
            logger.log(null, Level.ERROR, Level.WARN, Thread.currentThread().getStackTrace()[0], "message(%d)", 1);
        }
        PropStore store = PropStoreFactory.getStore("json");
        store.fromStringToMap(log_.get(1)); // Throws exception if not valid JSON.
        store.fromStringToMap(log_.get(3)); // Throws exception if not valid JSON.
        assertEquals(4, log_.size());
    }

    private static ZSocket socket_ = null;
    private static final List<String> log_ = new ArrayList<>();
}
