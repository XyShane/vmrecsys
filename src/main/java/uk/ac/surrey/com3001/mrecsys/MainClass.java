package uk.ac.surrey.com3001.mrecsys;

import org.apache.commons.io.FilenameUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.ParallelSGDFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDPlusPlusFactorizer;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import java.io.*;
import java.util.Scanner;
import java.util.logging.LogManager;
import de.umass.lastfm.*;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;
import uk.ac.surrey.com3001.mrecsys.handlers.DataProcessor;
import uk.ac.surrey.com3001.mrecsys.handlers.LastFMAPIHandler;
import uk.ac.surrey.com3001.mrecsys.recommenders.builder.ItemRecommender;
import uk.ac.surrey.com3001.mrecsys.recommenders.builder.CMItemUserHybridRec;
import uk.ac.surrey.com3001.mrecsys.recommenders.builder.SVDMatrixRecommender;
import uk.ac.surrey.com3001.mrecsys.recommenders.builder.UserNeighborhoodRecommender;

/**
 * Created by Xyline on 05/05/2016.
 */
public class MainClass {

    private static File datasetFile;
    private static File contentFile;
    private static DataModel dataModel;
    private static LastFMAPIHandler lfm;
    private static boolean usingLFM = false;
    private static boolean evaluationMode = false;

    public static void main(String[] args) throws Exception {
        // Disables all the logging in other classes/dependencies.
        LogManager.getLogManager().reset();

        System.out.println("==================================================");
        String logo = " __  __ ____  _____ ____ ______   ______  \n" +
                "|  \\/  |  _ \\| ____/ ___/ ___\\ \\ / / ___| \n" +
                "| |\\/| | |_) |  _|| |   \\___ \\\\ V /\\___ \\ \n" +
                "| |  | |  _ <| |__| |___ ___) || |  ___) |\n" +
                "|_|  |_|_| \\_\\_____\\____|____/ |_| |____/ \n" +
                "                                          ";
        System.out.println(logo);
        System.out.println("==================================================");
        Scanner sc = new Scanner(System.in);
        selectDataset(sc);

    }

    public static void mainMenu(Scanner sc) throws Exception {
        boolean invalidCommand = true;
        evaluationMode = false;
        DataProcessor dp = new DataProcessor(lfm);
        System.out.println("==================================================");
        System.out.println(" __  __       _         __  __                  \n" +
                "|  \\/  | __ _(_)_ __   |  \\/  | ___ _ __  _   _ \n" +
                "| |\\/| |/ _` | | '_ \\  | |\\/| |/ _ \\ '_ \\| | | |\n" +
                "| |  | | (_| | | | | | | |  | |  __/ | | | |_| |\n" +
                "|_|  |_|\\__,_|_|_| |_| |_|  |_|\\___|_| |_|\\__,_|");
        System.out.println("---------------------------------------------------");
        System.out.println("n - Normalize Data");
        System.out.println("r - Recommendation Menu");
        System.out.println("d - Change Data set");
        System.out.println("q - Exit application");
        System.out.println("help - Display Help Page");
        System.out.println("---------------------------------------------------");
        System.out.println("==================================================");

        System.out.println("Please key in a command.");
        String cmd = sc.nextLine();

        while(invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "n":
                    System.out.println("Are you sure? This will modify your preference data to a range of 1.00 - 5.00. (Y/N)");
                    String answer = sc.nextLine();
                    boolean invalidAns = true;
                    invalidCommand = false;
                    while(invalidAns){
                        if(answer.equals("Y") || answer.equals("y")){
                            invalidAns = false;
                            dp.prepareNormalize(datasetFile,sc);

                            // Set to new dataset
                            String baseUrl = FilenameUtils.getFullPath(datasetFile.getAbsolutePath());
                            String normURL = baseUrl + FilenameUtils.getBaseName(datasetFile.getName()) + "_norm." + FilenameUtils.getExtension(datasetFile.getPath());
                            datasetFile = new File(normURL);
                            System.out.println("System is now using " + FilenameUtils.getBaseName(datasetFile.getPath()) + "." + FilenameUtils.getExtension(datasetFile.getPath()) + " as the dataset.");

                        } else if (answer.equals("N") || answer.equals("n")){
                            invalidAns = false;
                            System.out.println("Returning to main menu...");
                        } else {
                            System.out.println("Invalid answer. Please choose (Y/N)");
                            answer = sc.nextLine();
                        }
                    }
                    mainMenu(sc);
                    break;
                case "r":
                    invalidCommand = false;
                    recommenderMenu(sc);
                    break;
                case "d":
                    invalidCommand = false;
                    selectDataset(sc);
                    mainMenu(sc);
                    break;
                case "q":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                case "help":
                    System.out.println("==================================================");
                    System.out.println("n - Normalize Data:");
                    System.out.println("This normalizes the data set's preference values to a range of 1.0 - 5.0.");
                    System.out.println("It's recommended that this step is performed for better recommendation and evaluation results.");
                    System.out.println("--------------------------------------------------------------");
                    System.out.println("r - Recommendation Menu");
                    System.out.println("Navigates to the recommendation menu.");
                    System.out.println("In this menu, you can setup implementation, generate recommendations, evaluate recommendations" +
                            " and enable usage of *Last.fm API to pull music data.");
                    System.out.println("*Stable internet connection is required.");
                    System.out.println("--------------------------------------------------------------");
                    System.out.println("d - Change Data set");
                    System.out.println("This allows you to change the currently set dataset.");
                    System.out.println("--------------------------------------------------------------");
                    System.out.println("q - Exit application");
                    System.out.println("Exits the application.");
                    System.out.println("--------------------------------------------------------------");
                    cmd = sc.nextLine();
                    break;
                default:
                    System.out.println("Sorry that response was invalid. Try again.");
                    cmd = sc.nextLine();
                    break;
            }
        }

    }

    private static void selectDataset(Scanner sc) throws Exception {
        System.out.println("Please type the path to your data set. Type 'quit' to exit application.");
        String filepath = sc.nextLine();
        File file = new File(filepath);

        while(!file.exists() || file.isDirectory() || filepath != "quit"){
            if(file.exists() && !file.isDirectory()) {
                datasetFile = file;
                System.out.println("Dataset loaded!");
                System.out.println(datasetFile.getAbsoluteFile());
                mainMenu(sc);
                break;
            } else if(filepath.equals("quit")){
                System.out.println("Exiting application..");
                System.out.println("Goodbye!");
                sc.close();
                System.exit(0);
            } else {
                System.out.println("File Not Found. Please ensure you typed in a correct path or if the file exists.");
                filepath = sc.nextLine();
                file = new File(filepath);
            }
        }
    }
//
    private static Recommender buildHybridRecommender(Recommender x, Recommender y) throws IOException, TasteException {
        RecommenderBuilder builder = new CMItemUserHybridRec(x,y);
        return builder.buildRecommender(dataModel);
    }

    private static Recommender buildItemRecommender(ItemSimilarity similarity) throws IOException, TasteException {
        RecommenderBuilder builder = new ItemRecommender(similarity);
        return builder.buildRecommender(dataModel);
    }

    private static Recommender buildUserRecommender(UserSimilarity similarity, NearestNUserNeighborhood neighborhood) throws IOException, TasteException {
        RecommenderBuilder builder = new UserNeighborhoodRecommender(similarity,neighborhood);
        return builder.buildRecommender(dataModel);
    }

    private static Recommender buildSVDRecommender(Factorizer factorizer) throws IOException, TasteException {
        RecommenderBuilder builder = new SVDMatrixRecommender(factorizer);
        return builder.buildRecommender(dataModel);
    }

    private static void recommenderMenu(Scanner sc) throws Exception {
        boolean invalidCommand = true;
        evaluationMode = false;
        System.out.println(" ____             __  __                  \n" +
                "|  _ \\ ___  ___  |  \\/  | ___ _ __  _   _ \n" +
                "| |_) / _ \\/ __| | |\\/| |/ _ \\ '_ \\| | | |\n" +
                "|  _ <  __/ (__  | |  | |  __/ | | | |_| |\n" +
                "|_| \\_\\___|\\___| |_|  |_|\\___|_| |_|\\__,_|\n" +
                "                                          ");
        System.out.println("---------------------------------");
        System.out.println("g - Generate Music Recommendations");
        System.out.println("e - Evaluate Recommenders");
        System.out.println("o - Last.Fm Options");
        System.out.println("m - Back to main menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");

        System.out.println("Please key in a command.");
        String cmd = sc.nextLine();
        while(invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "g":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "e":
                    invalidCommand = false;
                    evaluationMode = true;
                    generateRecommendations(sc);
                    break;
                case "o":
                    invalidCommand = false;
                    lastFmSettings(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    break;
                case "quit":
                    invalidCommand = false;
                    break;
                default:
                    cmd = sc.nextLine();
                    break;
            }
        }
    }

    private static void evaluateMethods(Scanner sc, RecommenderBuilder builder) throws Exception {
        // Reproducible results
        RandomUtils.useTestSeed();
        boolean invalidCommand = true;
        String evaluatorName = "RMSE";
        System.out.println(" __  __      _   _               _     \n" +
                "|  \\/  | ___| |_| |__   ___   __| |___ \n" +
                "| |\\/| |/ _ \\ __| '_ \\ / _ \\ / _` / __|\n" +
                "| |  | |  __/ |_| | | | (_) | (_| \\__ \\\n" +
                "|_|  |_|\\___|\\__|_| |_|\\___/ \\__,_|___/\n" +
                "                                       ");
        System.out.println("==========================================");
        System.out.println("---------------------------------");
        System.out.println("1 - Root Mean-Squared Error Evaluator");
        System.out.println("2 - Average Absolute Difference Evaluator");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");

        System.out.println("Please select an evaluator.");

        // Defaulted to RMSE
        RecommenderEvaluator evaluator = new RMSRecommenderEvaluator();
        String cmd = sc.nextLine();
        while(invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;

                    break;
                case "2":
                    invalidCommand = false;
                    evaluatorName = "Avg Absolute Difference";
                    evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
                    break;
                case "b":
                    invalidCommand = false;
                    recommenderMenu(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please enter a command from the list.");
                    cmd = sc.nextLine();
                    break;
            }
        }
        double trainingPercentage;
        double evaluationPercentage;
        System.out.println("Please enter the percentage for training. //'0.9' = 90%");
        while (!sc.hasNextDouble()) sc.next();
        {
            trainingPercentage = sc.nextDouble();
            System.out.println("Now key in the percentage for evaluation.");
            while (!sc.hasNextDouble()) sc.next();
            {
                evaluationPercentage = sc.nextDouble();
            }
        }
        System.out.println("Evaluating results..");
        double result = evaluator.evaluate(builder, null, dataModel, trainingPercentage, evaluationPercentage);
        System.out.println(evaluatorName + ": "+ result);

        invalidCommand = true;
        System.out.println("Do you want to evaluate more implementation? (Y/N) 'N' will bring you back to the recommender menu.");
        cmd = sc.nextLine();
        while (invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "y":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "n":
                    invalidCommand = false;
                    recommenderMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please key in either (Y/N).");
                    cmd = sc.nextLine();
                    break;
            }
        }
    }

    private static void generateRecommendations(Scanner sc) throws Exception {
        dataModel = new FileDataModel(datasetFile);
        boolean invalidCommand = true;

        System.out.println("==========================================");
        if(evaluationMode){
            System.out.println(" _____            _             _   _             \n" +
                    "| ____|_   ____ _| |_   _  __ _| |_(_) ___  _ __  \n" +
                    "|  _| \\ \\ / / _` | | | | |/ _` | __| |/ _ \\| '_ \\ \n" +
                    "| |___ \\ V / (_| | | |_| | (_| | |_| | (_) | | | |\n" +
                    "|_____| \\_/ \\__,_|_|\\__,_|\\__,_|\\__|_|\\___/|_| |_|\n" +
                    "                                                  ");
        } else {
            System.out.println(" __  __           _      ____           \n" +
                    "|  \\/  |_   _ ___(_) ___|  _ \\ ___  ___ \n" +
                    "| |\\/| | | | / __| |/ __| |_) / _ \\/ __|\n" +
                    "| |  | | |_| \\__ \\ | (__|  _ <  __/ (__ \n" +
                    "|_|  |_|\\__,_|___/_|\\___|_| \\_\\___|\\___|");
        }
        System.out.println("---------------------------------");
        System.out.println("1 - Item-Based Recommender");
        System.out.println("2 - User-Based Recommender");
        System.out.println("3 - SVD Recommender");
        System.out.println("4 - Cascading Mixed Item-User Hybrid Recommender");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");

        System.out.println("Please select a recommender.");
        String cmd = sc.nextLine();
        while(invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;
                    itemRecommenderMenu(sc);
                    break;
                case "2":
                    invalidCommand = false;
                    userRecommenderMenu(sc);
                    break;
                case "3":
                    invalidCommand = false;
                    svdRecommenderMenu(sc);
                    break;
                case "4":
                    invalidCommand = false;
                    hybridRecommenderMenu(sc);
                    break;
                case "b":
                    invalidCommand = false;
                    recommenderMenu(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please enter a command from above.");
                    cmd = sc.nextLine();
                    break;
            }
        }
    }

private static void itemRecommenderMenu(Scanner sc) throws Exception {
    boolean invalidCommand = true;
    String cmd;
    System.out.println("==========================================");
    System.out.println(" ___ _                 ____                     _ \n" +
            "|_ _| |_ ___ _ __ ___ | __ )  __ _ ___  ___  __| |\n" +
            " | || __/ _ \\ '_ ` _ \\|  _ \\ / _` / __|/ _ \\/ _` |\n" +
            " | || ||  __/ | | | | | |_) | (_| \\__ \\  __/ (_| |\n" +
            "|___|\\__\\___|_| |_| |_|____/ \\__,_|___/\\___|\\__,_|");
    System.out.println("---------------------------------");
    System.out.println("1 - Tanimoto Coefficient Similarity");
    System.out.println("2 - LogLikelihood Similarity");
    System.out.println("3 - Euclidean Distance Similarity");
    System.out.println("4 - Pearson Correlation Similarity");
    System.out.println("b - Back");
    System.out.println("m - Main Menu");
    System.out.println("quit - Exits the application");
    System.out.println("---------------------------------");
    System.out.println("==========================================");

    System.out.println("Please select a similarity algorithm.");

    // Defaulted to LogLikelihoodSimilarity
    ItemSimilarity similarity = new LogLikelihoodSimilarity(dataModel);
    cmd = sc.nextLine();
    while (invalidCommand) {
        switch (cmd.toLowerCase()) {
            case "1":
                invalidCommand = false;
                similarity = new TanimotoCoefficientSimilarity(dataModel);
                break;
            case "2":
                invalidCommand = false;
                break;
            case "3":
                invalidCommand = false;
                similarity = new EuclideanDistanceSimilarity(dataModel);
                break;
            case "4":
                invalidCommand = false;
                similarity = new PearsonCorrelationSimilarity(dataModel);
                break;
            case "b":
                invalidCommand = false;
                generateRecommendations(sc);
                break;
            case "m":
                invalidCommand = false;
                mainMenu(sc);
                break;
            case "quit":
                invalidCommand = false;
                System.out.println("Exiting application..");
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Please enter one of the above!");
                cmd = sc.nextLine();
                break;
        }
    }

    if (evaluationMode) {
        evaluateMethods(sc,new ItemRecommender(similarity));
    } else {
        Recommender itemRec = buildItemRecommender(similarity);
        long userid;
        int recNo;
        System.out.println("Please enter the userid.");
        while (!sc.hasNextLong()) sc.next();
        {
            userid = sc.nextLong();
            System.out.println("Now key in how many recommendations to generate. Note that large amounts will take longer time to process.");
            while (!sc.hasNextInt()) sc.next();
            {
                recNo = sc.nextInt();
            }
        }

        // Check if lastFM API is enabled.
        checkLFM(sc, itemRec, userid, recNo, "item");
    }
}
    private static void lastFmSettings(Scanner sc) throws Exception {
        boolean invalidCommand = true;
        System.out.println("==========================================");
        System.out.println(" _              _     _____ __  __      _    ____ ___ \n" +
                "| |    __ _ ___| |_  |  ___|  \\/  |    / \\  |  _ \\_ _|\n" +
                "| |   / _` / __| __| | |_  | |\\/| |   / _ \\ | |_) | | \n" +
                "| |__| (_| \\__ \\ |_ _|  _| | |  | |  / ___ \\|  __/| | \n" +
                "|_____\\__,_|___/\\__(_)_|   |_|  |_| /_/   \\_\\_|  |___|\n" +
                "                                                      ");
        System.out.println("---------------------------------");
        System.out.println("Enabling Last.FM API requires a content file that is related to your dataset containing song/artist/music information.");
        System.out.println("You also need a Last.FM Developer API account. You can create one at http://www.last.fm/api.");
        System.out.println("1 - Enable.");
        System.out.println("2 - Disable");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");

        System.out.println("Please key in a command.");
        String cmd = sc.nextLine();
        while(invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;
                    System.out.println("Please type in the path to your content file.");
                    String filepath = sc.nextLine();
                    File cFile = new File(filepath);

                    while(!cFile.exists() || cFile.isDirectory() || filepath != "quit"){
                        if(cFile.exists() && !cFile.isDirectory()) {
                            contentFile = cFile;
                            System.out.println("Content file loaded!");
                            System.out.println(contentFile.getAbsoluteFile());
                            break;
                        } else if(filepath.equals("quit")){
                            System.out.println("Exiting application..");
                            System.out.println("Goodbye!");
                            sc.close();
                            System.exit(0);
                        } else {
                            System.out.println("File Not Found. Please ensure you keyed in a correct path or if the file exists.");
                            filepath = sc.nextLine();
                            cFile = new File(filepath);
                        }
                    }

                    String apiKey;
                    String user;
                    String password;
                    String secret;
                    System.out.println("Please key in your Last.FM API key.");
                    while (!sc.hasNextLine()) sc.next(); {
                    apiKey = sc.nextLine();
                    System.out.println("Now type in your username.");
                        while (!sc.hasNextLine()) sc.next();{
                            user = sc.nextLine();
                            System.out.println("Now your password");
                            while(!sc.hasNextLine()) sc.next();{
                                password = sc.nextLine();
                                System.out.println("Finally, type in your secret value.");
                                while(!sc.hasNextLine()) sc.next();{
                                    secret = sc.nextLine();
                                }
                            }
                        }
                    }
                    lfm = new LastFMAPIHandler(apiKey,user,password,secret);
                    Caller.getInstance().setUserAgent("tst");
                    usingLFM = true;
                    System.out.println("Last FM Api enabled.");
                    mainMenu(sc);
                    break;
                case "2":
                    invalidCommand = false;
                    System.out.println("Last FM Api disabled.");
                    usingLFM = false;
                    mainMenu(sc);
                    break;
                case "b":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please key in one of the above!");
                    cmd = sc.nextLine();
                    break;
            }
        }

    }

    private static void userRecommenderMenu(Scanner sc) throws Exception {
        boolean invalidCommand = true;
        String cmd;
        System.out.println("==========================================");
        System.out.println(" _   _               ____                     _ \n" +
                "| | | |___  ___ _ __| __ )  __ _ ___  ___  __| |\n" +
                "| | | / __|/ _ \\ '__|  _ \\ / _` / __|/ _ \\/ _` |\n" +
                "| |_| \\__ \\  __/ |  | |_) | (_| \\__ \\  __/ (_| |\n" +
                " \\___/|___/\\___|_|  |____/ \\__,_|___/\\___|\\__,_|");
        System.out.println("---------------------------------");
        System.out.println("1 - Tanimoto Coefficient Similarity");
        System.out.println("2 - Pearson Correlation Similarity");
        System.out.println("3 - Euclidean Distance Similarity");
        System.out.println("4 - Spearman Correlation Similarity");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");

        System.out.println("Please select a similarity algorithm.");

        // Defaulted to PearsonCorrelationSimilarity
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        cmd = sc.nextLine();
        while (invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;
                    similarity = new TanimotoCoefficientSimilarity(dataModel);
                    break;
                case "2":
                    invalidCommand = false;
                    break;
                case "3":
                    invalidCommand = false;
                    similarity = new EuclideanDistanceSimilarity(dataModel);
                    break;
                case "4":
                    invalidCommand = false;
                    similarity = new SpearmanCorrelationSimilarity(dataModel);
                    break;
                case "b":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Sorry that response was invalid. Try again.");
                    cmd = sc.nextLine();
                    break;
            }
        }
        int neighbourhoodSize;
        System.out.println("Please enter a valid integer to set the neighbourhood size.");
        while (!sc.hasNextInt()) sc.next();
        {
            neighbourhoodSize = sc.nextInt();
        }

        NearestNUserNeighborhood neighbourhood = new NearestNUserNeighborhood(neighbourhoodSize, similarity, dataModel);
        if (evaluationMode) {
            evaluateMethods(sc, new UserNeighborhoodRecommender(similarity,neighbourhood));
        } else {
            Recommender userRec = buildUserRecommender(similarity, neighbourhood);
            System.out.println("Please key in the user id to recommend to.");

            long userid;
            int recNo;
            while (!sc.hasNextLong()) sc.next();
            {
                userid = sc.nextLong();
                System.out.println("Now key in how many recommendations to generate. Note that large amounts will take longer time to process.");
                while (!sc.hasNextInt()) sc.next();
                {
                    recNo = sc.nextInt();
                }
            }
            // Checks if last FM is enabled.
            checkLFM(sc, userRec, userid, recNo, "user");
        }
    }

    private static void checkLFM(Scanner sc, Recommender rec, long userid, int recNo, String recommenderType) throws Exception {
        // checks if LFM is enabled.
        boolean invalidCommand;
        String cmd;

        if(usingLFM){
            Caller.getInstance().setUserAgent("tst");
            DataProcessor dp = new DataProcessor(lfm);

            int dataType;
            int dataId;
            int contentIndex;
            String delimiter;
            boolean skipHeader;
            System.out.println("Please choose a datatype.");
            System.out.println("1: Artist, 2:Album, Anything else prints normal recommendations.");

            while(!sc.hasNextInt())sc.next(); {
                dataType = sc.nextInt();
                System.out.println("Do you want to skip the header? (some datasets have the first line to describe the columns)");
                System.out.println("true or false");
                while(!sc.hasNextBoolean())sc.next();{
                    skipHeader = sc.nextBoolean();

                    System.out.println("Please key in the index of the item id column.");
                    System.out.println("The index where your id is located. Must match the id in dataset.");
                    while(!sc.hasNextInt())sc.next();{
                        dataId = sc.nextInt();

                        System.out.println("Type in the delimiter. This is what is used to separate your content dataset.");
                        System.out.println("Values separator. 1: \'\\ t\' for tab seperated | 2: \',\' for comma separated anything else is");
                        while(!sc.hasNextInt())sc.next();{
                            int delimId = sc.nextInt();
                            if(delimId == 1){
                                delimiter = "\t";
                            } else {
                                delimiter = ",";
                            }

                            System.out.println("Finally, please key in the index of the column that contains your search query.");
                            System.out.println("0 represents the first column.");
                            while(!sc.hasNextInt())sc.next();{
                                contentIndex = sc.nextInt();
                            }
                        }
                    }
                }
            }
            dp.getInformation(rec,userid,recNo,contentFile,dataType,skipHeader,dataId,delimiter,contentIndex);
        } else {
            System.out.println("################################################");
            for(RecommendedItem recommendedItem : rec.recommend(userid, recNo))
                System.out.println("[Recommended Item: " + recommendedItem.getItemID() + ", Value: " + recommendedItem.getValue() + "]");
            System.out.println("################################################");
            System.out.println();
        }


        System.out.println("Here are the recommendations for user " + userid + ".");
        System.out.println("Do you want to generate more recommendations? (Y/N) 'N' will bring you back to the recommendations menu.");

        invalidCommand = true;
        cmd = sc.nextLine();
        while (invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "y":
                    invalidCommand = false;
                    switch(recommenderType.toLowerCase()){
                        case "svd":
                            svdRecommenderMenu(sc);
                            break;
                        case "item":
                            itemRecommenderMenu(sc);
                            break;
                        case "user":
                            userRecommenderMenu(sc);
                            break;
                        case "hybrid":
                            hybridRecommenderMenu(sc);
                            break;
                    }
                    break;
                case "n":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please enter either (Y/N).");
                    cmd = sc.nextLine();
                    break;
            }
        }
    }

    private static void svdRecommenderMenu(Scanner sc) throws Exception {
        boolean invalidCommand = true;
        String cmd;
        System.out.println("==========================================");
        System.out.println(" ______     ______  \n" +
                "/ ___\\ \\   / /  _ \\ \n" +
                "\\___ \\\\ \\ / /| | | |\n" +
                " ___) |\\ V / | |_| |\n" +
                "|____/  \\_/  |____/ \n" +
                "                    ");
        System.out.println("1 - Alternating-Least-Squares with Weighted-λ-Regularization(ALSWR) Factorizer");
        System.out.println("2 - SVD++ Factorizer");
        System.out.println("3 - Parallel SGD Factorizer");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");

        System.out.println("Please select a factorizer algorithm.");

        // Defaulted to ALWSWR Factorizer
        Factorizer factorizer = new ALSWRFactorizer(dataModel, 10, 0.05, 10);
        int noFeatures;
        double lambdaVal;
        int iterations;
        int numOfEpochs;
        cmd = sc.nextLine();
        while (invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;
                    System.out.println("ALSWR Factorizer requires 3 parameters.");
                    System.out.println("Please specify how many features.");
                    while (!sc.hasNextInt()) sc.next();
                {
                    noFeatures = sc.nextInt();
                    System.out.println("Now enter the value of lambda. Example: '1.0'.");
                    while (!sc.hasNextDouble()) sc.next();
                    {
                        lambdaVal = sc.nextDouble();
                        System.out.println("Finally, key in the number of iterations.");
                        while (!sc.hasNextInt()) sc.next();
                        {
                            iterations = sc.nextInt();
                        }
                    }
                }
                factorizer = new ALSWRFactorizer(dataModel, noFeatures, lambdaVal, iterations);
                break;
                case "2":
                    invalidCommand = false;
                    System.out.println("SVD++ Factorizer requires 2 parameters.");
                    System.out.println("Please specify how many features.");
                    while (!sc.hasNextInt()) sc.next();
                {
                    noFeatures = sc.nextInt();
                    System.out.println("Finally, key in the number of iterations.");
                    while (!sc.hasNextInt()) sc.next();
                    {
                        iterations = sc.nextInt();
                    }
                }
                factorizer = new SVDPlusPlusFactorizer(dataModel, noFeatures, iterations);
                break;
                case "3":
                    invalidCommand = false;
                    System.out.println("Parallel SGD Factorizer requires 3 parameters.");
                    System.out.println("Please specify how many features.");
                    while (!sc.hasNextInt()) sc.next();
                {
                    noFeatures = sc.nextInt();
                    System.out.println("Now enter the value of lambda. Example: '1.0'.");
                    while (!sc.hasNextDouble()) sc.next();
                    {
                        lambdaVal = sc.nextDouble();
                        System.out.println("Finally, key in the number of epochs.");
                        while (!sc.hasNextInt()) sc.next();
                        {
                            numOfEpochs = sc.nextInt();
                        }
                    }
                }
                factorizer = new ParallelSGDFactorizer(dataModel, noFeatures, lambdaVal, numOfEpochs);
                break;
                case "b":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Sorry that response was invalid. Try again.");
                    cmd = sc.nextLine();
                    break;
            }
        }

        if (evaluationMode) {
            evaluateMethods(sc,new SVDMatrixRecommender(factorizer));
        } else {
            System.out.println("Building Recommender... This could take some time.");
            Recommender svdRec = buildSVDRecommender(factorizer);
            System.out.println("Recommender built!");
            long userid;
            int recNo;
            System.out.println("Please enter the user id.");
            while (!sc.hasNextLong()) sc.next();
            {
                userid = sc.nextLong();
                System.out.println("Now key in how many recommendations to generate. Note that large amounts will take longer time to process.");
                while (!sc.hasNextInt()) sc.next();
                {
                    recNo = sc.nextInt();
                }
            }

            checkLFM(sc, svdRec, userid, recNo, "svd");
        }
    }

    private static void hybridRecommenderMenu(Scanner sc) throws Exception {
        boolean invalidCommand = true;
        String cmd;
        System.out.println("==========================================");
        System.out.println(" _   _       _          _     _ _   _              ___ _                 \n" +
                "| | | |_   _| |__  _ __(_) __| | | | |___  ___ _ _|_ _| |_ ___ _ __ ___  \n" +
                "| |_| | | | | '_ \\| '__| |/ _` | | | / __|/ _ \\ '__| || __/ _ \\ '_ ` _ \\ \n" +
                "|  _  | |_| | |_) | |  | | (_| | |_| \\__ \\  __/ |  | || ||  __/ | | | | |\n" +
                "|_| |_|\\__, |_.__/|_|  |_|\\__,_|\\___/|___/\\___|_| |___|\\__\\___|_| |_| |_|\n" +
                "       |___/       ");

        System.out.println("The Cascading Mixed Item-User Hybrid Recommender uses 2 implementation to produce results.");
        System.out.println("---------------------------------");
        System.out.println("1 - Tanimoto Coefficient Similarity");
        System.out.println("2 - LogLikelihood Similarity");
        System.out.println("3 - Euclidean Distance Similarity");
        System.out.println("4 - Pearson Correlation Similarity");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");
        System.out.println("Please select a similarity for the Item Recommender.");

        // Defaulted to LogLikelihoodSimilarity
        ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
        cmd = sc.nextLine();
        while (invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;
                    itemSimilarity = new TanimotoCoefficientSimilarity(dataModel);
                    break;
                case "2":
                    invalidCommand = false;
                    break;
                case "3":
                    invalidCommand = false;
                    itemSimilarity = new EuclideanDistanceSimilarity(dataModel);
                    break;
                case "4":
                    invalidCommand = false;
                    itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
                    break;
                case "b":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please choose a similarity.");
                    cmd = sc.nextLine();
                    break;
            }
        }

        Recommender x = buildItemRecommender(itemSimilarity);

        System.out.println("==========================================");
        System.out.println("1 - Tanimoto Coefficient Similarity");
        System.out.println("2 - Pearson Correlation Similarity");
        System.out.println("3 - Euclidean Distance Similarity");
        System.out.println("4 - Spearman Correlation Similarity");
        System.out.println("b - Back");
        System.out.println("m - Main Menu");
        System.out.println("quit - Exits the application");
        System.out.println("---------------------------------");
        System.out.println("==========================================");

        System.out.println("Now select a similarity for the User Recommender.");

        // Defaulted to PearsonCorrelationSimilarity
        UserSimilarity userSimilarity = new PearsonCorrelationSimilarity(dataModel);
        cmd = sc.nextLine();
        while (invalidCommand) {
            switch (cmd.toLowerCase()) {
                case "1":
                    invalidCommand = false;
                    userSimilarity = new TanimotoCoefficientSimilarity(dataModel);
                    break;
                case "2":
                    invalidCommand = false;
                    break;
                case "3":
                    invalidCommand = false;
                    userSimilarity = new EuclideanDistanceSimilarity(dataModel);
                    break;
                case "4":
                    invalidCommand = false;
                    userSimilarity = new SpearmanCorrelationSimilarity(dataModel);
                    break;
                case "b":
                    invalidCommand = false;
                    generateRecommendations(sc);
                    break;
                case "m":
                    invalidCommand = false;
                    mainMenu(sc);
                    break;
                case "quit":
                    invalidCommand = false;
                    System.out.println("Exiting application..");
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please choose a valid response.");
                    cmd = sc.nextLine();
                    break;
            }
        }
        int neighbourhoodSize;
        System.out.println("Please enter a valid integer to set the neighbourhood size.");
        while (!sc.hasNextInt()) sc.next();
        {
            neighbourhoodSize = sc.nextInt();
        }

        NearestNUserNeighborhood neighbourhood = new NearestNUserNeighborhood(neighbourhoodSize, userSimilarity, dataModel);

        Recommender y = buildUserRecommender(userSimilarity, neighbourhood);


        if (evaluationMode) {
            evaluateMethods(sc,new CMItemUserHybridRec(x,y));
        } else {
            Recommender hybridRec = buildHybridRecommender(x, y);
            long userid;
            int recNo;
            System.out.println("Please enter the user id.");
            while (!sc.hasNextLong()) sc.next();
            {
                userid = sc.nextLong();
                System.out.println("Now key in how many recommendations to generate. Note that large amounts will take longer time to process.");
                while (!sc.hasNextInt()) sc.next();
                {
                    recNo = sc.nextInt();
                }
            }

//         Check if lastFM API is enabled.
            checkLFM(sc, hybridRec, userid, recNo, "hybrid");
        }
    }
}