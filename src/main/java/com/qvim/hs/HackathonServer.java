package com.qvim.hs;

import com.qvim.hs.command.CommandManager;
import com.qvim.hs.news.NewsCache;
import com.qvim.hs.news.Twitter4J;
import com.qvim.hs.rates.IntegrityCache;
import com.qvim.hs.util.Scheduler;
import com.qvim.hs.util.configuration.ConfigurationManager;
import com.qvim.hs.util.configuration.FileConfiguration;
import com.qvim.hs.util.logger.Logger;
import com.qvim.hs.util.logger.LoggingOutputStream;
import com.qvim.hs.util.logger.OwnLogger;
import jline.console.ConsoleReader;
import lombok.Getter;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by RINES on 21.04.17.
 */
public class HackathonServer {

    @Getter
    private static HackathonServer instance;

    @Getter
    private int webPort;

    @Getter
    private Twitter4J twitter;

    HackathonServer() {
        instance = this;
        configuration: {
            FileConfiguration config = getConfig("web");
            this.webPort = config.getInt("port", 722);
            saveConfig("web");
        }
        preloadLogger();
        this.twitter = new Twitter4J();
        IntegrityCache.preloadExchangeRates();
        NewsCache.preloadNews();
    }

    public void disable() {
        Scheduler.sleep(3000L);
        System.exit(0);
    }

    private void preloadLogger() {
        System.setProperty("library.jansi.version", "HackathonServer");
        AnsiConsole.systemInstall();
        ConsoleReader consoleReader;
        try {
            consoleReader = new ConsoleReader();
            consoleReader.setExpandEvents(false);
            OwnLogger logger = new OwnLogger(consoleReader);
            System.setErr(new PrintStream(new LoggingOutputStream(logger, Level.SEVERE), true));
            System.setOut(new PrintStream(new LoggingOutputStream(logger, Level.INFO), true));
            Logger.setLogger(logger);
        }catch(IOException ex) {
            throw new IllegalStateException("Could not load console worker!");
        }
        Logger.log("Enabling HackathonServer..");
        CommandManager.initialize();
        Scheduler.run(() -> {
            while (true) {
                try {
                    String line = consoleReader.readLine("> ");
                    CommandManager.handleLine(line);
                } catch (Exception ex) {
                    Logger.warn("Can not handle command from console!");
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static FileConfiguration getConfig(String name) {
        return ConfigurationManager.getConfig(name);
    }

    public static void saveConfig(String name) {
        ConfigurationManager.saveConfig(name);
    }

    public static void reloadConfig(String name) {
        ConfigurationManager.reloadConfig(name);
    }

}
