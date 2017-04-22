package com.qvim.hs.util.logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by RINES on 21.04.17.
 */
public class LoggingOutputStream extends ByteArrayOutputStream {

    private static final String separator = System.getProperty("line.separator");
    /*========================================================================*/
    private final Logger logger;
    private final Level level;
    
    public LoggingOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void flush() throws IOException {
        String contents = toString("UTF-8");
        super.reset();
        if(!contents.isEmpty() && !contents.equals(separator))
            logger.logp(level, "", "", contents);
    }
    
}