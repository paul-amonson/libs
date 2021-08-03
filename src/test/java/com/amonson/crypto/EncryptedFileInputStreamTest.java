// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.crypto;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

public class EncryptedFileInputStreamTest {
    private final String golden_ = "0123456789012345678901234567890123456789";
    private KeyData key_ = null;

    @BeforeEach
    public void setup() {
        if(key_ == null) {
            try {
                key_ = new KeyData();
            } catch(NoSuchAlgorithmException e) {
                Assertions.fail(e.getMessage());
            }
        }
        try {
            EncryptedFileOutputStream stream = new EncryptedFileOutputStream("file.tmp", key_);
            byte[] bytes = golden_.getBytes();
            stream.write(bytes);
            stream.flush();
            stream.close();
        } catch(Exception e) {
            Assertions.fail(e.getMessage());
        }

    }

    @AfterEach
    public void tearDown() {
        File file = new File("file.tmp");
        Assertions.assertTrue(file.delete());
    }

    @Test
    public void ctor() throws Exception {
        String filename = "file.tmp";
        EncryptedFileInputStream stream = new EncryptedFileInputStream(filename, key_);
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[1024];
        StringBuilder str = new StringBuilder();
        int read;
        while((read = reader.read(buffer, 0, 1024)) != -1)
            str.append(String.copyValueOf(buffer, 0, read));
        Assertions.assertEquals(golden_, str.toString());
        reader.close();
    }

    @Test
    public void ctor2() throws Exception {
        File file = new File("file.tmp");
        EncryptedFileInputStream stream = new EncryptedFileInputStream(file, key_);
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[1024];
        StringBuilder str = new StringBuilder();
        int read;
        while((read = reader.read(buffer, 0, 1024)) != -1)
            str.append(String.copyValueOf(buffer, 0, read));
        Assertions.assertEquals(golden_, str.toString());
        reader.close();
    }

    @Test
    public void ctor3() throws Exception {
        File file = new File("file.tmp");
        EncryptedFileInputStream stream = new EncryptedFileInputStream(file, key_);
        Assertions.assertEquals(48, stream.read());
        byte[] bytes = new byte[39];
        stream.read(bytes);
        stream.close();
    }
}
