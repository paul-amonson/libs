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

package com.amonson.logger;

class ConsoleLoggerImpl extends Logger {
    ConsoleLoggerImpl() {}

    /**
     * Required to override in derived class implementation.
     *
     * @param lvl     The log level of the log line to be used for filtering.
     * @param logLine The full log line to log.
     */
    @Override
    protected void outputFinalString(Level lvl, String logLine) {
        if(lvl.ordinal() >= getLevel().ordinal()) {
            if(lvl.ordinal() >= Level.ERROR.ordinal())
                System.err.println(logLine);
            else
                System.out.println(logLine);
        }
    }
}
