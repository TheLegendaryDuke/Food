package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class NewCraving extends AppCompatActivity {

    private List<Food> searchResults;

    private ProgressDialog wait;

    private class foodAdapter extends ArrayAdapter<Food> {
        public List<Food> values;
        private Context context;

        public foodAdapter(Context context, List<Food> values) {
            super(context, -1, values);
            if (values == null) {
                values = new ArrayList<Food>();
            }
            this.values = values;
            this.context = context;
        }

        @Override
        public View getView(final int position, final View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.food_list_item, parent, false);
            ((ImageView) rowView.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(context.getFilesDir() + "/foods/" + values.get(position).image));
            ((TextView) rowView.findViewById(R.id.foodName)).setText(values.get(position).name);
            ((TextView) rowView.findViewById(R.id.foodDescription)).setText(values.get(position).description);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog ad = new AlertDialog.Builder(context)
                            .setTitle("Create a new craving for this food?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, int i) {
                                    new AsyncTask<Void, Void, Integer>() {
                                        @Override
                                        public void onPreExecute() {
                                            dialogInterface.dismiss();
                                            wait.show();
                                        }

                                        @Override
                                        public Integer doInBackground(Void... voids) {
                                            try {
                                                Map<String, String> craving = new HashMap<String, String>();
                                                craving.put("foodID", values.get(position).objectId);
                                                craving.put("numFollowers", "1");
                                                craving.put("ownerID", Data.user.getEmail());
                                                Map map = Backendless.Persistence.of("Craving").save(craving);
                                                Map<String, String> cravingFollower = new HashMap<String, String>();
                                                cravingFollower.put("userID", Data.user.getEmail());
                                                cravingFollower.put("cravingID", map.get("objectId").toString());
                                                Backendless.Persistence.of("cravingFollowers").save(cravingFollower);
                                            } catch (BackendlessException e) {
                                                Log.d("backendless", e.toString());
                                                return 1;
                                            }
                                            return 0;
                                        }

                                        @Override
                                        public void onPostExecute(Integer x) {
                                            if (x == 0) {
                                                Toast.makeText(context, "Succeeded", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                Toast.makeText(context, "Error, please contact the developer if the problem persists", Toast.LENGTH_LONG).show();
                                            }
                                            dialogInterface.dismiss();
                                            wait.dismiss();
                                        }
                                    }.execute(new Void[]{});
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).create();
                    ad.show();
                }
            });
            return rowView;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_craving);
        searchResults = new ArrayList<>();
        ListView listView = (ListView) findViewById(R.id.searchFoodResult);
        foodAdapter adapter = new foodAdapter(this, searchResults);
        listView.setAdapter(adapter);
        wait = new ProgressDialog(this);
        wait.setMessage("Please Wait...");
        new AlertDialog.Builder(this).setTitle("Do you want to select a existing food or create a new one?").setPositiveButton("Select", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                findViewById(R.id.searchFood).setVisibility(View.VISIBLE);
                dialogInterface.dismiss();
            }
        }).setNegativeButton("Create", new DialogInterface.OnClickListener() {
            //TODO: finish this
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }



    public void Search(View view) {
        searchResults.clear();

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
        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... voids) {
                try {
                    BackendlessCollection<Map> mapBackendlessCollection = Backendless.Persistence.of("Food").find(dataQuery);
                    final List<Map> maps = mapBackendlessCollection.getCurrentPage();
                    if (maps.size() != 0) {
                        for (int i = 0; i < maps.size(); i++) {
                            Map map = maps.get(i);
                            searchResults.add(new Food(map));
                        }
                        return 0;
                    } else {
                        return 1;
                    }
                } catch (BackendlessException e) {
                    Log.d("backendless", e.toString());
                    return 2;
                }
            }

            @Override
            public void onPostExecute(Integer i) {
                if (i == 0) {
                    ListView listView = (ListView) findViewById(R.id.searchFoodResult);
                    ((foodAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (i == 1) {
                    Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: your search has failed, please contact the developer if the issue presists", Toast.LENGTH_LONG).show();
                }
                wait.dismiss();
            }
        }.execute(new Void[]{});
    }
}
