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

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.junit.*;
import static org.junit.Assert.*;

public class EncryptedFileOutputStreamTest {
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
    }

    @After
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
