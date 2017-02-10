package itsjustaaron.food;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.exceptions.BackendlessException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class NewOffer extends AppCompatActivity {
    private Food food;
    private HashMap<String, String> offer = new HashMap<>();
    private Date date;
    private boolean defaultAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        new AsyncTask<Void, Void, Integer>() {
            @Override
            public void onPreExecute() {
                ProgressDialog progressDialog = new ProgressDialog(NewOffer.this);
                progressDialog.setMessage("Please wait");
                progressDialog.show();
            }

            @Override
            public Integer doInBackground(Void... voids) {
                try {
                    Map o = Backendless.Persistence.of("offers").save(offer);
                    HashMap<String, String> foodOffer = new HashMap<>();
                    foodOffer.put("city", offer.get("city"));
                    foodOffer.put("expire", offer.get("expire"));
                    foodOffer.put("foodID", offer.get("foodID"));
                    foodOffer.put("offerer", offer.get("offerer"));
                    foodOffer.put("offerID", o.get("objectId").toString());
                    foodOffer.put("price", offer.get("price"));
                    foodOffer.put("ownerId", Data.user.getObjectId());
                    Backendless.Persistence.of("foodOffers").save(foodOffer);
                    return 0;
                }catch (BackendlessException e) {
                    Log.d("backendless", e.toString());
                    return 1;
                }catch (Exception e) {
                    Log.d("food", e.toString());
                    return 1;
                }
            }

            @Override
            public void onPostExecute(Integer i) {
                if(i == 0) {
                    Toast.makeText(NewOffer.this, "Success", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(NewOffer.this, R.string.error, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(new Void[]{});
    }
}
