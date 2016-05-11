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

    public RecSysEngine() throws TasteException, IOException  {

    }

    public void initRecommender() throws TasteException, IOException {

        DataModel datamodel = new FileDataModel(new File("C:\\Users\\tansh\\IdeaProjects\\Visual Music Recommender\\src\\main\\resources\\user_artists.dat")); //data

        //Creating UserSimilarity object.
//        UserSimilarity usersimilarity = new PearsonCorrelationSimilarity(datamodel);
        ItemSimilarity similarity = new LogLikelihoodSimilarity(datamodel);

        //Creating UserNeighbourHHood object.
//        UserNeighborhood userneighborhood = new ThresholdUserNeighborhood(1.0, usersimilarity, datamodel);

        //Create UserRecomender
//        UserBasedRecommender recommender = new GenericUserBasedRecommender(datamodel, userneighborhood, usersimilarity);
        ItemBasedRecommender recommender = new GenericItemBasedRecommender(datamodel, similarity);

        List<RecommendedItem> recommendations = recommender.recommend(2, 5);
        for (RecommendedItem recommendation : recommendations) {
            System.out.println(recommendation);
        }
    }
}