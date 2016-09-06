package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CravingDetails extends AppCompatActivity {

    private Craving craving;

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_craving_details);
        progress = new ProgressDialog(this);
        progress.setMessage("Please wait...");
        progress.show();
        final String cravingID = getIntent().getStringExtra("cravingID");

        new Thread() {
            @Override
            public synchronized void run() {
                Backendless.Persistence.of("Craving").findById(cravingID, new AsyncCallback<Map>() {
                    @Override
                    public void handleResponse(Map map) {
                        craving = new Craving(map, new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {

                    }
                });
            }
        }.start();

    }

    public void updateUI() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //toolbar
                    Toolbar toolbar = (Toolbar) findViewById(R.id.detailBar);
                    setSupportActionBar(toolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    //poopulate form
                    ImageView imageView = (ImageView) findViewById(R.id.detailImage);
                    imageView.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + craving.food.image));
                    TextView name = (TextView) findViewById(R.id.detailName);
                    name.setText(craving.food.name);
                    List<String> tags = Food.csvToList(craving.food.tags);
                    String list = "";
                    for (int i = 0; i < tags.size(); i++) {
                        list = list + tags.get(i) + "\n";
                    }
                    ((TextView) findViewById(R.id.detailTags)).setText(list);
                    ((TextView) findViewById(R.id.detailDescription)).setText(craving.food.description);
                    final BackendlessDataQuery backendlessDataQuery = new BackendlessDataQuery();
                    String where = "foodID='" + craving.food.objectId + "'";
                    backendlessDataQuery.setWhereClause(where);
                    new Thread() {
                        @Override
                        public void run() {
                            Backendless.Persistence.of("foodOffer").find(backendlessDataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
                                @Override
                                public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                                    final List<Map> temp = mapBackendlessCollection.getCurrentPage();
                                    if (temp.size() != 0) {
                                        ListView offerList = (ListView)findViewById(R.id.detailOffers);
                                        final List<String> offerIDs = new ArrayList<String>();
                                        for(int i = 0; i < temp.size(); i++) {
                                            offerIDs.add(temp.get(i).get("offerID").toString());
                                        }
                                        List<String> offers = new ArrayList<String>();
                                        for(int i = 0; i < offerIDs.size(); i++) {
                                            try {
                                                Map offer = Backendless.Persistence.of("Offer").findById(offerIDs.get(i));
                                                StringBuilder newS = new StringBuilder();
                                                newS.append(offer.get("name"));
                                                newS.append(": $");
                                                newS.append(offer.get("price"));
                                                newS.append("\n");
                                                newS.append(offer.get("address"));
                                                offers.add(newS.toString());
                                            }catch (Exception e) {
                                                Log.d(e.getMessage(), "handleResponse: ");
                                            }
                                        }
                                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, offers);
                                        offerList.setAdapter(adapter);
                                        offerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                String Fooooooood = temp.get(i).get("foodID").toString();
                                                Intent next = new Intent(getApplicationContext(), OfferDetails.class);
                                                next.putExtra("foodID", Fooooooood);
                                                startActivity(next);
                                            }
                                        });

                                    } else {
                                        findViewById(R.id.detailOffersContainer).setVisibility(View.GONE);
                                    }
                                    Button propose = (Button)findViewById(R.id.proposeOffer);
                                    propose.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent next = new Intent(getApplicationContext(), NewOffer.class);
                                            next.putExtra("foodID", craving.food.objectId);
                                            startActivity(next);
                                            finish();
                                        }
                                    });
                                    progress.dismiss();
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {

                                }
                            });
                        }
                    }.start();
                }
            });
        }catch (Exception e) {
            Log.d(e.getMessage(), "updateUI: ");;
        }
    }
}
