package com.qvim.hs.rates;

import com.qvim.hs.util.TimestampUtil;
import lombok.Getter;

/**
 * Created by RINES on 21.04.17.
 */
public class TimedExchangeRate {

    /**
     * Временная метка в формате YYYYMMDDHH, то есть десятизначное число.
     */
    @Getter
    private final long timestamp;

    @Getter
    private float rate;

    private int counted = 1;

    TimedExchangeRate(int year, int month, int day, int hour, float rate) {
        this.timestamp = TimestampUtil.timeValuesToCustomTimestamp(year, month, day, hour);
        this.rate = rate;
    }

    public TimedExchangeRate(long unix) {
        this.timestamp = TimestampUtil.unixtimeToCustomTimestamp(unix);
        this.rate = 0f;
    }

    /**
     * Без сдвигов.
     */
    public int getTimestampYear() {
        return (int) ((this.timestamp / 1000000) % 10000);
    }

    /**
     * От 1 до 12 включительно.
     */
    public int getTimestampMonth() {
        return (int) ((this.timestamp / 10000) % 100);
    }

    /**
     * От 1 до 31 включительно.
     */
    public int getTimestampDay() {
        return (int) ((this.timestamp / 100) % 100);
    }

    /**
     * От 0 до 23 включительно.
     */
    public int getTimestampHours() {
        return (int) (this.timestamp % 100);
    }

    public boolean checkEquality(int year, int month, int day, int hour) {
        return TimestampUtil.timeValuesToCustomTimestamp(year, month, day, hour) == this.timestamp;
    }

    public void count(float rate) {
        rate = this.rate * this.counted + rate;
        this.rate = rate / ++counted;
    }

    /**
     * Вычитает из метки времени данного объекта метку времени переданного, возвращает всё в java-unix-time.
     * @return subtraction in java-unix-time.
     */
    public long subtract(TimedExchangeRate rate) {
        return TimestampUtil.customTimestampToUnixtime(this.timestamp) - TimestampUtil.customTimestampToUnixtime(rate.timestamp);
    }

    @Override
    public String toString() {
        return this.timestamp + " " + this.rate;
    }

}
