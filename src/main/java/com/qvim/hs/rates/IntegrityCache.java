package com.qvim.hs.rates;

import com.qvim.hs.util.ScaleFactor;
import com.qvim.hs.util.TimestampUtil;
import com.qvim.hs.util.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;

/**
 * Created by RINES on 21.04.17.
 */
public class IntegrityCache {

    private final static Map<Currency, Map<Currency, TimedExchangeRate[]>> EXCHANGE_RATES = new HashMap<>();

    /**
     * Получение курса валюты на всем имеющемся временном промежутке.
     * @param target курс какой валюты?
     * @param to к какой валюте?
     * @return null, если у нас нет информации по курсу target к to, иначе массив TimedExchangeRate (возможно, пустой).
     */
    public static TimedExchangeRate[] getExchangeRate(Currency target, Currency to) {
        Map<Currency, TimedExchangeRate[]> rates = EXCHANGE_RATES.get(target);
        if(rates == null)
            return null;
        return rates.get(to);
    }

    /**
     * Для использования в тестах. И только.
     */
    public static TimedExchangeRate[] getExchangeRateTimedByCustomTimestamp(Currency target, Currency to, int startDate, int endDate, ScaleFactor scale) {
        return getExchangeRateTimedScaled(target, to, TimestampUtil.customTimestampToUnixtime(startDate), TimestampUtil.customTimestampToUnixtime(endDate), scale);
    }

    /**
     * Получение курса валюты в заданном историческом промежутке.
     * @param target курс какой валюты?
     * @param to к какой валюте?
     * @param startDate первоначальная дата в java-unix-time.
     * @param endDate конечная дата в java-unix-time.
     * @return null, если у нас нет информации по курсу target к to, если время начала промежутка больше времени
     * последней записи в нашей базе по требуемому курсу или если время конца меньше времени начала;
     * в противном случае массив удовлетворяющих TimedExchangeRate.
     */
    public static TimedExchangeRate[] getExchangeRateTimed(Currency target, Currency to, long startDate, long endDate) {
        TimedExchangeRate[] rates = getExchangeRate(target, to);
        if(rates == null)
            return null;
        int start = Arrays.binarySearch(rates, new TimedExchangeRate(startDate), (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        int end = Arrays.binarySearch(rates, new TimedExchangeRate(endDate), (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        if(start < 0)
            start = -start;
        if(end < 0)
            end = -end;
        if(start >= rates.length) {
            if(end >= rates.length)
                return null;
            start = end;
        }else if(end >= rates.length)
            end = rates.length - 1;
        if(start > end)
            return null;
        TimedExchangeRate[] result = new TimedExchangeRate[end - start + 1];
        System.arraycopy(rates, start, result, 0, result.length);
        return result;
    }

    /**
     * @see IntegrityCache#getExchangeRateTimed(Currency, Currency, long, long)
     * @param target курс какой валюты?
     * @param to к какой валюте?
     * @param startDate первоначальная дата в java-unix-time.
     * @param endDate конечная дата в java-unix-time.
     * @param scale масштаб (минимальное время между двумя соседними TimedExchangeRate в возвращаемом массиве).
     * @return аналогично методу, указанному в @see, за исключением факта учета масштаба (здесь - учитывается).
     */
    public static TimedExchangeRate[] getExchangeRateTimedScaled(Currency target, Currency to, long startDate, long endDate, ScaleFactor scale) {
        TimedExchangeRate[] rates = getExchangeRateTimed(target, to, startDate, endDate);
        if(rates == null)
            return null;
        List<TimedExchangeRate> list = new ArrayList<>();
        TimedExchangeRate previous = null;
        BiFunction<TimedExchangeRate, TimedExchangeRate, Boolean> acceptor;
        switch(scale) {
            case HOUR:
                acceptor = (a, b) -> true;
                break;
            case DAY:
                acceptor = (a, b) -> a.getTimestampDay() != b.getTimestampDay() || a.getTimestampMonth() != b.getTimestampMonth();
                break;
            case WEEK:
                acceptor = (a, b) -> b.subtract(a) > 1000L * 60 * 60 * 24 * 7;
                break;
            case MONTH:
                acceptor = (a, b) -> a.getTimestampMonth() != b.getTimestampMonth() || a.getTimestampYear() != b.getTimestampYear();
                break;
            default:
                throw new IllegalArgumentException();
        }
        for(TimedExchangeRate rate : rates)
            if(previous == null || acceptor.apply(previous, rate)) {
                list.add(rate);
                previous = rate;
            }
        rates = null;
        return list.toArray(new TimedExchangeRate[list.size()]);
    }

    /**
     * Предзагрузка курсов валют.
     * Использовать единожды.
     */
    public static void preloadExchangeRates() {
        Logger.section("exchange rates preloading");
        File infoDir = new File("exchange_rates");
        for(File child : infoDir.listFiles()) {
            String[] args = child.getName().split(" ");
            if(args.length != 3) {
                Logger.log("Ignoring er-info file named %s", child.getName());
                continue;
            }
            String target = args[0], to = args[1];
            try {
                Currency ctarget = Currency.valueOf(target.toUpperCase());
                Currency cto = Currency.valueOf(to.toUpperCase());
                parseFile(ctarget, cto, child);
            }catch(Throwable t) {
                Logger.log("Ignored er-info file named %s due to unknown (target/to) currency type.", child.getName());
                t.printStackTrace();
                continue;
            }
        }
        EXCHANGE_RATES.values().stream().flatMap(map -> map.values().stream()).forEach(array -> Arrays.sort(array, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp())));
    }

    private static void parseFile(Currency target, Currency to, File info) {
        Logger.log("Parsing file %s", info.getName());
        int index = 0;
        TimedExchangeRate[] rates = new TimedExchangeRate[15000];
        try(BufferedReader reader = new BufferedReader(new FileReader(info))) {
            String line;
            while((line = reader.readLine()) != null) {
                StringBuilder sb = new StringBuilder();
                int year = 0, month = 0, day = 0, hour = 0;
                float rate = 0f;
                char[] chars = line.toCharArray();
                line = null; //to free memory
                for(int i = 1; i <= chars.length; ++i) {
                    char c = chars[i - 1];
                    if(c == ' ' || i >= 12 && i <= 16)
                        continue;
                    sb.append(c);
                    if(i == 4) {
                        year = i(sb);
                        sb = new StringBuilder();
                    }else if(i == 6) {
                        month = i(sb);
                        sb = new StringBuilder();
                    }else if(i == 8) {
                        day = i(sb);
                        sb = new StringBuilder();
                    }else if(i == 11) {
                        hour = i(sb);
                        sb = new StringBuilder();
                    }else if(i == 24) {
                        rate = Float.parseFloat(sb.toString());
                        break;
                    }
                }
                if(index != 0) {
                    TimedExchangeRate previous = rates[index - 1];
                    if(previous.checkEquality(year, month, day, hour)) {
                        previous.count(rate);
                        continue;
                    }
                }
                rates[index++] = new TimedExchangeRate(year, month, day, hour, rate);
            }
        }catch(IOException ex) {
            Logger.warn("Can not parse file due to unexpected exception being thrown!", ex);
            return;
        }
        TimedExchangeRate[] temp = new TimedExchangeRate[index];
        System.arraycopy(rates, 0, temp, 0, index);
        rates = temp;
        temp = null;
        Map<Currency, TimedExchangeRate[]> ratesMap = EXCHANGE_RATES.get(target);
        if(ratesMap == null) {
            ratesMap = new HashMap<>();
            EXCHANGE_RATES.put(target, ratesMap);
        }
        temp = ratesMap.get(to);
        if(temp == null) {
            ratesMap.put(to, rates);
            return;
        }
        TimedExchangeRate[] combined = new TimedExchangeRate[temp.length + rates.length];
        System.arraycopy(temp, 0, combined, 0, temp.length);
        System.arraycopy(rates, 0, combined, temp.length, rates.length);
        ratesMap.put(to, combined);
    }

    private static int i(StringBuilder sb) {
        return Integer.parseInt(sb.toString());
    }

}
