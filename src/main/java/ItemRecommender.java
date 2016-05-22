import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

/**
 * Created by Xyline on 15/05/2016.
 */
public class ItemRecommender implements RecommenderBuilder{

    private ItemSimilarity similarity;

    public ItemRecommender(ItemSimilarity similarity){
        this.similarity = similarity;
    }

    @Override
    public Recommender buildRecommender(DataModel dataModel) throws TasteException {
        return new GenericItemBasedRecommender(dataModel,this.similarity);
    }

}
