package com.qvim.hs.command;

import com.qvim.hs.HackathonServer;

/**
 * Created by RINES on 22.04.17.
 */
public class Stop extends AbstractCommand {

    Stop() {
        super("stop", "stops the server");
    }

    @Override
    public void run(String[] args) {
        log("&eStopping the server..");
        HackathonServer.getInstance().disable();
    }
}
