package itsjustaaron.food.FoodActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Back.PagedList;
import itsjustaaron.food.FoodShopActivities.FoodShopMain;
import itsjustaaron.food.FoodShopActivities.FoodShopWelcome;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.Helpers;
import itsjustaaron.food.Utilities.OfferCardAdapter;

public class CravingDetails extends AppCompatActivity {

    private Craving craving;
    private ProgressDialog progress;
    List<Offer> offers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_craving_details);
        progress = new ProgressDialog(this);
        progress.setMessage("Please wait...");
        final String cravingID = getIntent().getStringExtra("cravingID");

        new AsyncTask<Void, Void, Void>() {
            @Override
            public void onPreExecute() {
                progress.show();
            }

            @Override
            public Void doInBackground(Void... voids) {
                craving = (Craving) Back.getObjectByID(cravingID, Back.object.craving);
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                updateUI();
            }
        }.execute();
    }

    public void updateUI() {
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.detailBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //poopulate form

        ImageView image = (ImageView) findViewById(R.id.cravingDetailFoodImage);
        ImageView mat = (ImageView) findViewById(R.id.cravingDetailFoodImageMat);
        image.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + craving.food.image));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        CardView cardView = (CardView) findViewById(R.id.cravingCardView);
        mat.setLayoutParams(new RelativeLayout.LayoutParams(width, (int)(width * 0.526)));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(width * 0.526) - 100, (int)(width * 0.526) - 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        image.setLayoutParams(new FrameLayout.LayoutParams((int)(width * 0.526) - 100, (int)(width * 0.526) - 100));
        cardView.setLayoutParams(params);
        cardView.setRadius((int)((width * 0.526 - 100) / 2));

        TextView name = (TextView) findViewById(R.id.detailName);
        name.setText(craving.food.name);

        LinearLayout tags = (LinearLayout) findViewById(R.id.cravingDetailFoodTags);
        List<String> tagList = Food.csvToList(craving.food.tags);
        tags.removeAllViews();
        for(String tag : tagList) {
            int resID = Helpers.getTagDrawable(Data.tagColors.get(tag));
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextSize(20);
            tagView.setBackgroundResource(resID);
            tagView.setPadding(7,5,7,5);
            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagParams.setMargins(20,0,0,0);
            tags.addView(tagView, tagParams);
        }
        ((TextView) findViewById(R.id.detailDescription)).setText(craving.food.description);

        new AsyncTask<Void, Void, Void>() {
            List<String> offerIDs;

            @Override
            public Void doInBackground(Void... voids) {
                offers = new ArrayList<>();
                //add a dummy offer as placeholder
                offers.add(new Offer());
                String where = "foodID='" + craving.food.objectId + "'";
                PagedList result = Back.findObjectByWhere(where, Back.object.offer);
                final List<Map> temp = result.getCurPage();
                if (temp.size() != 0) {
                    offerIDs = new ArrayList<>();
                    for (int i = 0; i < temp.size(); i++) {
                        offerIDs.add(temp.get(i).get("objectId").toString());
                    }
                    for (int i = 0; i < offerIDs.size(); i++) {
                        try {
                            Offer offer = (Offer) Back.getObjectByID(offerIDs.get(i), Back.object.offer);
                            offers.add(offer);
                        } catch (Exception e) {
                            Data.handler.uncaughtException(Thread.currentThread(), e);
                        }
                    }
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                RecyclerView offerList = (RecyclerView) findViewById(R.id.detailCravingOffers);
                offerList.setHasFixedSize(true);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(CravingDetails.this, LinearLayoutManager.HORIZONTAL, false);
                offerList.setLayoutManager(mLayoutManager);
                OfferCardAdapter mAdapter = new OfferCardAdapter(CravingDetails.this, offers);
                offerList.setAdapter(mAdapter);
                progress.dismiss();
            }
        }.execute();
    }

    public void proposeNewOffer() {
        if(Data.user != null) {
            Intent next;
            if (Data.user.getProperty("offerer") == null || Data.user.getProperty("offerer").equals("")) {
                next = new Intent(CravingDetails.this, FoodShopWelcome.class);
            } else {
                next = new Intent(CravingDetails.this, FoodShopMain.class);
            }
            next.putExtra("food", craving.food.objectId);
            startActivity(next);
            finish();
        }else {
            Toast.makeText(CravingDetails.this, "Please login first!", Toast.LENGTH_SHORT).show();
        }
    }

    public void goToOffer(String offerID) {
        Intent next = new Intent(getApplicationContext(), OfferDetails.class);
        next.putExtra("offerID", offerID);
        startActivity(next);
    }
}
