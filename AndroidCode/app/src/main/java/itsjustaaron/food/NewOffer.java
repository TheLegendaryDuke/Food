package itsjustaaron.food;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;

public class NewOffer extends AppCompatActivity {
    private Food food;
    private HashMap<String, String> offer = new HashMap<>();
    private Date date;
    private boolean defaultAddress = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_offer);
        String foodId = getIntent().getStringExtra("food");
        for(int i = 0; i < Data.foods.size(); i++) {
            if(Data.foods.get(i).objectId.equals(foodId)) {
                food = Data.foods.get(i);
                break;
            }
        }
        ImageView image = (ImageView) findViewById(R.id.newOfferFoodImage);
        image.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + food.image));
        ((TextView)findViewById(R.id.newOfferFoodName)).setText(food.name);
        ((TextView)findViewById(R.id.newOfferFoodDesc)).setText(food.description);
        ((TextView)findViewById(R.id.newOfferFoodTags)).setText(food.tags);
        final Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        String curDate = Data.standardDateFormat.format(date);
        curDate += " 23:59";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd kk:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            date = sdf.parse(curDate);
        }catch (ParseException e) {
            Log.d("dateparse", e.toString());
        }
        ((TextView)findViewById(R.id.newOfferExpire)).setText(Data.standardDateFormat.format(date));
        findViewById(R.id.newOfferChangeExpire).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewOffer.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        try {
                            String d = String.valueOf(year) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(dayOfMonth) + " 23:59";
                            date = new SimpleDateFormat("yyyy/MM/dd kk:mm").parse(d);
                        }catch (Exception e) {
                            Log.d("Dateparse", e.toString());
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        ((RadioButton)findViewById(R.id.newOfferSelectOther)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    findViewById(R.id.newOfferOtherAddress).setVisibility(View.VISIBLE);
                }else {
                    findViewById(R.id.newOfferOtherAddress).setVisibility(View.GONE);
                }
                defaultAddress = !isChecked;
            }
        });
    }

    public void Submit(View v) {
        offer.put("price", ((EditText)findViewById(R.id.newOfferPrice)).getText().toString());
        offer.put("expire", String.valueOf(date.getTime()));
        offer.put("ownerId", Data.user.getObjectId());
        String city,address,zip;
        if(defaultAddress) {
            if(Data.user.getProperty("city") == null || Data.user.getProperty("address") == null || Data.user.getProperty("zipCode") == null) {
                Toast.makeText(this, "Please enter your address information under your profile", Toast.LENGTH_LONG).show();
            }
            city = Data.user.getProperty("city").toString();
            address = Data.user.getProperty("address").toString();
            zip = Data.user.getProperty("zipCode").toString();
        }else {
            city = ((EditText)findViewById(R.id.newOfferCity)).getText().toString();
            address = ((EditText)findViewById(R.id.newOfferAddress)).getText().toString();
            zip = ((EditText)findViewById(R.id.newOfferZip)).getText().toString();
        }
        offer.put("city", city);
        offer.put("address", address);
        offer.put("zipCode", zip);
        offer.put("comment", ((EditText)findViewById(R.id.newOfferComment)).getText().toString());
        offer.put("foodID", food.objectId);
        offer.put("offerer", Data.user.getProperty("name").toString());
        offer.put("offererPortrait", Data.user.getProperty("portrait").toString());
        new AsyncTask<Void, Void, Integer>() {
            ProgressDialog progressDialog = new ProgressDialog(NewOffer.this);
            @Override
            public void onPreExecute() {
                progressDialog.setMessage("Please wait");
                progressDialog.show();
            }

            @Override
            public Integer doInBackground(Void... voids) {
                Map o = Back.store(offer, Back.object.offer);
                HashMap<String, String> foodOffer = new HashMap<>();
                if(!Data.user.getProperty("portrait").equals("")) {
                    try {
                        File newFile = new File(Data.fileDir + "/offers/offerers/" + Data.user.getObjectId() + ".png");
                        Bitmap bitmap = BitmapFactory.decodeFile(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait").toString());
                        if(!newFile.getParentFile().exists()){
                            newFile.getParentFile().mkdirs();
                        }
                        newFile.createNewFile();
                        OutputStream out = new FileOutputStream(newFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 10, out);
                        Back.upload(newFile, "offers/offerers/", true);
                    }catch (Exception e) {
                        Log.e("IO", e.toString(), e);
                    }
                }
                foodOffer.put("offererPortrait", Data.user.getProperty("portrait").toString());
                foodOffer.put("city", offer.get("city"));
                foodOffer.put("expire", offer.get("expire"));
                foodOffer.put("foodID", offer.get("foodID"));
                foodOffer.put("offerer", offer.get("offerer"));
                foodOffer.put("offerID", o.get("objectId").toString());
                foodOffer.put("price", offer.get("price"));
                foodOffer.put("ownerId", Data.user.getObjectId());
                Back.store(foodOffer, Back.object.foodoffer);
                return 0;
            }

            @Override
            public void onPostExecute(Integer i) {
                Toast.makeText(NewOffer.this, "Success", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }
        }.execute(new Void[]{});
    }
}