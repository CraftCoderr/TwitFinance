import com.qvim.hs.HackathonServer;
import com.qvim.hs.Launcher;
import com.qvim.hs.util.Crawler;
import com.qvim.hs.util.logger.Logger;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by RINES on 21.04.17.
 */
public class ExchangeRateDownloader {

    public static void main(String[] args) {
        Launcher.main(null);
        Calendar calendar = new GregorianCalendar();
        calendar.set(2000, 0, 1);
        getDataFor(calendar);
        HackathonServer.getInstance().disable();
    }

    private static String getDataFor(Calendar calendar) {
        String url = format(calendar);
        String data = Crawler.executeGet("http://api.fixer.io/" + url);
        Logger.log("Data for %s: %s", url, data);
        return data;
    }

    private static String format(Calendar c) {
        return String.format("%d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

}
