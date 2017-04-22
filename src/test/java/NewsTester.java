import com.qvim.hs.HackathonServer;
import com.qvim.hs.Launcher;
import com.qvim.hs.news.News;
import com.qvim.hs.news.NewsCache;
import com.qvim.hs.util.TimestampUtil;
import com.qvim.hs.util.logger.Logger;

import java.util.Date;

/**
 * Created by RINES on 22.04.17.
 */
public class NewsTester {

    public static void main(String[] args) {
        Launcher.main(args);
        test();
        HackathonServer.getInstance().disable();
    }

    private static void test() {
        print(2016010100, 2017040100);
    }

    private static void print(int from, int to) {
        print(NewsCache.getNewsTimed(TimestampUtil.customTimestampToUnixtime(from), TimestampUtil.customTimestampToUnixtime(to), false), from, to);
    }

    private static void print(News[] news, int from, int to) {
        Logger.log("Data from %s to %s:", new Date(TimestampUtil.customTimestampToUnixtime(from)).toGMTString(), new Date(TimestampUtil.customTimestampToUnixtime(to)).toGMTString());
        if(news == null)
            Logger.log("DATA IS NULL");
        else
            for(News n : news)
                Logger.log(n.toString());
    }
}
