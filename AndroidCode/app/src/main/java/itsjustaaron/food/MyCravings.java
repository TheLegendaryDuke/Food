package itsjustaaron.food;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.Map;

public class MyCravings extends AppCompatActivity {
    ArrayList<Craving> cravings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cravings);
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;

            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(MyCravings.this);
                progressDialog.setMessage("Please wait");
                progressDialog.show();
            }

            @Override
            public Void doInBackground(Void... voids) {
                String where = "userID = " + Data.user.getEmail();
                BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                backendlessDataQuery.setWhereClause(where);
                BackendlessCollection<Map> result = Backendless.Persistence.of("cravingFollowers").find(backendlessDataQuery);
                for(Map m : result.getCurrentPage()) {
                    String cravingid = m.get("cravingID").toString();
                    Map map = Backendless.Persistence.of("cravings").findById(cravingid);
                    cravings.add(new Craving(map));
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                ListView listView = (ListView) findViewById(R.id.myCravingList);
                ArrayList<Food> foods = new ArrayList<>();
                for(Craving craving : cravings) {
                    foods.add(craving.food);
                }
                BasicFoodAdapter foodAdapter = new BasicFoodAdapter(MyCravings.this, cravings);
                listView.setAdapter(foodAdapter);
            }
        }.execute(new Void[]{});
    }
}
