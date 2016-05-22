import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * Created by Xyline on 15/05/2016.
 */
public class SVDMatrixRecommender implements RecommenderBuilder {

    Factorizer factorizer;

    public SVDMatrixRecommender(Factorizer factorizer){
        this.factorizer = factorizer;
    }

    @Override
    public Recommender buildRecommender(DataModel dataModel) throws TasteException {
        return new SVDRecommender(dataModel, this.factorizer);
    }
}
