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

package com.amonson.crypto;

import org.junit.*;
import java.io.File;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

public class EncryptedFileInputStreamTest {
    private String golden_ = "0123456789012345678901234567890123456789";
    private KeyData key_ = null;

    @Before
    public void setup() {
        if(key_ == null) {
            try {
                key_ = new KeyData();
            } catch(NoSuchAlgorithmException e) {
                fail(e.getMessage());
            }
        }
        try {
            EncryptedFileOutputStream stream = new EncryptedFileOutputStream("file.tmp", key_);
            byte[] bytes = golden_.getBytes();
            stream.write(bytes);
            stream.flush();
            stream.close();
        } catch(Exception e) {
            fail(e.getMessage());
        }

    }

    @After
    public void tearDown() {
        File file = new File("file.tmp");
        assertTrue(file.delete());
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
        assertEquals(golden_, str.toString());
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
        assertEquals(golden_, str.toString());
        reader.close();
    }

    @Test
    public void ctor3() throws Exception {
        File file = new File("file.tmp");
        EncryptedFileInputStream stream = new EncryptedFileInputStream(file, key_);
        assertEquals(48, stream.read());
        byte[] bytes = new byte[39];
        stream.read(bytes);
        stream.close();
    }
}
