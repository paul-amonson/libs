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

package com.amonson.prop_store;

/**
 * Thrown by the PropStoreFactory.getStore or PropStoreFactory.registerNewStore.
 */
@SuppressWarnings("serial")
public class PropStoreFactoryException extends Exception {
    PropStoreFactoryException(String msg, Throwable e) { super(msg, e); }
    PropStoreFactoryException(String msg) { super(msg); }
}
