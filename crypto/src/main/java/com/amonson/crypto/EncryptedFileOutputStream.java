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
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Class to open an encrypted file stream for write.
 */
public class EncryptedFileOutputStream extends OutputStream {
    /**
     * Open a file for write that is AES encrypted.
     * @param filename File to open for writing.
     * @param key Key object for crypto.
     * @throws FileNotFoundException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    public EncryptedFileOutputStream(String filename, KeyData key) throws FileNotFoundException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        super();
        key_ = key;
        init(new File(filename));
    }

    /**
     * Open a file for write that is AES encrypted.
     * @param file File to open for writing.
     * @param key Key object for crypto.
     * @throws FileNotFoundException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     */
    public EncryptedFileOutputStream(File file, KeyData key) throws FileNotFoundException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        super();
        key_ = key;
        init(file);
    }

    /**
     * Write a byte to the stream.
     * @param n The byte to write.
     * @throws IOException
     */
    @Override
    public void write(int n) throws IOException {
        out_.write(n);
    }

    /**
     * Write bytes to the stream.
     * @param bytes The bytes to write.
     * @throws IOException
     */
    @Override
    public void write(byte[] bytes) throws IOException {
        out_.write(bytes);
    }

    /**
     * Write bytes to the stream.
     * @param bytes The bytes to write.
     * @param off offset in byte buffer.
     * @param len length of bytes.
     * @throws IOException
     */
    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        out_.write(bytes, off, len);
    }

    /**
     * Close the stream.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        out_.close();
        super.close();
    }

    /**
     * Flush bytes to disk.
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        out_.flush();
        super.flush();
    }

    private void init(File file) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, FileNotFoundException {
        IvParameterSpec ivSpec = new IvParameterSpec(key_.IVAsBytes());
        SecretKeySpec keySpec = new SecretKeySpec(key_.keyAsBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        out_ = new CipherOutputStream(new FileOutputStream(file), cipher);
    }

    private KeyData key_;
    private CipherOutputStream out_;
}
