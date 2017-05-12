package itsjustaaron.food.FoodActivities;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.Helpers;

public class OfferDetails extends AppCompatActivity {

    public Offer offer;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_details);
        final String offerID = getIntent().getStringExtra("offerID");
        progress = new ProgressDialog(this);
        progress.setMessage("Please wait...");

        new AsyncTask<Void, Void, Void>() {
            @Override
            public void onPreExecute() {
                progress.show();
            }

            @Override
            public Void doInBackground(Void... voids) {
                offer = (Offer) Back.getObjectByID(offerID, Back.object.offer);
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                updateUI();
                progress.dismiss();
            }
        }.execute();
    }

    public void updateUI() {
        ImageView image = (ImageView) findViewById(R.id.offerDetailFoodImage);
        ImageView mat = (ImageView) findViewById(R.id.offerDetailFoodImageMat);
        image.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + offer.food.image));
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        CardView cardView = (CardView) findViewById(R.id.cardView);
        mat.setLayoutParams(new RelativeLayout.LayoutParams(width, (int)(width * 0.526)));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(width * 0.526) - 100, (int)(width * 0.526) - 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        image.setLayoutParams(new FrameLayout.LayoutParams((int)(width * 0.526) - 100, (int)(width * 0.526) - 100));
        cardView.setLayoutParams(params);
        cardView.setRadius((int)((width * 0.526 - 100) / 2));
        ((TextView) findViewById(R.id.offerDetailFoodName)).setText(offer.food.name);
        ((TextView) findViewById(R.id.offerDetailFoodDesc)).setText(offer.food.description);
        LinearLayout tags = (LinearLayout) findViewById(R.id.offerDetailFoodTags);
        List<String> tagList = Food.csvToList(offer.food.tags);
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
        ((TextView) findViewById(R.id.offerDetailFoodComment)).setText(offer.comment);
        ((TextView) findViewById(R.id.offerDetailFoodPrice)).setText(String.valueOf("$" + offer.price));
        ((TextView) findViewById(R.id.offerDetailOfferer)).setText(offer.offerer);
        ((TextView) findViewById(R.id.offerDetailFoodLocation)).setText(offer.address + "\n" + offer.city + "\n" + offer.zipCode);
        ((TextView) findViewById(R.id.offerDetailFoodExpire)).setText(Data.standardDateFormat.format(offer.expire));
        ((ImageView) findViewById(R.id.offerDetailOffererImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/offers/offerers/" + offer.offererPortrait));
    }
}
