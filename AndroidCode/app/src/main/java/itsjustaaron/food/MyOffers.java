package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.PagedList;

public class MyOffers extends AppCompatActivity {

    ArrayList<FoodOffer> foodOffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);
        Data.UI = this;
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
                String where = "ownerId = '" + Data.user.getObjectId() + "'";
                PagedList<Map> result = Back.findObjectByWhere(where, Back.object.foodoffer);
                for(Map m : result.getCurPage()) {
                    foodOffers.add(new FoodOffer(m));
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                if(foodOffers.size() == 0) {
                    new AlertDialog.Builder(MyOffers.this).setMessage("You don't have any offers!")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
                }else {
                    ListView listView = (ListView) findViewById(R.id.myOffersList);
                    ArrayList<Food> foods = new ArrayList<>();
                    for (FoodOffer foodOffer : foodOffers) {
                        foods.add(foodOffer.food);
                    }
                    BasicFoodAdapter foodAdapter = new BasicFoodAdapter(MyOffers.this, foodOffers);
                    listView.setAdapter(foodAdapter);
                }
                progressDialog.dismiss();
            }
        }.execute(new Void[]{});
    }
}
