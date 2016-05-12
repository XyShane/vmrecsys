package uk.ac.surrey.vmrecsys;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xyline on 18/04/2016.
 */
@RestController
public class MainClass {

    @Autowired
    private RecSysEngine engine;

    @RequestMapping("/rec")
    public List<RecommendedItem> greeting(@RequestParam(value = "person") String person, @RequestParam(value = "number") int number) throws IOException, TasteException {

//        Caller.getInstance().setUserAgent("tst");
//        String key = "4c9ae349ba011cd1ef2ecc315d4efa13"; //this is the key used in the Last.fm API examples
//        String user = "SynerC";
//        String password = "..."; // user's password
//        String secret = "...";   // api secret
////        Session session = Authenticator.getMobileSession(user, password, key, secret);
////        Playlist playlist = Playlist.create("example playlist", "description", session);
//        Chart<Artist> chart = User.getWeeklyArtistChart(user, 10, key);
//        DateFormat format = DateFormat.getDateInstance();
//        String from = format.format(chart.getFrom());
//        String to = format.format(chart.getTo());
//        System.out.printf("Charts for %s for the week from %s to %s:%n", user, from, to);
//        System.out.println(Album.getInfo("","21b03748-3bb5-4b24-8e7d-14e47eee80ca",key));
//        System.out.println(Track.getInfo("","2625c24d-0aaa-4dcf-b616-3f27a78fe4f9",key));
//        List<String> trackInfo =
//            List<String> recommendations = new ArrayList<String>();
//            try {
//                for(RecommendedItem item : engine.recommendArtists(person, number)) {
//                    recommendations.add(String.valueOf(item.getItemID()));
//                }
//            } catch (TasteException e) {
//                System.out.print("failed");
//                e.printStackTrace();
//                throw e;
//            }
//        for (String item: re.recommendThings("7")) {
//
//        }
        return engine.recommendArtists(person, number);
    }

    @RequestMapping("/showArtistInfo")
    public ArrayList<String[]> showArtistsInfo(@RequestParam(value = "person") String person, @RequestParam(value = "number") int number) throws IOException, TasteException {
        ArrayList<String[]> recommendedStuff = new ArrayList<>();

        for (RecommendedItem item : engine.recommendArtists(person, number)) {
            recommendedStuff.add(engine.findArtistById(item));
        }

        return recommendedStuff;

    }
}
