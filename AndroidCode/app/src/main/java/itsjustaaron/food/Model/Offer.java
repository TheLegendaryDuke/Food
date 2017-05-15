package itsjustaaron.food.Model;

import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import itsjustaaron.food.Back.Back;
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
    public String contact;
    public Map map;

    public Offer(Map map) {
        this.map = map;
        offerer = map.get("offerer").toString();
        objectId = map.get("objectId").toString();
        boolean found = false;
        String foodID = map.get("foodID").toString();
        for (int i = 0; i < Data.foods.size(); i++) {
            if (Data.foods.get(i).objectId.equals(foodID)) {
                food = Data.foods.get(i);
                found = true;
                break;
            }
        }
        if (!found) {
            food = (Food) Back.getObjectByID(foodID, Back.object.food);
            final String imagePath = Data.fileDir + "/foods/" + food.image;
            final File file = new File(imagePath);
            File dir = new File(Data.fileDir + "/foods/");
            if (!dir.exists()) {
                dir.mkdir();
            }
            if (!file.exists()) {
                String path = "/foods/" + food.image;
                Back.downloadToLocal(path);
            }
            Data.foods.add(food);
        }
        offererPortrait = map.get("offererPortrait").toString();
        if (offererPortrait != null && !offererPortrait.equals("")) {
            final String path = "/offers/offerers/" + offererPortrait;
            File file = new File(Data.fileDir + path);
            if (!file.exists()) {
                Back.downloadToLocal(path);
            }
        }
        address = map.get("address").toString();
        city = map.get("city").toString();
        comment = map.get("comment").toString();
        zipCode = map.get("zipCode").toString();
        price = Double.parseDouble(map.get("price").toString());
        contact = map.get("contact").toString();
        try {
            expire = Data.serverDateFormat.parse(map.get("expire").toString());
        } catch (Exception e) {
            Log.d("dateParse", e.toString());
        }

    }

    public Map returnMap() {
        return map;
    }
}
