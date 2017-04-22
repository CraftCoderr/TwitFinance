package com.qvim.hs.news;

import com.qvim.hs.HackathonServer;
import com.qvim.hs.util.Crawler;
import com.qvim.hs.util.Scheduler;
import com.qvim.hs.util.configuration.FileConfiguration;
import com.qvim.hs.util.logger.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import twitter4j.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.*;

/**
 * Created by RINES on 22.04.17.
 */
public class NewYorkTimes {

    private final static Random RANDOM = new Random();
    private final static JSONParser PARSER = new JSONParser();
    private final static String URL = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private static String API_KEY;

    public static void parseEverything() {
        Logger.section("news parsing from NYT");
        FileConfiguration config = HackathonServer.getConfig("new_york_times");
        API_KEY = config.getString("api-key");
        List<News> news = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set(2016, 6, 20, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        parseEverything(news, calendar);
    }

    private static void parseEverything(List<News> news, Calendar calendar) {
        while(true) {
            if(calendar.get(Calendar.YEAR) == 2017 && calendar.get(Calendar.MONTH) == 3)
                break;
            news.addAll(getNews(calendar));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if(calendar.get(Calendar.DAY_OF_MONTH) % 10 == 0)
                break;
        }
        saveToFile(news);
        if(calendar.get(Calendar.DAY_OF_MONTH) % 10 == 0) {
            Scheduler.sleep(10000L);
            parseEverything(news, calendar);
        }
    }

    private static void saveToFile(List<News> news) {
        try {
            File target = new File("news/NYT201602.txt");
            if(!target.exists())
                target.createNewFile();
            try(PrintWriter pw = new PrintWriter(new FileWriter(target))) {
                for(News s : news) {
                    JSONObject json = new JSONObject();
                    json.put("text", s.getText());
                    json.put("author", s.getAuthor());
                    json.put("url", s.getUrl());
                    json.put("date", s.getDate());
                    json.put("rating", s.getRating());
                    pw.println(json.toJSONString());
                }
                pw.close();
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private static List<News> getNews(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH) + 1, day = calendar.get(Calendar.DAY_OF_MONTH);
        int sum = day + 100 * (month + 100 * year);
        try {
            List<News> result = new ArrayList<>();
            JSONObject json = (JSONObject) PARSER.parse(Crawler.executeGet(URL + "?api-key=" + API_KEY + "&begin_date=" + sum + "&end_date=" + sum));
            json = (JSONObject) json.get("response");
            JSONArray docs = (JSONArray) json.get("docs");
            for(int i = 0; i < docs.size(); ++i) {
                JSONObject doc = (JSONObject) docs.get(i);
                JSONObject header = (JSONObject) doc.get("headline");
                String date = (String) doc.get("pub_date");
                long millisDate = Instant.parse(date.replace("+0000", ".00Z")).toEpochMilli();
                result.add(new News((String) header.get("main"), "NewYorkTimes", millisDate, (String) doc.get("web_url"), RANDOM.nextInt(100)));
            }
            Logger.log("Got all news for %s!", calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH));
            Scheduler.sleep(1000L);
            return result;
        }catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
