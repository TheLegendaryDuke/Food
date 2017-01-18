package itsjustaaron.food;

import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;

import java.util.ArrayList;

/**
 * Created by Aaron-Work on 8/4/2016.
 */
public class Data {
    public static int loadCount = 10;
    public static BackendlessUser user;
    public static ArrayList<Craving> cravings;
    public static ArrayList<Offer> offers;
    public static BackendlessCollection collection;
    public static ArrayList<String> tags;
    public static CravingFragment cravingFragment;
    public static OfferFragment offerFragment;
}
