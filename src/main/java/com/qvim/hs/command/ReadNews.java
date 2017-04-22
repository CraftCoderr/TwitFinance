package com.qvim.hs.command;

import com.qvim.hs.HackathonServer;

/**
 * Created by RINES on 22.04.17.
 */
public class ReadNews extends AbstractCommand {

    ReadNews() {
        super("readnews", "reads news from twitter");
    }

    @Override
    public void run(String[] args) {
        if(args.length != 1) {
            notEnoughArgs("readnews <twitter account name>");
            return;
        }
        HackathonServer.getInstance().getTwitter().readNewsToFile(args[0]);
    }
}
