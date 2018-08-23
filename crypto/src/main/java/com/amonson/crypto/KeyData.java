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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import com.amonson.prop_store.*;

/**
 * Class to create/store initialization vectors and keys for AES encryption/decryption.
 */
@SuppressWarnings("serial")
public class KeyData {
    /**
     * Constructor that takes a IV and Key Strings for AES encryption encoded with Base64.
     * @param iv The Base64 encoded initialization vector.
     * @param key The base64 encoded key.
     */
    public KeyData(String iv, String key) {
        iv_ = iv;
        key_ = key;
    }

    /**
     * Duplacating constructor for Key data.
     * @param data The KeyData instance to copy from.
     */
    public KeyData(KeyData data) {
        iv_ = data.IV();
        key_ = data.key();
    }

    /**
     * Generates new strong IV and Key's for AES encryption/decryption.
     * @throws NoSuchAlgorithmException Thrown when the JRE platform does not support strong algorithms for
     * IV/Key generation.
     */
    public KeyData() throws NoSuchAlgorithmException {
        SecureRandom randomSecureRandom = SecureRandom.getInstanceStrong();
        byte[] iv = new byte[16];
        randomSecureRandom.nextBytes(iv);
        iv_ = Base64.getEncoder().encodeToString(iv);
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        SecretKey aesKey = kgen.generateKey();
        key_ = Base64.getEncoder().encodeToString(aesKey.getEncoded());
    }

    /**
     * IV Accessor method.
     * @return The base64 encoded initialization vector.
     */
    public String IV() {
        return iv_;
    }

    /**
     * Key accessor method.
     * @return The base64 encoded key.
     */
    public String key() {
        return key_;
    }

    /**
     * Convert object to human readable representation.
     * @return A human readable string representing the object.
     */
    @Override
    public String toString() {
        return String.format("IV='%s'; Key='%s'", iv_, key_);
    }

    /**
     * @param o The KeyData to compare with this instance.
     * @return true if the 2 instances contain the same data; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if(KeyData.class.isInstance(o)) {
            KeyData kd = (KeyData)o;
            return kd.iv_.equals(iv_) && kd.key_.equals(key_);
        }
        return super.equals(o);
    }

    /**
     * Standard hashCode implementation.
     * @return super.hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Gets the IV as bytes.
     * @return The IV as bytes.
     */
    public byte[] IVAsBytes() {
        return Base64.getDecoder().decode(iv_);
    }

    /**
     * Gets the Key as bytes.
     * @return The Key as bytes.
     */
    public byte[] keyAsBytes() {
        return Base64.getDecoder().decode(key_);
    }

    /**
     * Convert to PropMap.
     * @return The PropMap representation of this object.
     */
    public PropMap toPropMap() {
        return new PropMap() {{
            put("A", iv_);
            put("B", key_);
        }};
    }

    /**
     * Factory for creating a KeyData object from a PropMap object.
     * @param keyProp The object that must be in the same form as toPropMap produces.
     * @return The new KeyData instance.
     */
    public static KeyData fromPropMap(PropMap keyProp) {
        String iv = keyProp.getString("A");
        String key = keyProp.getString("B");
        return new KeyData(iv, key);
    }

    String iv_;
    String key_;
}
