package uk.ac.surrey.com3001.mrecsys.recommenders.builder;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * Created by Xyline on 15/05/2016.
 */
public class UserNeighborhoodRecommender implements RecommenderBuilder {

    private UserSimilarity similarity;
    private UserNeighborhood neighborhood;

    public UserNeighborhoodRecommender(UserSimilarity similarity, UserNeighborhood neighborhood){
        this.similarity = similarity;
        this.neighborhood = neighborhood;
    }

    @Override
    public Recommender buildRecommender(DataModel dataModel) throws TasteException {
        return new GenericUserBasedRecommender(dataModel,neighborhood,similarity);
    }
}
