package com.qvim.hs.news;

import com.qvim.hs.rates.TimedExchangeRate;
import com.qvim.hs.util.ScaleFactor;
import com.qvim.hs.util.logger.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by RINES on 22.04.17.
 */
public class NewsCache {

    private final static Random RANDOM = new Random();
    private final static JSONParser PARSER = new JSONParser();

    private static News[] NEWS = new News[1];
    private static int index = 0;

    private static News[] PEOPLE = new News[1];
    private static int indexP = 0;

    /**
     * Предзагружает новости.
     * Нужно использовать единожды.
     */
    public static void preloadNews() {
        Logger.section("news preloading");
        File directory = new File("news");
        for(File child : directory.listFiles())
            readNews(child);
        directory = new File("people");
        for(File child : directory.listFiles())
            readPeople(child);
    }

    /**
     * Получение новостей из заданного временного промежутка.
     * @param startTime начало промежутка в java-unix-time.
     * @param endTime конец промежутка в java-unix-time.
     * @param people получать новости от человека? (если false, то от СМИ)
     * @return null, если начальное время превосходит время последней новости в базе или превосходит конечное время;
     * массив подходящих новостей (возможно, пустой) в противном случае.
     */
    public static News[] getNewsTimed(long startTime, long endTime, boolean people) {
        News[] NEWS = people ? NewsCache.PEOPLE : NewsCache.NEWS;
        int start = Arrays.binarySearch(NEWS, new News(null, null, startTime, null, 0), (a, b) -> Long.compare(a.getDate(), b.getDate()));
        int end = Arrays.binarySearch(NEWS, new News(null, null, endTime, null, 0), (a, b) -> Long.compare(a.getDate(), b.getDate()));
        if(start < 0)
            start = -start;
        if(end < 0)
            end = -end;
        if(start >= NEWS.length) {
            if(end >= NEWS.length)
                return null;
            start = end;
        }else if(end >= NEWS.length)
            end = NEWS.length - 1;
        if(start > end)
            return null;
        News[] result = new News[end - start + 1];
        System.arraycopy(NEWS, start, result, 0, result.length);
        return result;
    }

    /**
     * Получение новостей из заданного промежутка времени, которые, скорее всего, привели к чему-то интересному
     * в поведении курса валют.
     * @param startTime начало промежутка в java-unix-time.
     * @param endTime конец промежутка в java-unix-time.
     * @param people получать новости от человека? (если false, то от СМИ)
     * @param rates массив котировок для заданного промежутка.
     * @param scale масштаб.
     * @return почти {@link NewsCache#getNewsTimed(long, long, boolean)}, но с условием того, что должен делать этот метод (:
     */
    public static News[] getInterestingNewsTimed(long startTime, long endTime, boolean people, TimedExchangeRate[] rates, ScaleFactor scale) {
        News[] result = getNewsTimed(startTime, endTime, people);
        if(result == null)
            return null;
        int radius;
        float acceptableDelta;
        switch(scale) {
            case HOUR:
                radius = 12;
                acceptableDelta = .0095f;
                break;
            case DAY:
                radius = 2;
                acceptableDelta = .02f;
                break;
            case WEEK:
                radius = 1;
                acceptableDelta = .035f;
                break;
            case MONTH:
                radius = 1;
                acceptableDelta = .05f;
                break;
            default:
                return null;
        }
        List<News> accepted = new ArrayList<>();
        for(News news : result)
            if(isNewsAcceptable(rates, news.getDate(), radius, acceptableDelta))
                accepted.add(news);
        rarefy(accepted);
        sieveDependingOnEquality(accepted);
        return accepted.toArray(new News[accepted.size()]);
    }

    private static void sieveDependingOnEquality(List<News> news) {
        int index = 0;
        while(index < news.size() - 1) {
            String current = news.get(index).getText().toLowerCase(), next = news.get(index + 1).getText().toLowerCase();
            double similarity = JaroWinkler.similarity(current, next);
            double treshold = 1 - 0.02 / 0.4f;
            if(similarity >= treshold)
                news.remove(index);
            else
                ++index;
        }
    }

    private static boolean isNewsAcceptable(TimedExchangeRate[] rates, long time, int radius, float acceptableDelta) {
        int index = Arrays.binarySearch(rates, new TimedExchangeRate(time), (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        if(index < 0)
            index = -index;
        if(index >= rates.length)
            return false;
        int min = Math.max(0, index - radius + 1);
        int max = Math.min(index + radius - 1, rates.length - 1);
        float minRate = 0f, maxRate = 0f, sum = 0f;
        for(index = min; index <= max; ++index) {
            float rate = rates[index].getRate();
            if(minRate == 0f || minRate > rate)
                minRate = rate;
            if(maxRate == 0f || maxRate < rate)
                maxRate = rate;
            sum += rate;
        }
        return (maxRate - minRate) / (sum / (max - min + 1)) >= acceptableDelta;
    }

    private static void rarefy(List<News> accepted) {
        final long HOUR = 1000L * 60 * 60;
        int index = 1;
        while(index < accepted.size() - 1) {
            News previous = accepted.get(index - 1), current = accepted.get(index), next = accepted.get(index + 1);
            if(current.getDate() - previous.getDate() < HOUR << 1 && next.getDate() - current.getDate() < HOUR << 1) {
                News retaining = previous.getRating() >= current.getRating() ?
                        previous.getRating() >= next.getRating() ?
                                previous :
                                next :
                        current.getRating() >= next.getRating() ?
                                current :
                                next;
//                News retaining = RANDOM.nextBoolean() ? previous : RANDOM.nextBoolean() ? current : next;
                if(previous == retaining) {
                    accepted.remove(index);
                    accepted.remove(index);
                }else if(retaining == current) {
                    accepted.remove(index - 1);
                    accepted.remove(index);
                }else if(retaining == next) {
                    accepted.remove(index - 1);
                    accepted.remove(index - 1);
                }
            }else
                ++index;
        }
    }

    private static void readPeople(File info) {
        read(info, true);
    }

    private static void read(File info, boolean people) {
        if(info.getName().startsWith("."))
            return;
        Logger.log("Reading news from file %s..", info.getName());
        try(BufferedReader reader = new BufferedReader(new FileReader(info))) {
            String line;
            while((line = reader.readLine()) != null) {
                JSONObject news = (JSONObject) PARSER.parse(line);
                if(people) {
                    if(indexP == PEOPLE.length) {
                        News[] temp = PEOPLE;
                        PEOPLE = new News[PEOPLE.length + 5000];
                        System.arraycopy(temp, 0, PEOPLE, 0, temp.length);
                    }
                }else {
                    if(index == NEWS.length) {
                        News[] temp = NEWS;
                        NEWS = new News[NEWS.length + 5000];
                        System.arraycopy(temp, 0, NEWS, 0, temp.length);
                    }
                }
                String text = (String) news.get("text");
                String author = (String) news.get("author");
                if(author == null || author.isEmpty())
                    author = "Unknown";
                String url = (String) news.get("url");
                if(url == null)
                    url = "#";
                long date = (long) news.get("date");
                int rating = (int) (long) news.get("rating");
                News created = new News(text, author, date, url, rating);
                if(people)
                    PEOPLE[indexP++] = created;
                else
                    NEWS[index++] = created;
            }
            if(people) {
                News[] temp = PEOPLE;
                PEOPLE = new News[indexP];
                System.arraycopy(temp, 0, PEOPLE, 0, PEOPLE.length);
                Arrays.sort(PEOPLE, (a, b) -> Long.compare(a.getDate(), b.getDate()));
            }else {
                News[] temp = NEWS;
                NEWS = new News[index];
                System.arraycopy(temp, 0, NEWS, 0, NEWS.length);
                Arrays.sort(NEWS, (a, b) -> Long.compare(a.getDate(), b.getDate()));
            }
            Logger.log("Done with this file!");
        }catch(Exception ex) {
            Logger.warn("Can not parse news from this file!", ex);
        }
    }

    private static void readNews(File info) {
        read(info, false);
    }

}
