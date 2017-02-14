package itsjustaaron.food.Back;

import android.widget.LinearLayout;

import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Craving;
import itsjustaaron.food.CravingFragment;
import itsjustaaron.food.Food;
import itsjustaaron.food.FoodOffer;
import itsjustaaron.food.Main;
import itsjustaaron.food.OfferFragment;

/**
 * Created by Aaron-Work on 8/4/2016.
 */
public class Data {
    public static int loadCount = 15;
    public static BackendlessUser user;
    public static ArrayList<Craving> cravings;
    public static ArrayList<FoodOffer> foodOffers;
    public static PagedList<Map> cravingPaged;
    public static BackendlessCollection offerCollection;
    public static ArrayList<String> tags;
    public static CravingFragment cravingFragment;
    public static OfferFragment offerFragment;
    public static ArrayList<Food> foods;
    public static String fileDir;
    public static boolean onCraving;
    public static SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");
    public static SimpleDateFormat standardDateFormat = new SimpleDateFormat("yyyy MMM dd");
    public static ArrayList<String> cSearchCriteria = new ArrayList<>();
    public static ArrayList<String> oSearchCriteria = new ArrayList<>();
    public static Main main;
}
