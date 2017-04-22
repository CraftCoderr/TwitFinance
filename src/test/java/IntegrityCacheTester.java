import com.qvim.hs.HackathonServer;
import com.qvim.hs.Launcher;
import com.qvim.hs.rates.Currency;
import com.qvim.hs.rates.IntegrityCache;
import com.qvim.hs.rates.TimedExchangeRate;
import com.qvim.hs.util.ScaleFactor;
import com.qvim.hs.util.TimestampUtil;
import com.qvim.hs.util.logger.Logger;

import java.util.Date;

/**
 * Created by RINES on 21.04.17.
 */
public class IntegrityCacheTester {

    public static void main(String[] args) {
        Launcher.main(args);
        test();
        HackathonServer.getInstance().disable();
    }

    private static void test() {
//        testTime(2014010300, 1388707200000L);
//        testTime(2015040715, 1428418800000L);
//        print(2014010100, 2014020100);
        print(2016010319, 2016080713);
    }

    private static void testTime(long custom, long unix) {
        long custom2 = TimestampUtil.unixtimeToCustomTimestamp(unix);
        long unix2 = TimestampUtil.customTimestampToUnixtime(custom);
        if(unix2 == unix && custom2 == custom)
            return;
        Logger.log("%d -> unix: %d", custom, unix2);
        Logger.log("%d -> custom: %d", unix, custom2);
    }

    private static void print(int from, int to) {
        print(IntegrityCache.getExchangeRateTimedByCustomTimestamp(Currency.EUR, Currency.USD, from, to, ScaleFactor.MONTH), from, to);
    }

    private static void print(TimedExchangeRate[] rates, int from, int to) {
        Logger.log("Data from %s to %s:", new Date(TimestampUtil.customTimestampToUnixtime(from)).toGMTString(), new Date(TimestampUtil.customTimestampToUnixtime(to)).toGMTString());
        if(rates == null)
            Logger.log("DATA IS NULL");
        else
            for(TimedExchangeRate rate : rates)
                Logger.log(rate.toString());
    }

}
