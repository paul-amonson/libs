package com.amonson.logger;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class PassThruFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + "\n";
    }
}
