package uk.ac.surrey.vmrecsys;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.MemoryIDMigrator;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by tansh on 17/03/2016.
 */
//@Component
public class RecEngine {

    public RecEngine() throws TasteException, IOException {

    }
    private MemoryIDMigrator migrator = new MemoryIDMigrator();
    private DataModel dataModel;
    private Recommender recommender = null;

//    @PostConstruct
    public void initRecommender() throws TasteException {

        try {

            // create a file out of the resource
            // create a map for saving the preferences (likes) for
            // a certain person
            Map<Long,List<Preference>> preferecesOfUsers = new HashMap<Long,List<Preference>>();

            String csvFile = "C:\\Users\\tansh\\Desktop\\Pictionary\\Visual Music Rec Web\\src\\main\\resources\\300userNorm.csv";
            BufferedReader br = null;
            String line = "";
            String csvSplitBy = ",";
            br = new BufferedReader(new FileReader(csvFile));

            // go through every line
            while((line = br.readLine()) != null) {
                String[] lines = line.split(csvSplitBy);

                String person = lines[0];
                System.out.println(person);
                String likeName = lines[1];
                Float rating = Float.parseFloat(lines[2]);
                if (rating.isNaN()){
                    rating = 1.0f;
                }

                // other lines contained but not used
                // String category = line[2];
                // String id = line[3];
                // String created_time = line[4];

                // create a long from the person name
                long userLong = migrator.toLongID(person);

                // store the mapping for the user
                migrator.storeMapping(userLong, person);

                // create a long from the like name
                long itemLong = migrator.toLongID(likeName);

                // store the mapping for the item
                migrator.storeMapping(itemLong, likeName);

                List<Preference> userPrefList;

                // if we already have a userPrefList use it
                // otherwise create a new one.
                if((userPrefList = preferecesOfUsers.get(userLong)) == null) {
                    userPrefList = new ArrayList<Preference>();
                    preferecesOfUsers.put(userLong, userPrefList);
                }
                // add the like that we just found to this user
                userPrefList.add(new GenericPreference(userLong, itemLong, rating));
                System.out.println("Adding "+person+"("+userLong+") to "+likeName+"("+itemLong+")");

            }

            // create the corresponding mahout data structure from the map
            FastByIDMap<PreferenceArray> preferecesOfUsersFastMap = new FastByIDMap<PreferenceArray>();
            for(Map.Entry<Long, List<Preference>> entry : preferecesOfUsers.entrySet()) {
                preferecesOfUsersFastMap.put(entry.getKey(), new GenericUserPreferenceArray(entry.getValue()));
                System.out.println("prefs loop");
            }

            // create a data model
            dataModel = new GenericDataModel(preferecesOfUsersFastMap);

            // Instantiate the recommender
//            recommender = new GenericBooleanPrefItemBasedRecommender(dataModel, new LogLikelihoodSimilarity(dataModel));
//            UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
            ItemSimilarity similarity = new LogLikelihoodSimilarity(dataModel);
//            ItemSimilarity similarity = new EuclideanDistanceSimilarity(dataModel);
//            UserNeighborhood userneighborhood = new ThresholdUserNeighborhood(1.0, similarity, dataModel);
//            recommender = new GenericUserBasedRecommender(dataModel, userneighborhood, similarity);
            recommender = new GenericItemBasedRecommender(dataModel, similarity);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<String> recommendThings(String personName) throws TasteException {
        System.out.println("recommending now.");

        List<String> recommendations = new ArrayList<String>();
        try {
            List<RecommendedItem> items = recommender.recommend(migrator.toLongID(personName), 5);
            for(RecommendedItem item : items) {
                recommendations.add(migrator.toStringID(item.getItemID()));
            }
        } catch (TasteException e) {
            System.out.print("failed");
            e.printStackTrace();
            throw e;
        }
        return recommendations;
        //return recommendations.toArray(new String[recommendations.size()]);
    }


}