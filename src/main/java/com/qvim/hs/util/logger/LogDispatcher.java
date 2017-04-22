package com.qvim.hs.util.logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogRecord;

/**
 * Created by RINES on 21.04.17.
 */
public class LogDispatcher extends Thread {

    private final OwnLogger logger;
    private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>();

    public LogDispatcher(OwnLogger logger) {
        super("DmsStats Logger Thread");
        this.logger = logger;
    }

    @Override
    public void run() {
        while(!isInterrupted()) {
            LogRecord record;
            try {
                record = queue.take();
            }catch(InterruptedException ex) {
                continue;
            }
            logger.doLog(record);
        }
        queue.forEach(logger::doLog);
    }

    public void queue(LogRecord record) {
        if(!isInterrupted())
            queue.add(record);
    }
}
