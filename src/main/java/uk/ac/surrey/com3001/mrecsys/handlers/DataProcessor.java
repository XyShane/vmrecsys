package uk.ac.surrey.com3001.mrecsys.handlers;

import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;
import org.apache.commons.io.FilenameUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import uk.ac.surrey.com3001.mrecsys.MainClass;

import java.io.*;
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

    public static void prepareNormalize(File file, Scanner sc) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(file));
        String baseUrl = FilenameUtils.getFullPath(file.getAbsolutePath());
        String normURL = baseUrl + FilenameUtils.getBaseName(file.getName()) + "_norm." + FilenameUtils.getExtension(file.getPath());
        File normFile = new File(normURL);
        String cmd;

        // Checks if the normalized version created by this application exists or not.
        if(normFile.exists() && !normFile.isDirectory()) {
            boolean isNotValidAnswer = true;
            System.out.println("Apparently, a normalized file of your dataset already exists.");
            System.out.println("Do you want to overwrite your file? 'N' will take you back to main menu.(Y/N)");
            cmd = sc.nextLine();
            while (isNotValidAnswer) {
                if (cmd.toLowerCase().equals("y")) {
                    isNotValidAnswer = false;
                    normFile.delete();
                    normFile = new File(normURL);
                    normFile.createNewFile();
                } else if (cmd.toLowerCase().equals("n")) {
                    isNotValidAnswer = false;
                    MainClass.mainMenu(sc);
                } else {
                    System.out.println("Sorry! That was an invalid choice.");
                    System.out.println("Do you want to overwrite your file? 'N' will take you back to main menu.(Y/N)");
                    cmd = sc.nextLine();
                }
            }
        }

        FileWriter fw = new FileWriter(normFile);

        String line = "";
        String[] data;
        String currentUser = "0";
        Map<String,Double> map = new HashMap<>();

        while((line = br.readLine()) != null){
            data = line.split("\t");
            if(currentUser.equals(data[0])){
                map.put(data[1],Double.parseDouble(data[2]));
            } else {
                if(data[0] == null){
                    // Nothing in the map.
                    continue;
                } else {
                    for (Map.Entry<String, Double> entry : normalizePreferences(map).entrySet()) {
                        fw.append(data[0] + "\t" + entry.getKey() + "\t" + entry.getValue() + "\n");
                    }
                    map = new HashMap<>();
                    currentUser = String.valueOf(data[0]);
                }

            }
        }
        System.out.println("Preferences normalized!");
        fw.flush();
        fw.close();
    }

    private static Map<String,Double> normalizePreferences(Map<String, Double> map){
        Map<String, Double> rMap = new HashMap<>();
        double highestVal = 0;
        double lowestVal = Double.MAX_VALUE;

        // Get the highest and lowest value
        for(Map.Entry<String,Double> entry : map.entrySet()){
            if(entry.getValue() > highestVal){
                highestVal = entry.getValue();
            } else if(entry.getValue() < lowestVal){
                lowestVal = entry.getValue();
            }
        }
        for (Map.Entry<String, Double> entry: map.entrySet()) {
            double val;
            double nVal;
            val = ((entry.getValue()- lowestVal)/(highestVal-lowestVal));
            nVal = val * (5-1) + 1;
            double roundOff = Math.round(nVal * 100.0) / 100.0;
            if(roundOff < 1){
                roundOff = 1.00;
            }
            rMap.put(entry.getKey(), roundOff);
        }
        return rMap;
    }



}
