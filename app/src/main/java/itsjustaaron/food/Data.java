package itsjustaaron.food;

import android.app.ProgressDialog;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/4/2016.
 */
public class Data {
    public static int loadCount = 10;
    public static BackendlessUser user;
    public static List<Craving> cravings;
    public static List<Offer> offers;
    public static BackendlessCollection collection;
    public static List<String> tags;
    public static CravingFragment cravingFragment;
    public static OfferFragment offerFragment;
}
