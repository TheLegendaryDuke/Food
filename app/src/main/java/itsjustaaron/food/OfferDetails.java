package itsjustaaron.food;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.backendless.Backendless;

import java.util.Map;

public class OfferDetails extends AppCompatActivity {

    public Offer offer;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                Map map = Backendless.Persistence.of("offers").findById(offerID);
                offer = new Offer(map);
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                updateUI();
            }
        }.execute(new Void[]{});
    }

    public void updateUI() {

    }
}
