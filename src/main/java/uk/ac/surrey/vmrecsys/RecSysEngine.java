package uk.ac.surrey.vmrecsys;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xyline on 05/05/2016.
 */
@Component
public class RecSysEngine {

    private Recommender recommender = null;

    public RecSysEngine() throws TasteException, IOException {
    }

    @PostConstruct
    public void initRecommender() throws TasteException, IOException {
        System.out.println("Constructed!");
        DataModel datamodel = new FileDataModel(new File("C:\\Users\\tansh\\Desktop\\Final Year Project\\Project\\Visual Music Rec Web\\src\\main\\resources\\datasets\\hetrec\\user_artists.dat")); //data

        ItemSimilarity similarity = new LogLikelihoodSimilarity(datamodel);

        recommender = new GenericItemBasedRecommender(datamodel, similarity);

    }

    public List<RecommendedItem> recommendArtists(String person, int recNo) throws TasteException {

        Long pr = Long.parseLong(person);

        List<RecommendedItem> recommendations = this.recommender.recommend(pr, recNo);

        return recommendations;
    }

    public String[] findArtistById (RecommendedItem item) throws IOException {
        File file = new File("C:\\Users\\tansh\\Desktop\\Final Year Project\\Project\\Visual Music Rec Web\\src\\main\\resources\\datasets\\hetrec\\artists.dat");
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(file));
        boolean firstLine = true;
        String[] data;
        String[] defaultData = {"Data not found.","Sadly.."};
        System.out.println(item.getItemID());

        while((line = br.readLine()) != null){
            data = line.split("\t");
            if(firstLine){
                firstLine = false;
                continue;
            } else {
                if (Long.parseLong(data[0]) == (item.getItemID())){
                    System.out.println("Found");
                    return data;
                }
            }
        }
        return defaultData;
    }
}