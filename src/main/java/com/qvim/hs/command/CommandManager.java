package com.qvim.hs.command;

import com.qvim.hs.util.logger.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by RINES on 22.04.17.
 */
public class CommandManager {

    final static Map<String, AbstractCommand> COMMANDS = new HashMap<>();

    public static void initialize() {
        new Help();
        new Stop();
        new ReadNews();
        new NYT();
    }

    public static void handleLine(String line) {
        String[] spl = line.split(" ");
        String cmd = spl[0];
        AbstractCommand command = COMMANDS.get(cmd.toLowerCase());
        if(command == null) {
            Logger.log("What did you mean? Try 'help' to find out existing console commands.");
            return;
        }
        String[] args = new String[spl.length - 1];
        if(args.length > 0)
            System.arraycopy(spl, 1, args, 0, args.length);
        command.run(args);
    }

}
