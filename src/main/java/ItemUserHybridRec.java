import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

/**
 * Created by Xyline on 20/05/2016.
 */
public class ItemUserHybridRec implements RecommenderBuilder {

    private Recommender x;
    private Recommender y;

    public ItemUserHybridRec(Recommender x, Recommender y){
        this.x = x;
        this.y = y;
    }

    @Override
    public Recommender buildRecommender(DataModel dataModel) throws TasteException {
        return new HybridDoubleRecommender(dataModel,x,y);
    }
}
