package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewCraving extends AppCompatActivity {

    private List<Food> searchResults;

    private ProgressDialog wait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_craving);
        wait = new ProgressDialog(this);
        wait.setTitle("Please Wait...");
        new AlertDialog.Builder(this).setTitle("Do you want to select a existing food or create a new one?").setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                findViewById(R.id.searchFood).setVisibility(View.VISIBLE);
            }
        }).setNegativeButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    public void Search(View view) {
        searchResults = new ArrayList<>();
        String search = ((EditText) findViewById(R.id.searchFoodBox)).getText().toString();
        wait.show();
        List<String> tagResult = Food.csvToList(search);
        boolean tagCheck = true;
        for (int i = 0; i < tagResult.size(); i++) {
            if (!Data.tags.contains(tagResult.get(i))) {
                tagCheck = false;
                break;
            }
        }
        String whereClause = "";
        if (tagCheck) {
            for (int i = 0; i < tagResult.size(); i++) {
                whereClause = whereClause + "tags LIKE '%" + tagResult.get(i) + "%'";
                if (i != tagResult.size() - 1) {
                    whereClause = whereClause + " and ";
                }
            }
        } else {
            whereClause = "name LIKE '%" + search + "%'";
        }
        final BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(whereClause);
        new Thread() {
            @Override
            public void run() {
                Backendless.Persistence.of("Food").find(dataQuery, new AsyncCallback<BackendlessCollection<Map>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                        List<String> foodIDs = new ArrayList<String>();
                        final List<Map> maps = mapBackendlessCollection.getCurrentPage();
                        if (maps.size() != 0) {
                            for (int i = 0; i < maps.size(); i++) {
                                Map map = maps.get(i);
                                searchResults.add(new Food(map));
                            }
                            renderList();
                        } else {
                            Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {

                    }
                });
            }
        }.start();
    }

    private void renderList() {
        ListView listView = (ListView) findViewById(R.id.searchFoodResult);
        foodAdapter adapter = new foodAdapter(this, searchResults);
        listView.setAdapter(adapter);
        wait.dismiss();
    }

    private class foodAdapter extends ArrayAdapter<Food> {
        public final List<Food> values;
        private final Context context;

        public foodAdapter(Context context, List<Food> values) {
            super(context, -1, values);
            if (values == null) {
                values = new ArrayList<Food>();
            }
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.food_list_item, parent, false);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wait.show();
                    new AlertDialog.Builder(context).setTitle("Create a new craving for this food?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new Thread() {
                                @Override
                                public void run() {
                                    Map<String, String> craving = new HashMap<String, String>();
                                    craving.put("foodID", values.get(position).objectId);
                                    craving.put("numFollowers", "1");
                                    craving.put("ownerID", Data.user.getEmail());
                                    Backendless.Persistence.of("Craving").save(craving, new AsyncCallback<Map>() {
                                        @Override
                                        public void handleResponse(Map map) {
                                            Map<String, String> cravingFollower = new HashMap<String, String>();
                                            cravingFollower.put("userID", Data.user.getEmail());
                                            cravingFollower.put("cravingID", map.get("objectId").toString());
                                            try {
                                                Backendless.Persistence.of("cravingFollowers").save(cravingFollower);
                                                if (position == values.size() - 1) {
                                                    wait.dismiss();
                                                }
                                            } catch (Exception e) {
                                                Log.d(e.getMessage(), "handleResponse: ");
                                            }
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault backendlessFault) {
                                            Log.d(backendlessFault.getMessage(), "handleFault: ");
                                        }
                                    });
                                }
                            }.start();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
                }
            });
            ((ImageView) rowView.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(NewCraving.this.getFilesDir() + "/foods/" + values.get(position).image));
            ((TextView) rowView.findViewById(R.id.foodName)).setText(values.get(position).name);
            ((TextView) rowView.findViewById(R.id.foodDescription)).setText(values.get(position).description);
            return rowView;
        }
    }
}
