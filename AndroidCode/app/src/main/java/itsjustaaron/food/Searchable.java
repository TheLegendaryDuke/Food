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

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;

/**
 * Created by Aaron-Work on 8/21/2016.
 */
public class Searchable extends Activity {

    private String whereClause;

    private void doMySearch(String query) {
        ArrayList searchCriteria;
        if(Data.onCraving) {
            searchCriteria = Data.cSearchCriteria;
        }else {
            searchCriteria = Data.oSearchCriteria;
        }
        searchCriteria.clear();
        List<String> tagResult = Food.csvToList(query.toUpperCase());
        boolean tagCheck = true;
        for (int i = 0; i < tagResult.size(); i++) {
            if (!Data.tags.contains(tagResult.get(i))) {
                tagCheck = false;
                break;
            }
        }
        whereClause = "";
        if (tagCheck) {
            searchCriteria.addAll(tagResult);
            for (int i = 0; i < tagResult.size(); i++) {
                whereClause = whereClause + "tags LIKE '%" + tagResult.get(i) + "%'";
                if (i != tagResult.size() - 1) {
                    whereClause = whereClause + " and ";
                }
            }
        } else {
            searchCriteria.add(query);
            whereClause = "name LIKE '%" + query + "%'";
        }
        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... voids) {
                BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause(whereClause);
                List<Map> maps = Backendless.Persistence.of("foods").find(dataQuery).getCurrentPage();
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
                    if(Data.onCraving) {
                        Data.cravingPaged = Back.findObjectByWhere(where, Back.object.craving);
                        List<Map> mapResult = Data.cravingPaged.getCurPage();
                        if(mapResult.size() == 0) {
                            return 1;
                        }else {
                            Data.cravings.clear();
                            for (int i = 0; i < mapResult.size(); i++) {
                                Map obj = mapResult.get(i);
                                final Craving craving = new Craving(obj);
                                Data.cravings.add(craving);
                            }
                        }
                    }else {
                        Data.offerCollection = Backendless.Persistence.of("foodOffers").find(backendlessDataQuery);
                        List<Map> mapResult = Data.offerCollection.getCurrentPage();
                        if(mapResult.size() == 0) {
                            return 1;
                        }else {
                            Data.foodOffers.clear();
                            for (int i = 0; i < mapResult.size(); i++) {
                                Map obj = mapResult.get(i);
                                final FoodOffer foodOffer = new FoodOffer(obj);
                                Data.foodOffers.add(foodOffer);
                            }
                        }
                    }
                }else {
                    return 1;
                }
                return 0;
            }

            @Override
            public void onPostExecute(Integer v) {
                if(Data.onCraving) {
                    if (v == 1) {
                        Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                    }else {
                        Data.main.searchCallBack();
                        Data.cravingFragment.notifyChanges();
                    }
                }else {
                    if (v == 1) {
                        Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                    }else {
                        Data.main.searchCallBack();
                        Data.offerFragment.notifyChanges();
                    }
                }
                finish();
            }
        }.execute(new Void[]{});
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wait);

        // Get the intent, verify the action and get the query(ommitted since custom usage)
        Intent intent = getIntent();
        String query = intent.getStringExtra(SearchManager.QUERY);
        doMySearch(query);
    }
}
