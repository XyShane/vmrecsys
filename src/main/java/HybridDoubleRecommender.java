import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Xyline on 20/05/2016.
 */
public class HybridDoubleRecommender extends AbstractRecommender implements HybridRecommender {

    private DataModel dataModel;

    private final Recommender x;
    private final Recommender y;
    private ArrayList<RecommendedItemImpl> zList;

    public HybridDoubleRecommender(DataModel dataModel, Recommender x, Recommender y) {
        super(dataModel);
        this.x = x;
        this.y = y;
    }

    @Override
    public void normalizeAllValues(List<RecommendedItemImpl> toNormalize, float biasFactor) {
        float max = Collections.max(toNormalize).getValue();
        float min = Collections.min(toNormalize).getValue();

        for(RecommendedItemImpl item : toNormalize){
            item.normalizeValue(min,max,biasFactor);
        }
    }

    @Override
    public void initializeRecommender(Long user, int noOfVal, IDRescorer idRescorer) throws TasteException, IOException {
        float upperBias = 7;
        float lowerBias = 3;
        float biasFactor = 10;

        List<RecommendedItemImpl> xList = new ArrayList<>();
        List<RecommendedItemImpl> yList = new ArrayList<>();

        zList = new ArrayList<>();

        xList.addAll(x.recommend(user, noOfVal, idRescorer).stream().map(RecommendedItemImpl::new).collect(Collectors.toList()));
        yList.addAll(y.recommend(user, noOfVal, idRescorer).stream().map(RecommendedItemImpl::new).collect(Collectors.toList()));

        normalizeAllValues(xList,upperBias/biasFactor);
        normalizeAllValues(yList,lowerBias/biasFactor);

        zList.addAll(xList);
        zList.addAll(yList);
    }

    @Override
    public List<RecommendedItem> recommend(long l, int no, IDRescorer idRescorer) throws TasteException {
        try {
            initializeRecommender(l,no,idRescorer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<RecommendedItem> recommendations = new ArrayList<>();

        Collections.sort(zList);

        for(int i = 0; i < no; i++){
            recommendations.add(zList.get(i));
        }
        return recommendations;
    }

    @Override
    public float estimatePreference(long l, long l1) throws TasteException {
        float xResult = x.estimatePreference(l,l1);
        float yResult = y.estimatePreference(l,l1);

        if(Float.isNaN(xResult)) {
            xResult = 1;
        }
        if(Float.isNaN(yResult)){
            yResult = 1;
        }
        float average = (xResult + yResult)/2;
        float roundOff = (float) (Math.round(average * 100.0) / 100.0);

       return roundOff;
    }

    @Override
    public void refresh(Collection<Refreshable> collection) {
        x.refresh(collection);
        y.refresh(collection);
    }
}
