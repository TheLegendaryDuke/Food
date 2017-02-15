package itsjustaaron.food.Back;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessException;
import com.backendless.geo.IBackendlessLocationListener;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.backendless.persistence.local.UserIdStorageFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import itsjustaaron.food.Craving;
import itsjustaaron.food.Food;
import itsjustaaron.food.FoodOffer;
import itsjustaaron.food.Offer;
import itsjustaaron.food.R;

/**
 * Created by Aaron-Home on 2017-02-11.
 */

public class Back {
    private final static String downloadLink = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files";

    public static void init() {
        Backendless.initApp(Data.application, "0020F1DC-E584-AD36-FF74-6D3E9E917400", "7DCC75D9-058A-6830-FF54-817317E0C000", "v1");
    }

    public enum object {
        food, foodoffer, offer, craving, cravingfollower, tag
    }

    public static Object getObjectByID(String id, object object) {
        try {
            switch (object) {
                case food:
                    return new Food(Backendless.Persistence.of("foods").findById(id));
                case craving:
                    return new Craving(Backendless.Persistence.of("cravings").findById(id));
                case offer:
                    return new Offer(Backendless.Persistence.of("offers").findById(id));
            }
        }catch (Exception e) {
            errorHandle(e);
        }
        return null;
    }

    public static PagedList<Map> findObjectByWhere(String where, object object) {
        try {
            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
            dataQuery.setWhereClause(where);
            switch (object) {
                case cravingfollower:
                    return new PagedList<Map>(Backendless.Persistence.of("cravingFollowers").find(dataQuery));
                case craving:
                    return new PagedList<Map>(Backendless.Persistence.of("cravings").find(dataQuery));
                case food:
                    return new PagedList<Map>(Backendless.Persistence.of("foods").find(dataQuery));
                case foodoffer:
                    return new PagedList<Map>(Backendless.Persistence.of("foodOffers").find(dataQuery));
                case offer:
                    return new PagedList<Map>(Backendless.Persistence.of("offers").find(dataQuery));
            }
        }catch (Exception e) {
            errorHandle(e);
        }
        return null;
    }

    public static Map store(Map map, object object) {
        try {
            switch (object) {
                case craving:
                    return Backendless.Persistence.of("cravings").save(map);
                case cravingfollower:
                    return Backendless.Persistence.of("cravingFollowers").save(map);
                case food:
                    return Backendless.Persistence.of("foods").save(map);
                case tag:
                    return Backendless.Persistence.of("tags").save(map);
                case offer:
                    return Backendless.Persistence.of("offers").save(map);
                case foodoffer:
                    return Backendless.Persistence.of("foodOffers").save(map);
                default:
                    return null;
            }
        }catch (Exception e) {
            errorHandle(e);
            return null;
        }
    }

    public static void remove(Map map, object object) {
        try {
            switch (object) {
                case cravingfollower:
                    Backendless.Persistence.of("cravingFollowers").remove(map);
            }
        }catch (Exception e) {
            errorHandle(e);
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
            errorHandle(ex);
        }
    }

    public static PagedList<Map> getAll(object object) {
        try {
            BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.setOffset(0);
            queryOptions.setPageSize(Data.loadCount);
            backendlessDataQuery.setQueryOptions(queryOptions);
            switch (object) {
                case craving:
                    Data.cravingPaged = new PagedList<Map>(Backendless.Persistence.of("cravings").find(backendlessDataQuery));
                    return Data.cravingPaged;
                case tag:
                    return new PagedList<Map>(Backendless.Persistence.of("tags").find());
                case foodoffer:
                    Data.offerPaged = new PagedList<Map>(Backendless.Persistence.of("foodOffers").find(backendlessDataQuery));
                    return Data.offerPaged;
            }
        }catch (Exception e) {
            errorHandle(e);
        }
        return null;
    }

    public static void logOff() {
        Backendless.UserService.logout();
        Data.user = null;
    }

    public static void upload(File file, String target, boolean override) {
        try {
            Backendless.Files.upload(file, target, override);
        }catch (Exception e) {
            errorHandle(e);
        }
    }

    private static void errorHandle(Exception ex) {
        Data.UI.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Data.application, R.string.error, Toast.LENGTH_LONG).show();
            }
        });
        Log.e("Backend", "An error has happened", ex);
    }

    public static void resetPassword() {
        try {
            Backendless.UserService.restorePassword(Data.user.getEmail());
        }catch (Exception e) {
            errorHandle(e);
        }
    }

    public static void resetPassword(String email) {
        try {
            Backendless.UserService.restorePassword(email);
        }catch (Exception e) {
            errorHandle(e);
        }
    }

    public static void updateUserData() {
        try {
            Backendless.UserService.update(Data.user);
        }catch (Exception e) {
            errorHandle(e);
        }
    }

    public static String login(String email, String password, boolean remember) {
        try {
            Data.user = Backendless.UserService.login(email, password, remember);
            return "";
        }catch (BackendlessException ex) {
            return ex.getCode();
        }
    }
    public static boolean checkUserSession() {
        try {
            if (Backendless.UserService.isValidLogin()) {
                String userID = UserIdStorageFactory.instance().getStorage().get();
                Data.user = Backendless.Data.of(BackendlessUser.class).findById(userID);
                return true;
            }
        }catch (Exception e) {
            errorHandle(e);
        }
        return false;
    }

    public static String registerUser(String email, String password, String name) {
        try {
            final BackendlessUser user = new BackendlessUser();
            user.setEmail(email);
            user.setPassword(password);
            user.setProperty("name", name);
            user.setProperty("portrait", "");
            Data.user = Backendless.UserService.register(user);
            return "";
        }catch (BackendlessException ex) {
            return ex.getCode();
        }
    }
}
