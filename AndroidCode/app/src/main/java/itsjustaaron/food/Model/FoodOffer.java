package itsjustaaron.food.Model;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;

/**
 * Created by aozhang on 2/1/2017.
 */

public class FoodOffer {
    public Food food;
    public String offerID;
    public String city;
    public double price;
    public Date expire;
    public String offerer;
    public String offererPortrait;

    public FoodOffer(Map map) {
        //this.objectId = map.get("objectId").toString();
        this.offerID = map.get("offerID").toString();
        this.price = Double.parseDouble(map.get("price").toString());
        this.offerer = map.get("offerer").toString();
        this.offererPortrait = map.get("offererPortrait").toString();
        try {
            this.expire = (Data.serverDateFormat).parse(map.get("expire").toString());
        }catch (Exception e) {
            Log.d("dateParsing", e.toString());
        }
        this.city = map.get("city").toString();
        final String foodID = map.get("foodID").toString();
        boolean found = false;
        for (int i = 0; i < Data.foods.size(); i++) {
            if (Data.foods.get(i).objectId.equals(foodID)) {
                this.food = Data.foods.get(i);
                found = true;
                break;
            }
        }
        if (!found) {
            food = (Food) Back.getObjectByID(foodID, Back.object.food);
            final String imagePath = Data.fileDir + "/foods/" + food.image;
            final File file = new File(imagePath);
            File dir = new File(Data.fileDir + "/foods/");
            if(!dir.exists()) {
                dir.mkdir();
            }
            if(!file.exists()) {
                String path = "/foods/" + food.image;
                Back.downloadToLocal(path);
            }
            Data.foods.add(food);
        }
        if (offererPortrait != null && !offererPortrait.equals("")) {
            final String path = "/offers/offerers/" + offererPortrait;
            File file = new File(Data.fileDir + path);
            if (!file.exists()) {
                Back.downloadToLocal(path);
            }
        }
    }
}
