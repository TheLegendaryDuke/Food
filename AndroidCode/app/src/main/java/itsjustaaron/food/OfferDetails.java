package itsjustaaron.food;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;

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
        }.execute(new Void[]{});
    }

    public void updateUI() {
        ImageView image = (ImageView) findViewById(R.id.offerDetailFoodImage);
        image.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + offer.food.image));
        ((TextView)findViewById(R.id.offerDetailFoodName)).setText(offer.food.name);
        ((TextView)findViewById(R.id.offerDetailFoodDesc)).setText(offer.food.description);
        ((TextView)findViewById(R.id.offerDetailFoodTags)).setText(offer.food.tags);
        ((TextView)findViewById(R.id.offerDetailFoodComment)).setText(offer.comment);
        ((TextView)findViewById(R.id.offerDetailFoodPrice)).setText(String.valueOf("$" + offer.price));
        ((TextView)findViewById(R.id.offerDetailOfferer)).setText(offer.offerer);
        ((TextView)findViewById(R.id.offerDetailFoodLocation)).setText(offer.address + "\n" + offer.city + "\n" + offer.zipCode);
        ((TextView)findViewById(R.id.offerDetailFoodExpire)).setText(Data.standardDateFormat.format(offer.expire));
    }
}
