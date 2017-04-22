package com.qvim.hs.command;

import com.qvim.hs.news.NewYorkTimes;

/**
 * Created by RINES on 22.04.17.
 */
public class NYT extends AbstractCommand {

    NYT() {
        super("nyt", "parses news from new york times api");
    }

    @Override
    public void run(String[] args) {
        NewYorkTimes.parseEverything();
    }
}
