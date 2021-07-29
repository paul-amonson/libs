// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.volt_wrapper;

public class DataAccessLayerForVoltWrapper implements DataAccessLayer {
    @Override
    public DataAccessLayerResponse query(String name, Object... params) {
        return null;
    }

    @Override
    public void query(DataAccessLayerCallback callback, String name, Object... params) {

    }

    @Override
    public long queryForLong(String name, Object... params) {
        return 0;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }
}
