package itsjustaaron.food;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aaron-Work on 8/21/2016.
 */
public class Searchable extends Activity {

    private String whereClause;

    private void doMySearch(String query) {
        List<String> tagResult = Food.csvToList(query);
        boolean tagCheck = true;
        for (int i = 0; i < tagResult.size(); i++) {
            if (!Data.tags.contains(tagResult.get(i))) {
                tagCheck = false;
                break;
            }
        }
        whereClause = "";
        if (tagCheck) {
            for (int i = 0; i < tagResult.size(); i++) {
                whereClause = whereClause + "tags LIKE '%" + tagResult.get(i) + "%'";
                if (i != tagResult.size() - 1) {
                    whereClause = whereClause + " and ";
                }
            }
        } else {
            whereClause = "name LIKE '%" + query + "%'";
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                Data.cravings.clear();
                BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause(whereClause);
                List<Map> maps = Backendless.Persistence.of("Food").find(dataQuery).getCurrentPage();
                List<String> foodIDs = new ArrayList<String>();
                if (maps.size() != 0) {
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
                    Data.collection = Backendless.Persistence.of("Craving").find(backendlessDataQuery);
                    List<Map> mapResult = Data.collection.getCurrentPage();
                    for (int i = 0; i < mapResult.size(); i++) {
                        Map obj = mapResult.get(i);
                        final Craving craving = new Craving(obj);
                        Data.cravings.add(craving);
                    }
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                Data.cravingFragment.notifyChanges();
                if (Data.cravings.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                }
                Data.cravingFragment.notifyChanges();
                finish();
            }
        }.execute(new Void[]{});
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
