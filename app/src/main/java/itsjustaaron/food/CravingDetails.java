package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CravingDetails extends AppCompatActivity {

    private Craving craving;

    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                Map map = Backendless.Persistence.of("cravings").findById(cravingID);
                craving = new Craving(map);
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                updateUI();
            }
        }.execute(new Void[]{});
    }

    public void updateUI() {
        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.detailBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //poopulate form
        ImageView imageView = (ImageView) findViewById(R.id.detailImage);
        imageView.setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/foods/" + craving.food.image));
        TextView name = (TextView) findViewById(R.id.detailName);
        name.setText(craving.food.name);
        List<String> tags = Food.csvToList(craving.food.tags);
        String list = "";
        for (int i = 0; i < tags.size(); i++) {
            list = list + tags.get(i) + "\n";
        }
        ((TextView) findViewById(R.id.detailTags)).setText(list);
        ((TextView) findViewById(R.id.detailDescription)).setText(craving.food.description);

        Button propose = (Button) findViewById(R.id.proposeOffer);
        propose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next = new Intent(getApplicationContext(), NewOffer.class);
                next.putExtra("foodID", craving.food.objectId);
                startActivity(next);
            }
        });
        new AsyncTask<Void, Void, Void>() {
            List<String> offers;
            List<String> offerIDs;

            @Override
            public Void doInBackground(Void... voids) {
                offers = null;
                BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                String where = "foodID='" + craving.food.objectId + "'";
                backendlessDataQuery.setWhereClause(where);
                BackendlessCollection<Map> mapBackendlessCollection = Backendless.Persistence.of("foodOffers").find(backendlessDataQuery);
                final List<Map> temp = mapBackendlessCollection.getCurrentPage();
                if (temp.size() != 0) {
                    offerIDs = new ArrayList<>();
                    for (int i = 0; i < temp.size(); i++) {
                        offerIDs.add(temp.get(i).get("offerID").toString());
                    }
                    offers = new ArrayList<>();
                    for (int i = 0; i < offerIDs.size(); i++) {
                        try {
                            Map offer = Backendless.Persistence.of("Offer").findById(offerIDs.get(i));
                            StringBuilder newS = new StringBuilder();
                            newS.append(offer.get("name"));
                            newS.append(": $");
                            newS.append(offer.get("price"));
                            newS.append("\n");
                            newS.append(offer.get("address"));
                            offers.add(i, newS.toString());
                        } catch (Exception e) {
                            Log.d(e.getMessage(), "handleResponse: ");
                        }
                    }
                }
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                if (offers != null) {
                    ListView offerList = (ListView) findViewById(R.id.detailOffers);
                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, offers);
                    offerList.setAdapter(adapter);
                    offerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String offer = offerIDs.get(i);
                            Intent next = new Intent(getApplicationContext(), OfferDetails.class);
                            next.putExtra("offerID", offer);
                            startActivity(next);
                        }
                    });
                } else {
                    findViewById(R.id.detailOffers).setVisibility(View.GONE);
                }
                progress.dismiss();
            }
        }.execute(new Void[]{});
    }
}
