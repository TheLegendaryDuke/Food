package itsjustaaron.food.FoodActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Back.PagedList;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;

//returns a string "id" as the foodID created/selected, consumes a boolean "onCraving" to indicate calling activity

public class NewFood extends AppCompatActivity {

    private boolean imageUpdated = false;

    private boolean onCraving;

    private Bitmap pic;

    private List<Food> searchResults;

    private ProgressDialog wait;

    private ArrayList<Boolean> tagChecks = new ArrayList<>();

    private Uri rawImage;

    public void pickPhoto(View view) {
        rawImage = null;
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_food);
        searchResults = new ArrayList<>();
        onCraving = getIntent().getBooleanExtra("onCraving", true);
        ListView listView = (ListView) findViewById(R.id.searchFoodResult);
        foodAdapter adapter = new foodAdapter(this, searchResults);
        listView.setAdapter(adapter);
        wait = new ProgressDialog(this);
        wait.setMessage("Please Wait...");
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Do you want to select a existing food or create a new one?")
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        findViewById(R.id.searchFood).setVisibility(View.VISIBLE);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new AlertDialog.Builder(NewFood.this)
                        .setTitle("Caution")
                        .setMessage("Please make sure that you cannot find the food you are looking for before creating a new Food\n(Did you know you can enter tags of a food like \"noodle\" in the search?")
                        .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                findViewById(R.id.searchFood).setVisibility(View.GONE);
                                findViewById(R.id.createFood).setVisibility(View.VISIBLE);
                            }
                        })
                        .setNegativeButton("Go Back to Search", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                findViewById(R.id.searchFood).setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        }).show();
            }
        }).show();

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
                        checkedTextView.setCheckMarkDrawable(ContextCompat.getDrawable(NewFood.this, R.drawable.ic_check));
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
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(NewFood.this);
                final EditText input = new EditText(NewFood.this);
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
                            Toast.makeText(NewFood.this, "There is already a tag with the same name!", Toast.LENGTH_LONG).show();
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
                                    final CheckedTextView ctv = new CheckedTextView(NewFood.this);
                                    ctv.setText(tag);
                                    ctv.setChecked(true);
                                    ctv.setId(Data.tags.size() - 1);
                                    ctv.setCheckMarkDrawable(ContextCompat.getDrawable(NewFood.this, R.drawable.ic_check));
                                    ctv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (ctv.isChecked()) {
                                                ctv.setCheckMarkDrawable(null);
                                                ctv.setChecked(false);
                                            } else {
                                                ctv.setCheckMarkDrawable(ContextCompat.getDrawable(NewFood.this, R.drawable.ic_check));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                Uri image = data.getData();
                rawImage = image;
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                cropIntent.setDataAndType(image, "image/*");
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 10);
                cropIntent.putExtra("aspectY", 10);
                cropIntent.putExtra("outputX", 128);
                cropIntent.putExtra("outputY", 128);
                cropIntent.putExtra("return-data", true);
                startActivityForResult(cropIntent, 1);
            }
        } else {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    imageUpdated = true;
                    pic = data.getExtras().getParcelable("data");
                    ((ImageView) findViewById(R.id.createFoodImage)).setImageBitmap(pic);
                }
            } else {
                if (rawImage != null) {
                    try {
                        pic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), rawImage);
                        pic = Bitmap.createScaledBitmap(pic, 200, 200, true);
                        ((ImageView) findViewById(R.id.createFoodImage)).setImageURI(rawImage);
                        imageUpdated = true;
                    } catch (Exception e) {
                        Log.d("error", e.toString(), e);
                    }
                }
            }
        }
    }

    public void Create(View view) {
        EditText nameView = (EditText) findViewById(R.id.createFoodName);
        EditText descView = (EditText) findViewById(R.id.createFoodDesc);
        if (nameView.getText() == null || nameView.getText().toString().length() == 0) {
            Toast.makeText(NewFood.this, "Please enter a name for the food", Toast.LENGTH_SHORT).show();
            return;
        }
        if (descView.getText() == null || descView.getText().toString().length() == 0) {
            Toast.makeText(NewFood.this, "Please enter a description for the food", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!imageUpdated) {
            Toast.makeText(NewFood.this, "Please select an image for the food", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog pd = new ProgressDialog(NewFood.this);
        pd.setMessage("Please wait...");
        pd.show();
        final String name = nameView.getText().toString();
        String desc = descView.getText().toString();
        try {
            final File dest = new File(Data.fileDir + "/foods/" + name.replaceAll(" ", "-") + ".png");
            OutputStream out = new FileOutputStream(dest);
            pic.compress(Bitmap.CompressFormat.PNG, 100, out);
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", name);
            hashMap.put("description", desc);
            hashMap.put("image", name.replaceAll(" ", "-") + ".png");
            hashMap.put("ownerId", Data.user.getObjectId());
            ArrayList<String> tags = new ArrayList<>();
            for (int i = 0; i < Data.tags.size(); i++) {
                if (tagChecks.get(i)) {
                    tags.add(Data.tags.get(i));
                }
            }
            hashMap.put("tags", Food.listToCsv(tags));
            new AsyncTask<Void, Void, Integer>() {

                Food food = new Food();

                @Override
                public Integer doInBackground(Void... voids) {
                    String whereC = "name = \'" + name + "\'";
                    PagedList<Map> result = Back.findObjectByWhere(whereC, Back.object.food);
                    if (result.getCurPage().size() != 0) {
                        return 2;
                    }
                    Back.upload(dest, "foods/", true);
                    food = new Food(Back.store(hashMap, Back.object.food));
                    Data.foods.add(food);
                    if (onCraving) {
                        Map<String, String> craving = new HashMap<String, String>();
                        craving.put("foodID", food.objectId);
                        craving.put("numFollowers", "1");
                        craving.put("ownerId", Data.user.getObjectId());
                        Map map = Back.store(craving, Back.object.craving);
                        Map<String, String> cravingFollower = new HashMap<String, String>();
                        cravingFollower.put("userID", Data.user.getObjectId());
                        cravingFollower.put("cravingID", map.get("objectId").toString());
                        Back.store(cravingFollower, Back.object.cravingfollower);
                    }
                    return 0;
                }

                @Override
                public void onPostExecute(Integer i) {
                    if (onCraving) {
                        if (i == 0) {
                            Toast.makeText(NewFood.this, "Created successfully.", Toast.LENGTH_SHORT).show();
                            Intent ret = new Intent();
                            ret.putExtra("id", food.objectId);
                            setResult(RESULT_OK, ret);
                            finish();
                        } else if (i == 2) {
                            Toast.makeText(NewFood.this, "A food with the same name already exists, you can find it by searching the food name or tags", Toast.LENGTH_LONG).show();
                            setResult(2);
                            finish();
                        }
                    } else {
                        Intent ret = new Intent();
                        ret.putExtra("id", food.objectId);
                        setResult(RESULT_OK, ret);
                        finish();
                    }
                }
            }.execute();
        } catch (FileNotFoundException e) {
            Toast.makeText(NewFood.this, getString(R.string.error), Toast.LENGTH_LONG).show();
            Log.d("Save pic", e.toString());
        }

    }

    public void Search(View view) {
        searchResults.clear();

        String search = ((EditText) findViewById(R.id.searchFoodBox)).getText().toString().toUpperCase();
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
        final String where = whereClause;
        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... voids) {
                PagedList<Map> result = Back.findObjectByWhere(where, Back.object.food);
                final List<Map> maps = result.getCurPage();
                if (maps.size() != 0) {
                    for (int i = 0; i < maps.size(); i++) {
                        Map map = maps.get(i);
                        Food food = new Food(map);
                        searchResults.add(food);
                        boolean contains = false;
                        for (Food f : Data.foods) {
                            if (f.objectId.equals(food.objectId)) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            Data.foods.add(food);
                        }
                    }
                    return 0;
                } else {
                    return 1;
                }
            }

            @Override
            public void onPostExecute(Integer i) {
                if (i == 0) {
                    ListView listView = (ListView) findViewById(R.id.searchFoodResult);
                    ((foodAdapter) listView.getAdapter()).notifyDataSetChanged();
                } else if (i == 1) {
                    Toast.makeText(NewFood.this, "Your search yields no results", Toast.LENGTH_SHORT).show();
                    foodAdapter adapter = (foodAdapter) ((ListView) findViewById(R.id.searchFoodResult)).getAdapter();
                    adapter.values.clear();
                    adapter.notifyDataSetChanged();
                }
                wait.dismiss();
            }
        }.execute();
    }

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
            final View rowView = inflater.inflate(R.layout.food_list_item, parent, false);
            final File image = new File(Data.fileDir + "/foods/" + values.get(position).image);
            if (!image.exists()) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... voids) {
                        Back.downloadToLocal("/foods/" + values.get(position).image);
                        return null;
                    }

                    @Override
                    public void onPostExecute(Void v) {
                        ((ImageView) rowView.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/foods/" + values.get(position).image));
                    }
                }.execute();
            } else {
                ((ImageView) rowView.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/foods/" + values.get(position).image));
            }
            ((TextView) rowView.findViewById(R.id.foodName)).setText(values.get(position).name);
            ((TextView) rowView.findViewById(R.id.foodDescription)).setText(values.get(position).description);
            List<String> tags = Food.csvToList(values.get(position).tags);
            LinearLayout cont = (LinearLayout) rowView.findViewById(R.id.foodItemTags);
            for (String tag : tags) {
                TextView textView = new TextView(context);
                textView.setText(tag);
                cont.addView(textView);
            }
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog ad = new AlertDialog.Builder(context)
                            .setTitle("Select this food?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialogInterface, int i) {
                                    if (onCraving) {
                                        new AsyncTask<Void, Void, Integer>() {
                                            Food food;

                                            @Override
                                            public void onPreExecute() {
                                                dialogInterface.dismiss();
                                                wait.show();
                                            }

                                            @Override
                                            public Integer doInBackground(Void... voids) {
                                                food = values.get(position);
                                                String whereC = "foodID = \'" + food.objectId + "\'";
                                                PagedList<Map> result = Back.findObjectByWhere(whereC, Back.object.craving);
                                                if (result.getCurPage().size() != 0) {
                                                    return 2;
                                                } else {
                                                    Map<String, String> craving = new HashMap<String, String>();
                                                    craving.put("foodID", food.objectId);
                                                    craving.put("numFollowers", "1");
                                                    craving.put("ownerID", Data.user.getObjectId());
                                                    Map map = Back.store(craving, Back.object.craving);
                                                    Map<String, String> cravingFollower = new HashMap<String, String>();
                                                    cravingFollower.put("userID", Data.user.getObjectId());
                                                    cravingFollower.put("cravingID", map.get("objectId").toString());
                                                    Back.store(cravingFollower, Back.object.cravingfollower);
                                                }
                                                return 0;
                                            }

                                            @Override
                                            public void onPostExecute(Integer x) {
                                                if (x == 0) {
                                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                                    Intent ret = new Intent();
                                                    ret.putExtra("id", food.objectId);
                                                    setResult(RESULT_OK, ret);
                                                    finish();
                                                } else {
                                                    new AlertDialog.Builder(context).setMessage("A craving with this food already exists, you can find it by searching the food name or tags").setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            setResult(2);
                                                            finish();
                                                        }
                                                    }).show();
                                                }
                                                dialogInterface.dismiss();
                                                wait.dismiss();
                                            }
                                        }.execute();
                                    } else {
                                        Intent ret = new Intent();
                                        ret.putExtra("id", values.get(position).objectId);
                                        setResult(RESULT_OK, ret);
                                        finish();
                                    }
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
    }
}