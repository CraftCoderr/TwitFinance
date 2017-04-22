package com.qvim.hs.command;

import com.qvim.hs.util.logger.ChatColor;
import com.qvim.hs.util.logger.Logger;
import lombok.Data;

/**
 * Created by RINES on 22.04.17.
 */
@Data
public abstract class AbstractCommand {

    private final String name;

    private final String description;

    AbstractCommand(String name, String description) {
        this.name = name;
        this.description = description;
        CommandManager.COMMANDS.put(name.toLowerCase(), this);
    }

    public abstract void run(String[] args);

    protected void notEnoughArgs(String usage) {
        log("&cNot enough arguments!");
        log("&cCorrect usage: &e%s&c.", usage);
    }

    protected void log(String s) {
        Logger.log(ChatColor.translateAlternateColorCodes('&', s));
    }

    protected void log(String s, Object... args) {
        log(String.format(s, args));
    }

}
