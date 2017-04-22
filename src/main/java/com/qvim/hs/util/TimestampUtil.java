package com.qvim.hs.util;

import java.util.*;

/**
 * Created by RINES on 21.04.17.
 */
public class TimestampUtil {

    /**
     * Преобразование java-unix-time'a в собственный формат хранения времени.
     * @param unix время в формате java-unix-time.
     * @return время в собственном формате хранения.
     */
    public static long unixtimeToCustomTimestamp(long unix) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(unix);
        return timeValuesToCustomTimestamp(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY));
    }

    /**
     * Преобразование времени, характеризуемого переданными параметрами, в собственный формат хранения.
     * @param year год.
     * @param month месяц (1-12).
     * @param day день (1-31).
     * @param hours часы (0-23).
     * @return время в собственном формате хранения.
     */
    public static long timeValuesToCustomTimestamp(int year, int month, int day, int hours) {
        return hours + 100 * (day + 100 * (month + 100L * year));
    }

    /**
     * Преобразование времени, хранимом в собственном формате, в java-unix-time формат.
     * @param timestamp время в собственном формате хранения.
     * @return время в java-unix-time формате.
     */
    public static long customTimestampToUnixtime(long timestamp) {
        long hours = timestamp % 100;
        timestamp /= 100;
        long day = timestamp % 100;
        timestamp /= 100;
        long month = timestamp % 100;
        timestamp /= 100;
        long year = timestamp;
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.set((int) year, (int) month - 1, (int) day, (int) hours, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
