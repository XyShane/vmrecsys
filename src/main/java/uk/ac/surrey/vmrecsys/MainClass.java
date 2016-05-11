package uk.ac.surrey.vmrecsys;

import de.umass.lastfm.*;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.List;

/**
 * Created by Xyline on 18/04/2016.
 */
@RestController
public class MainClass {
    @Autowired
    private RecEngine engine;

    @RequestMapping("/rec")
    public List<String> greeting(@RequestParam(value = "person") String person) throws IOException, TasteException {

        Caller.getInstance().setUserAgent("tst");
        String key = "4c9ae349ba011cd1ef2ecc315d4efa13"; //this is the key used in the Last.fm API examples
        String user = "SynerC";
        String password = "..."; // user's password
        String secret = "...";   // api secret
//        Session session = Authenticator.getMobileSession(user, password, key, secret);
//        Playlist playlist = Playlist.create("example playlist", "description", session);
        Chart<Artist> chart = User.getWeeklyArtistChart(user, 10, key);
        DateFormat format = DateFormat.getDateInstance();
        String from = format.format(chart.getFrom());
        String to = format.format(chart.getTo());
        System.out.printf("Charts for %s for the week from %s to %s:%n", user, from, to);
//        System.out.println(Album.getInfo("","21b03748-3bb5-4b24-8e7d-14e47eee80ca",key));
//        System.out.println(Track.getInfo("","2625c24d-0aaa-4dcf-b616-3f27a78fe4f9",key));
//        List<String> trackInfo =
//        for (String item: re.recommendThings("7")) {
//
//        }
        return engine.recommendThings(person);
    }
}
