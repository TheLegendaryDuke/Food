package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.Map;

public class MyOffers extends AppCompatActivity {

    ArrayList<FoodOffer> foodOffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;

            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(MyOffers.this);
                progressDialog.setMessage("Please wait");
                progressDialog.show();
            }

            @Override
            public Void doInBackground(Void... voids) {
                String where = "ownerId = " + Data.user.getEmail();
                BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                backendlessDataQuery.setWhereClause(where);
                BackendlessCollection<Map> result = Backendless.Persistence.of("foodOffers").find(backendlessDataQuery);
                for(Map m : result.getCurrentPage()) {
                    foodOffers.add(new FoodOffer(m));
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                ListView listView = (ListView) findViewById(R.id.myOffersList);
                ArrayList<Food> foods = new ArrayList<>();
                for(FoodOffer foodOffer : foodOffers) {
                    foods.add(foodOffer.food);
                }
                BasicFoodAdapter foodAdapter = new BasicFoodAdapter(MyOffers.this, foodOffers);
                listView.setAdapter(foodAdapter);
            }
        }.execute(new Void[]{});
    }
}
