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

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Class to open an encrypted file stream for read.
 */
public class EncryptedFileInputStream extends InputStream {
    /**
     * Open a file for read.
     * @param filename File to open for read.
     * @param key Key object for crypto.
     * @throws FileNotFoundException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     */
    public EncryptedFileInputStream(String filename, KeyData key) throws FileNotFoundException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        super();
        key_ = key;
        init(new File(filename));
    }

    /**
     * Open a file for read.
     * @param file File to open for read.
     * @param key Key object for crypto.
     * @throws FileNotFoundException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     */
    public EncryptedFileInputStream(File file, KeyData key) throws FileNotFoundException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        super();
        key_ = key;
        init(file);
    }

    /**
     * Read a byte.
     * @return The byte read.
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        return in_.read();
    }

    /**
     * Read bytes.
     * @param bytes Buffer for bytes.
     * @return count of bytes read.
     * @throws IOException
     */
    @Override
    public int read(byte[] bytes) throws IOException {
        return in_.read(bytes);
    }

    /**
     * Read bytes.
     * @param bytes Buffer for bytes.
     * @param off offset in byte buffer.
     * @param len length of bytes.
     * @return count of bytes read.
     * @throws IOException
     */
    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        return in_.read(bytes, off, len);
    }

    /**
     * Close the stream.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        super.close();
        in_.close();
    }

    private void init(File file) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, FileNotFoundException {
        IvParameterSpec ivSpec = new IvParameterSpec(key_.IVAsBytes());
        SecretKeySpec keySpec = new SecretKeySpec(key_.keyAsBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        in_ = new CipherInputStream(new FileInputStream(file), cipher);
    }

    private KeyData key_;
    private CipherInputStream in_;
}
