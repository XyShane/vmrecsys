package uk.ac.surrey.com3001.mrecsys.recommenders.implementation;

import org.apache.mahout.cf.taste.recommender.RecommendedItem;

/**
 * Created by Xyline on 16/05/2016.
 */
public class RecommendedItemImpl implements RecommendedItem,Comparable<RecommendedItemImpl> {

    private long itemId;
    private float itemVal;

    public RecommendedItemImpl() {
        super();
    }
    public RecommendedItemImpl(RecommendedItem recommendedItem) {
        this.itemId = recommendedItem.getItemID();
        this.itemVal = recommendedItem.getValue();
    }

    public RecommendedItemImpl(long itemId, float itemVal) {
        this.itemId = itemId;
        this.itemVal = itemVal;
    }

    @Override
    public long getItemID() {
        return this.itemId;
    }

    @Override
    public float getValue() {
        return this.itemVal;
    }

    public void setItemVal(float itemVal) {
        this.itemVal = itemVal;
    }

    public void normalizeValue(float minVal, float maxVal, float biasFactor){
        setItemVal((((this.itemVal - minVal) + biasFactor) / ((maxVal - minVal) + 1)));
    }

    @Override
    public int compareTo(RecommendedItemImpl o) {
        if(this.itemVal > o.getValue()){
            return 1;
        } else if(this.itemVal < o.getValue()){
            return -1;
        } else {
            return 0;
        }
    }
}
