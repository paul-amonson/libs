package com.amonson.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

@Deprecated
public class PassThruFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + "\n";
    }
}
