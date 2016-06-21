package uk.ac.surrey.com3001.mrecsys.recommenders.builder;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import uk.ac.surrey.com3001.mrecsys.recommenders.implementation.CascadingMixedHybridRecommender;

/**
 * Created by Xyline on 20/05/2016.
 */
public class CMItemUserHybridRec implements RecommenderBuilder {

    private Recommender x;
    private Recommender y;

    public CMItemUserHybridRec(Recommender x, Recommender y){
        this.x = x;
        this.y = y;
    }

    @Override
    public Recommender buildRecommender(DataModel dataModel) throws TasteException {
        return new CascadingMixedHybridRecommender(dataModel,x,y);
    }
}
