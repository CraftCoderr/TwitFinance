package com.qvim.hs.news;

import com.qvim.hs.HackathonServer;
import com.qvim.hs.util.configuration.FileConfiguration;
import org.json.simple.JSONObject;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by RINES on 22.04.17.
 */
public class Twitter4J {

    private final Twitter twitter;

    public Twitter4J() {
        FileConfiguration config = HackathonServer.getConfig("twitter");
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(config.getString("consumer_key"))
                .setOAuthConsumerSecret(config.getString("consumer_secret"))
                .setOAuthAccessToken(config.getString("access_token"))
                .setOAuthAccessTokenSecret(config.getString("access_token_secret"));
        TwitterFactory tf = new TwitterFactory(cb.build());
        this.twitter = tf.getInstance();
    }

    /**
     * Считывает все твиты указанного пользователя твиттера в специальный файл, подразумевая, что каждый твит есть
     * ни что иное, как новость.
     * @param username ник пользователя в твиттере.
     */
    public void readNewsToFile(String username) {
        com.qvim.hs.util.logger.Logger.log("Reading news to file from twitter account %s..", username);
        try {
            List<Status> result = getStatuses(username);
            File target = new File("news/" + username + ".txt");
            if(!target.exists())
                target.createNewFile();
            else
                com.qvim.hs.util.logger.Logger.warn("File news/%s.txt already exists!", username);
            try(PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for(Status s : result) {
                    JSONObject json = new JSONObject();
                    json.put("author", username);
                    String[] spl = s.getText().split(" ");
                    String url = spl[spl.length - 1];
                    if(!url.startsWith("https://t.co/")) {
                        json.put("text", s.getText());
                        url = "";
                    }else {
                        StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < spl.length - 1; ++i)
                            if(spl[i].startsWith("https://t.co/"))
                                break;
                            else
                                sb.append(spl[i]).append(" ");
                        json.put("text", sb.toString().trim());
                    }
                    json.put("url", url);
                    json.put("date", s.getCreatedAt().getTime());
                    json.put("rating", s.getRetweetCount() * 3 + s.getFavoriteCount());
                    pw.println(json.toJSONString());
                }
                pw.close();
            }
        }catch(Exception ex) {
            com.qvim.hs.util.logger.Logger.warn("Can not read news from account " + username + " to file!", ex);
        }
    }

    /**
     * Получение всех доступных с помощью API статусов (твитов) пользователя в твиттере (всего их около 3200-3500).
     * @param username ник пользователя в твиттере.
     */
    public List<Status> getStatuses(String username) {
        try {
            List<Status> result = new ArrayList<>();
            result.addAll(this.twitter.getUserTimeline(username, new Paging().count(200)));
            long since = result.get(result.size() - 1).getId();
            for(int i = 0; i < 17; ++i) {
                result.addAll(getStatuses(username, since));
                long newSince = result.get(result.size() - 1).getId();
                if(newSince >= since)
                    break;
                since = newSince;
            }
            return result;
        }catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private List<Status> getStatuses(String username, long since) throws TwitterException {
        com.qvim.hs.util.logger.Logger.log("Collecting since %d..", since);
        return this.twitter.getUserTimeline(username, new Paging().count(200).maxId(since));
    }

}
