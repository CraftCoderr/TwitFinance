package com.qvim.hs.util.logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import jline.console.ConsoleReader;

/**
 * Created by RINES on 21.04.17.
 */
public class OwnLogger extends Logger {

    private final Formatter formatter = new ConciseFormatter();
    private final LogDispatcher dispatcher = new LogDispatcher(this);

    @SuppressWarnings({ "CallToPrintStackTrace", "CallToThreadStartDuringObjectConstruction" })
    public OwnLogger(ConsoleReader consoleReader ) {
        super("HackathonServer", null);
        setLevel(Level.ALL);
        try {
            FileHandler fileHandler = new FileHandler("hs.log", 1 << 24, 8, true);
            fileHandler.setFormatter(formatter);
            addHandler(fileHandler);
            
            ColouredWriter consoleHandler = new ColouredWriter(consoleReader);
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(formatter);
            addHandler(consoleHandler);
        } catch (IOException ex) {
            System.err.println("Could not register logger!");
            ex.printStackTrace();
        }
        dispatcher.start();
    }

    @Override
    public void log(LogRecord record) {
        dispatcher.queue(record);
    }

    void doLog(LogRecord record) {
        super.log(record);
    }
}
