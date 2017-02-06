package itsjustaaron.food;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/19/2016.
 */
public class Offer {
    public String offerer;
    public String objectId;
    public Food food;
    public String address;
    public String city;
    public String comment;
    public Date expire;
    public String zipCode;
    public double price;

    public Offer(Map map) {
        offerer = map.get("offerer").toString();
        objectId = map.get("objectId").toString();
        for (Food f : Data.foods) {
            if (f.objectId.equals(map.get("foodID"))) {
                food = f;
            }
        }
        address = map.get("address").toString();
        city = map.get("city").toString();
        comment = map.get("comment").toString();
        zipCode = map.get("zipCode").toString();
        try {
            expire = (new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy")).parse(map.get("expire").toString());
        } catch (Exception e) {
            Log.d("dateParse", e.toString());
        }

    }
}
