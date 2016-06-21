package uk.ac.surrey.com3001.mrecsys.recommenders.implementation;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.IOException;
import java.util.List;

/**
 * Created by Xyline on 20/05/2016.
 */
public interface MixedHybridRecommender extends Recommender {
    void normalizeAllValues(List<RecommendedItemImpl> toNormalize, float biasFactor);
    void initializeRecommender(Long user, int noOfVal, IDRescorer idRescorer) throws TasteException, IOException;
}
