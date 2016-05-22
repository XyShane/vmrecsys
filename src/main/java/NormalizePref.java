import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Xyline on 20/05/2016.
 */
public class NormalizePref {
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
        int incrementUser = 0;
        Map<String,Double> map = new HashMap<>();

        while((line = br.readLine()) != null){
            data = line.split("\t");
            if(currentUser.equals(data[0])){
                map.put(data[1],Double.parseDouble(data[2]));
            } else if(data.length == 0){
                // Checks for empty lines in dataset
                continue;
            } else {
                if(map.size() == 0){
                    // Nothing in the map.
                    currentUser = data[0];
                    map.put(data[1],Double.parseDouble(data[2]));
                } else {
                    for (Map.Entry<String, Double> entry : normalizePreferences(map).entrySet()) {
                        fw.append(incrementUser + "\t" + entry.getKey() + "\t" + entry.getValue() + "\n");
                    }
                    map = new HashMap<>();
                    incrementUser++;
                    currentUser = String.valueOf(incrementUser);
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
