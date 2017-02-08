package itsjustaaron.food;

import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;

import java.util.ArrayList;

/**
 * Created by Aaron-Work on 8/4/2016.
 */
public class Data {
    public static int loadCount = 15;
    public static BackendlessUser user;
    public static ArrayList<Craving> cravings;
    public static ArrayList<FoodOffer> foodOffers;
    public static BackendlessCollection cravingCollection;
    public static BackendlessCollection offerCollection;
    public static ArrayList<String> tags;
    public static CravingFragment cravingFragment;
    public static OfferFragment offerFragment;
    public static ArrayList<Food> foods;
    public static String fileDir;
    public static boolean onCraving;
}
