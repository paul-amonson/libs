// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.data_access;

import com.amonson.factory.FactoryGeneric;

import java.util.logging.Logger;

/**
 * DataAccessLayerFactory is the declared factory for @link{DataAccessLayer} instances.
 */
public class DataAccessLayerFactory  extends FactoryGeneric<DataAccessLayer> {
    public DataAccessLayerFactory(Logger logger) {
        super(logger);
        registerClass("voltdb", DataAccessLayerForVoltWrapper.class);
    }
}
