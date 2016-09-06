package itsjustaaron.food;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/21/2016.
 */
public class Searchable extends Activity {

    private void doMySearch(String query) {
        List<String> tagResult = Food.csvToList(query);
        boolean tagCheck = true;
        for(int i = 0; i < tagResult.size(); i++) {
            if(!Data.tags.contains(tagResult.get(i))) {
                tagCheck = false;
                break;
            }
        }
        String whereClause = "";
        if(tagCheck) {
            for(int i = 0; i< tagResult.size(); i++) {
                whereClause = whereClause + "tags LIKE '%" + tagResult.get(i) + "%'";
                if(i != tagResult.size() - 1) {
                    whereClause = whereClause + " and ";
                }
            }
        }else {
            whereClause = "name LIKE '%" + query + "%'";
        }
        final BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(whereClause);
        new Thread() {
            @Override
            public void run() {
                Backendless.Persistence.of("Food").find(dataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                        List<String> foodIDs = new ArrayList<String>();
                        final List<Map> maps = mapBackendlessCollection.getCurrentPage();
                        if(maps.size() != 0) {
                            Data.cravings.clear();
                            for (int i = 0; i < maps.size(); i++) {
                                foodIDs.add(maps.get(i).get("objectId").toString());
                            }
                            String where = "foodID in (";
                            for (int i = 0; i < foodIDs.size(); i++) {
                                where = where + "'" + foodIDs.get(i) + "'";
                                if (i != foodIDs.size() - 1) {
                                    where = where + ", ";
                                }
                            }
                            where = where + ")";
                            final BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                            backendlessDataQuery.setWhereClause(where);
                            Backendless.Persistence.of("Craving").find(backendlessDataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
                                @Override
                                public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                                    Data.collection = mapBackendlessCollection;
                                    List<Map> mapResult = mapBackendlessCollection.getCurrentPage();
                                    for (int i = 0; i < mapResult.size(); i++) {
                                        Map obj = mapResult.get(i);
                                        final Craving craving = new Craving(obj, true);
                                    }
                                    finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {

                                }
                            });
                        }else {
                            Data.cravings.clear();
                            Data.cravingFragment.notifyChanges();
                            Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {

                    }
                });
            }
        }.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wait);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }
}
