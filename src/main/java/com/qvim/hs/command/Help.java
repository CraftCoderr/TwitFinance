package com.qvim.hs.command;

/**
 * Created by RINES on 22.04.17.
 */
public class Help extends AbstractCommand {

    Help() {
        super("help", "shows help menu");
    }

    @Override
    public void run(String[] args) {
        log("&6List of available commands:");
        CommandManager.COMMANDS.values().forEach(cmd -> log("&e%s &8- &7%s&8.", cmd.getName(), cmd.getDescription()));
    }
}
