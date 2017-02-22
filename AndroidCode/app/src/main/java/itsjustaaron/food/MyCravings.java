package itsjustaaron.food;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Back.PagedList;

public class MyCravings extends AppCompatActivity {
    ArrayList<Craving> cravings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
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
                String where = "userID = '" + Data.user.getObjectId() + "'";
                PagedList<Map> result = Back.findObjectByWhere(where, Back.object.cravingfollower);
                for(Map m : result.getCurPage()) {
                    String cravingid = m.get("cravingID").toString();
                    cravings.add((Craving) Back.getObjectByID(cravingid, Back.object.craving));
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
                progressDialog.dismiss();
            }
        }.execute(new Void[]{});
    }
}
