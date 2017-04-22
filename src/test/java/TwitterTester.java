import com.qvim.hs.HackathonServer;
import com.qvim.hs.Launcher;
import twitter4j.Status;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by RINES on 22.04.17.
 */
public class TwitterTester {

    public static void main(String[] args) {
        Launcher.main(args);
        HackathonServer.getInstance().getTwitter().readNewsToFile("ftfinancenews");
        HackathonServer.getInstance().disable();
    }

    private static void save(String username) {
        List<Status> statuses = HackathonServer.getInstance().getTwitter().getStatuses(username);
        try {
            File f = new File(username + ".txt");
            if(!f.exists())
                f.createNewFile();
            try(PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                statuses.forEach(s -> pw.println(String.format("\"%s\" CREATED: %d, RETWEETS: %d, LIKES: %d, ID: %d", s.getText(), s.getCreatedAt().getTime(), s.getRetweetCount(), s.getFavoriteCount(), s.getId())));
                pw.close();
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
