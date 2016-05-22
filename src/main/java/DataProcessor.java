import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Xyline on 15/05/2016.
 */
public class DataProcessor {
    private LastFMAPIHandler lfm;
    public DataProcessor(LastFMAPIHandler lfm){
        this.lfm = lfm;
    }


    public void getInformation(Recommender recommender,long userid, int noRec, File cfile, int dataType, boolean firstLine, int idIndex, String splitBy, int contentIndex) throws TasteException, IOException {

        String[] rec;
        ArrayList<String> musicInfo = new ArrayList<>();
        System.out.println("################################################");
        for(RecommendedItem recommendations: recommender.recommend(userid,noRec)) {
            rec = contentExtract(recommendations,cfile,firstLine, idIndex, splitBy, contentIndex);
            musicInfo.add(rec[0]);
            if(dataType == 1) {
                Artist artist = Artist.getInfo(rec[0], lfm.getApiKey());
                System.out.println("Artist: " + rec[0] + " | Value: " + rec[1]);
                System.out.println(artist);
            }else if(dataType == 2){
                Collection<Album> albums = Album.search(rec[0],lfm.getApiKey());
                for(Album album : albums){
                    System.out.println(album);
                    System.out.println("Album: " + album + " | Value: " + rec[1]);
                    break;
                }
            } else {
                System.out.println(recommendations);
            }
        }
        System.out.println("################################################");
    }
    public String[] contentExtract(RecommendedItem item, File file, boolean firstLine, int idIndex, String splitBy, int contentIndex) throws IOException {
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(file));
        String[] data;
        String resultData[] = new String[2];
        String[] defaultData = {"Data not found.","Please check if the file is valid."};
        while((line = br.readLine()) != null){
            data = line.split(splitBy);
            if(firstLine){
                firstLine = false;
                continue;
            } else {
                if (Long.parseLong(data[idIndex]) == (item.getItemID())){
                    resultData[0] = data[contentIndex];
                    resultData[1] = "" + item.getValue();
                    return resultData;
                }
            }
        }
        return defaultData;
    }

}
