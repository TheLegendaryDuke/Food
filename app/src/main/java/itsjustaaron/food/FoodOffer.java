package itsjustaaron.food;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

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

    public FoodOffer(Map map) {
        //this.objectId = map.get("objectId").toString();
        this.offerID = map.get("offerID").toString();
        this.price = Double.parseDouble(map.get("price").toString());
        this.offerer = map.get("offerer").toString();
        try {
            this.expire = (new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy")).parse(map.get("expire").toString());
        }catch (Exception e) {
            Log.d("dateParsing", e.toString());
        }
        this.city = map.get("city").toString();
        final String foodID = map.get("foodID").toString();
        boolean found = false;
        for (int i = 0; i < Data.foods.size(); i++) {
            if (Data.foods.get(i).objectId == foodID) {
                this.food = Data.foods.get(i);
                found = true;
                break;
            }
        }
        if (!found) {
            try {
                food = new Food(Backendless.Persistence.of("foods").findById(foodID));
                final String imagePath = Data.fileDir + "/foods/" + food.image;
                final File file = new File(imagePath);
                final String path = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files/foods/" + food.image;
                File dir = new File(Data.fileDir + "/foods/");
                if(!dir.exists()) {
                    dir.mkdir();
                }
                if(!file.exists()) {
                    file.createNewFile();
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    Bitmap bm = BitmapFactory.decodeStream(is);
                    FileOutputStream fos = new FileOutputStream(imagePath);
                    ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                    byte[] byteArray = outstream.toByteArray();
                    fos.write(byteArray);
                    fos.close();
                }
                Data.foods.add(food);
            } catch (BackendlessException e) {
                Log.d("backendless", e.toString());
            } catch (Exception e) {
                Log.d("download food pic", e.toString());
            }
        }
    }
}
