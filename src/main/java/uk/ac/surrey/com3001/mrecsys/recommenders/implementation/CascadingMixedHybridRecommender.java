package uk.ac.surrey.com3001.mrecsys.recommenders.implementation;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.AbstractRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
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
public class CascadingMixedHybridRecommender extends AbstractRecommender implements MixedHybridRecommender {

    private DataModel dataModel;

    private final Recommender x;
    private final Recommender y;
    private ArrayList<RecommendedItemImpl> zList;

    public CascadingMixedHybridRecommender(DataModel dataModel, Recommender x, Recommender y) {
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
        float upperBias = 6;
        float lowerBias = 4;
        float biasFactor = 10;

        List<RecommendedItemImpl> xList = new ArrayList<>();
        List<RecommendedItemImpl> yList = new ArrayList<>();

        zList = new ArrayList<>();

        xList.addAll(x.recommend(user, noOfVal, idRescorer).stream().map(RecommendedItemImpl::new).collect(Collectors.toList()));
        yList.addAll(y.recommend(user, noOfVal, idRescorer).stream().map(RecommendedItemImpl::new).collect(Collectors.toList()));

        if(xList!= null && xList.size() != 0)
            normalizeAllValues(xList,upperBias/biasFactor);

        if(yList!= null && yList.size() != 0)
            normalizeAllValues(yList,lowerBias/biasFactor);

        if(xList!= null)
            zList.addAll(xList);

        if(yList!= null)
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
        Collections.reverse(zList);
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
