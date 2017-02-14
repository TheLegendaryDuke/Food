package itsjustaaron.food.Back;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import itsjustaaron.food.Craving;
import itsjustaaron.food.Food;
import itsjustaaron.food.FoodOffer;
import itsjustaaron.food.Offer;

/**
 * Created by Aaron-Home on 2017-02-11.
 */

public class Back {
    private final static String downloadLink = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files";

    public static enum object {
        food, foodoffer, offer, craving, cravingfollower
    }

    public static Object getObjectByID(String id, object object) {
        switch (object) {
            case food:
                return new Food(Backendless.Persistence.of("foods").findById(id));
            case craving:
                return new Craving(Backendless.Persistence.of("cravings").findById(id));
            case offer:
                return new Offer(Backendless.Persistence.of("offers").findById(id));
        }
        return null;
    }

    public static PagedList<Map> findObjectByWhere(String where, object object) {
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(where);
        switch (object) {
            case cravingfollower:
                return new PagedList<Map>(Backendless.Persistence.of("cravingFollowers").find(dataQuery));
        }
        return null;
    }

    public static void store(Map map, object object) {
        switch (object) {
            case craving:
                Backendless.Persistence.of("cravings").save(map);
                break;
            case cravingfollower:
                Backendless.Persistence.of("cravingFollowers").save(map);
        }
    }

    public static void remove(Map map, object object) {
        switch (object) {
            case cravingfollower:
                Backendless.Persistence.of("cravingFollowers").remove(map);
        }
    }

    public static void downloadToLocal(String fileDir) {
        String url = downloadLink + fileDir;
        String local = Data.fileDir + fileDir;
        try {
            File file = new File(local);
            file.createNewFile();
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            Bitmap bm = BitmapFactory.decodeStream(is);
            FileOutputStream fos = new FileOutputStream(local);
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            byte[] byteArray = outstream.toByteArray();
            fos.write(byteArray);
            fos.close();
        }catch (Exception ex) {
            Log.e("Back.Download", ex.toString(), ex);
        }
    }

    public static PagedList<Map> getAll(object object) {
        BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setOffset(0);
        queryOptions.setPageSize(Data.loadCount);
        backendlessDataQuery.setQueryOptions(queryOptions);
        switch (object) {
            case craving:
            return new PagedList<Map>(Backendless.Data.of("cravings").find(backendlessDataQuery));
        }
        return null;
    }
}
