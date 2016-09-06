package itsjustaaron.food;

import android.app.ProgressDialog;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Aaron-Work on 8/17/2016.
 */
public class Craving {
    public String objectId;
    Food food;
    int numFollowers;
    boolean following;

    public Craving() {}

    public Craving(final Map map, final Runnable runnable) {
        this.objectId = map.get("objectId").toString();
        this.numFollowers = Integer.parseInt(map.get("numFollowers").toString());
        final String foodID = map.get("foodID").toString();
        new Thread(){
            @Override
            public synchronized void run() {
                try {
                    food = new Food(Backendless.Persistence.of("Food").findById(foodID));
                    BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                    dataQuery.setWhereClause("cravingID='" + objectId + "' and userID='" + Data.user.getEmail() + "'");
                    List<Map> maps = Backendless.Persistence.of("cravingFollowers").find(dataQuery).getCurrentPage();
                    if (maps == null || maps.size() == 0) {
                        following = false;
                    } else {
                        following = true;
                    }
                    runnable.run();
                }catch (Exception e) {
                    Log.d(e.getMessage(), "run: ");
                }
            }
        }.start();
    }

    public Craving(Map map, final boolean genList) {
        this.objectId = map.get("objectId").toString();
        this.numFollowers = Integer.parseInt(map.get("numFollowers").toString());
        final String foodID = map.get("foodID").toString();
        new Thread(){
            @Override
            public synchronized void run() {
                try {
                    food = new Food(Backendless.Persistence.of("Food").findById(foodID));
                    BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                    dataQuery.setWhereClause("cravingID='" + objectId + "' and userID='" + Data.user.getEmail() + "'");
                    List<Map> maps = Backendless.Persistence.of("cravingFollowers").find(dataQuery).getCurrentPage();
                    if (maps == null || maps.size() == 0) {
                        following = false;
                    } else {
                        following = true;
                    }
                    if (genList) {
                        Data.cravings.add(Craving.this);
                        Data.cravingFragment.notifyChanges();
                        Main.hideWait();
                    }
                }catch (Exception e) {
                    Log.d(e.getMessage(), "run: ");
                }
            }
        }.start();
    }

    public void save() {
        HashMap map = new HashMap();
        map.put("objectId", objectId);
        map.put("foodID", food.objectId);
        map.put("numFollowers", numFollowers);
        Backendless.Persistence.of("Craving").save(map, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map map) {

            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {

            }
        });
    }

    public void followSwitch() {
        this.following = !this.following;
        new Thread() {
            public void run() {
                if (following) {
                    HashMap map = new HashMap();
                    map.put("cravingID", objectId);
                    map.put("userID", Data.user.getEmail());
                    numFollowers++;
                    save();
                    Backendless.Persistence.of("cravingFollowers").save(map, new AsyncCallback<Map>() {
                        @Override
                        public void handleResponse(Map map) {

                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {

                        }
                    });
                } else {
                    numFollowers--;
                    save();
                    BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                    backendlessDataQuery.setWhereClause("cravingID='" + objectId + "' and userID='" + Data.user.getEmail() + "'");
                    Backendless.Persistence.of("cravingFollowers").find(backendlessDataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                            Backendless.Persistence.of("cravingFollowers").remove(mapBackendlessCollection.getCurrentPage().get(0), new AsyncCallback<Long>() {
                                @Override
                                public void handleResponse(Long aLong) {

                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {

                                }
                            });
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {

                        }
                    });
                }
            }
        }.run();
    }
}
