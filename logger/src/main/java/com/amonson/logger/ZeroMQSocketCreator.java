// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import org.zeromq.ZSocket;

@FunctionalInterface
interface ZeroMQSocketCreator {
    ZSocket create(String zeroMQUrl);
}
