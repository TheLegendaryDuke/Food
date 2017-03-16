package itsjustaaron.food.Back;

import com.backendless.BackendlessUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.Offer;

/**
 * Created by Aaron-Work on 8/4/2016.
 */
public class Data {
    public static int loadCount = 15;
    public static BackendlessUser user;
    public static ArrayList<Craving> cravings;
    public static ArrayList<Offer> offers;
    public static PagedList<Map> cravingPaged;
    public static PagedList<Map> offerPaged;
    public static ArrayList<String> tags;
    public static ArrayList<Food> foods;
    public static String fileDir;
    public static boolean onCraving;
    public static SimpleDateFormat serverDateFormat = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy");
    public static SimpleDateFormat standardDateFormat = new SimpleDateFormat("yyyy MMM dd");
    public static ArrayList<String> cSearchCriteria = new ArrayList<>();
    public static ArrayList<String> oSearchCriteria = new ArrayList<>();
    public static int sortByO = 0;
    public static boolean cityRestricted = true;
    public static MyHandler handler;
    public static int sortByC = 0;
}
