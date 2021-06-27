// Copyright (C) 2018 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.logger;

import org.zeromq.ZSocket;

@FunctionalInterface
@Deprecated(forRemoval = true, since = "1.3.1")
interface ZeroMQSocketCreator {
    ZSocket create(String zeroMQUrl);
}
