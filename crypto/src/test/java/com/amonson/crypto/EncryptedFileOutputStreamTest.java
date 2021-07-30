// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.crypto;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class EncryptedFileOutputStreamTest {
    private KeyData key_ = null;

    @BeforeEach
    public void setup() {
        if(key_ == null) {
            try {
                key_ = new KeyData();
            } catch(NoSuchAlgorithmException e) {
                fail(e.getMessage());
            }
        }
    }

    @AfterEach
    public void tearDown() {
        File file = new File("file2.tmp");
        file.delete();
    }

    @Test
    public void ctor() throws Exception {
        try (EncryptedFileOutputStream stream = new EncryptedFileOutputStream("file2.tmp", key_)) {
            stream.write(0);
            byte[] bytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            stream.write(bytes, 0, 10);
            stream.flush();
        }
        try (EncryptedFileOutputStream stream = new EncryptedFileOutputStream(new File("file2.tmp"), key_)) {
            stream.write(0);
            byte[] bytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            stream.write(bytes, 0, 10);
            stream.flush();
        }
    }
}
