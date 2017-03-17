package itsjustaaron.food.FoodShopActivities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;

public class FoodShopWelcome extends AppCompatActivity {

    private ArrayList<Boolean> tagChecks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_shop_welcome);
        final LinearLayout container = (LinearLayout) findViewById(R.id.tagContainer);
        for (int i = 0; i < Data.tags.size(); i++) {
            String tag = Data.tags.get(i);
            final CheckedTextView checkedTextView = new CheckedTextView(this);
            checkedTextView.setText(tag);
            checkedTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkedTextView.isChecked()) {
                        checkedTextView.setCheckMarkDrawable(null);
                        checkedTextView.setChecked(false);
                        tagChecks.set(checkedTextView.getId(), false);
                    } else {
                        checkedTextView.setCheckMarkDrawable(ContextCompat.getDrawable(FoodShopWelcome.this, R.drawable.ic_check));
                        checkedTextView.setChecked(true);
                        tagChecks.set(checkedTextView.getId(), true);
                    }
                }
            });
            checkedTextView.setChecked(false);
            checkedTextView.setId(i);
            container.addView(checkedTextView);
            tagChecks.add(i, false);
        }
        final Button button = new Button(this);
        button.setText("Add");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodShopWelcome.this);
                final EditText input = new EditText(FoodShopWelcome.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setTitle("Enter your tag:");
                alertDialog.setPositiveButton("add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String tag = input.getText().toString().toUpperCase();
                        if (Data.tags.contains(tag)) {
                            Toast.makeText(FoodShopWelcome.this, "There is already a tag with the same name!", Toast.LENGTH_LONG).show();
                        } else {
                            Data.tags.add(tag);

                            new AsyncTask<Void, Void, Integer>() {
                                @Override
                                public Integer doInBackground(Void... voids) {
                                    HashMap<String, String> tagMap = new HashMap<String, String>();
                                    tagMap.put("tag", tag);
                                    tagMap.put("ownerId", Data.user.getObjectId());
                                    Back.store(tagMap, Back.object.tag);
                                    return 0;
                                }

                                @Override
                                public void onPostExecute(Integer integer) {
                                    final CheckedTextView ctv = new CheckedTextView(FoodShopWelcome.this);
                                    ctv.setText(tag);
                                    ctv.setChecked(true);
                                    ctv.setId(Data.tags.size() - 1);
                                    ctv.setCheckMarkDrawable(ContextCompat.getDrawable(FoodShopWelcome.this, R.drawable.ic_check));
                                    ctv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (ctv.isChecked()) {
                                                ctv.setCheckMarkDrawable(null);
                                                ctv.setChecked(false);
                                            } else {
                                                ctv.setCheckMarkDrawable(ContextCompat.getDrawable(FoodShopWelcome.this, R.drawable.ic_check));
                                                ctv.setChecked(true);
                                            }
                                        }
                                    });
                                    container.addView(ctv, container.getChildCount() - 1);
                                    tagChecks.add(ctv.getId(), true);
                                }
                            }.execute();
                        }
                    }
                }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });
        button.setWidth(container.getWidth());
        container.addView(button);
    }

    public void click(View view) {
        final ArrayList<String> tags = new ArrayList<>();
        for (int i = 0; i < Data.tags.size(); i++) {
            if (tagChecks.get(i)) {
                tags.add(Data.tags.get(i));
            }
        }
        final String description = ((EditText) findViewById(R.id.offererDescription)).getText().toString();
        new AsyncTask<Void, Void, Void>() {
            ProgressDialog progressDialog;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(FoodShopWelcome.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
            }

            @Override
            public Void doInBackground(Void... voids) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("tags", Food.listToCsv(tags));
                hashMap.put("userID", Data.user.getObjectId());
                hashMap.put("description", description);
                Back.store(hashMap, Back.object.offerer);
                return null;
            }

            @Override
            public void onPostExecute(Void v) {
                progressDialog.dismiss();
                Intent go = new Intent(FoodShopWelcome.this, FoodShopMain.class);
                startActivity(go);
            }
        }.execute();
    }
}
