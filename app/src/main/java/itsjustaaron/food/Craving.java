package itsjustaaron.food;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/17/2016.
 */
public class Craving {
    public String objectId;
    Food food;
    int numFollowers;
    boolean following;

    public Craving(Map map) {
        this.objectId = map.get("objectId").toString();
        this.numFollowers = Integer.parseInt(map.get("numFollowers").toString());
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
                food = new Food(Backendless.Persistence.of("Food").findById(foodID));
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
                BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause("cravingID='" + objectId + "' and userID='" + Data.user.getEmail() + "'");
                List<Map> maps = Backendless.Persistence.of("cravingFollowers").find(dataQuery).getCurrentPage();
                if (maps == null || maps.size() == 0) {
                    following = false;
                } else {
                    following = true;
                }
                Data.foods.add(food);
            } catch (BackendlessException e) {
                Log.d("backendless", e.toString());
            } catch (Exception e) {
                Log.d("download food pic", e.toString());
            }
        }
    }


    public void save() {
        HashMap map = new HashMap();
        map.put("objectId", objectId);
        map.put("foodID", food.objectId);
        map.put("numFollowers", numFollowers);
        Backendless.Persistence.of("Craving").save(map);
    }

    public void followSwitch() {
        this.following = !this.following;
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                if (following) {
                    HashMap map = new HashMap();
                    map.put("cravingID", objectId);
                    map.put("userID", Data.user.getEmail());
                    numFollowers++;
                    save();
                    Backendless.Persistence.of("cravingFollowers").save(map);
                } else {
                    numFollowers--;
                    save();
                    BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                    backendlessDataQuery.setWhereClause("cravingID='" + objectId + "' and userID='" + Data.user.getEmail() + "'");
                    BackendlessCollection<Map> result = Backendless.Persistence.of("cravingFollowers").find(backendlessDataQuery);
                    Backendless.Persistence.of("cravingFollowers").remove(result.getCurrentPage().get(0));
                }
                return null;
            }
        }.execute(new Void[]{});
    }
}
