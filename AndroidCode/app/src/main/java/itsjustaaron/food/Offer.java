package itsjustaaron.food;

import android.util.Log;

import java.util.Date;
import java.util.Map;

import itsjustaaron.food.Back.Data;

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
    public String offererPortrait;

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
        price = Double.parseDouble(map.get("price").toString());
        offererPortrait = map.get("offererPortrait").toString();
        try {
            expire = Data.serverDateFormat.parse(map.get("expire").toString());
        } catch (Exception e) {
            Log.d("dateParse", e.toString());
        }

    }
}
