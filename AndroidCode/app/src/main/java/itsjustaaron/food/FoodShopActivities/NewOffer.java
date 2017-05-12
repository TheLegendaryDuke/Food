package itsjustaaron.food.FoodShopActivities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
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
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.Helpers;

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
        for (int i = 0; i < Data.foods.size(); i++) {
            if (Data.foods.get(i).objectId.equals(foodId)) {
                food = Data.foods.get(i);
                break;
            }
        }
        //todo: allow user to add their photo of the food
        ImageView image = (ImageView) findViewById(R.id.newOfferFoodImage);
        ImageView mat = (ImageView) findViewById(R.id.newOfferMat);
        image.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + food.image));

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        CardView cardView = (CardView) findViewById(R.id.newOfferCardView);
        mat.setLayoutParams(new RelativeLayout.LayoutParams(width, (int)(width * 0.526)));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(width * 0.526) - 100, (int)(width * 0.526) - 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        image.setLayoutParams(new FrameLayout.LayoutParams((int)(width * 0.526) - 100, (int)(width * 0.526) - 100));
        cardView.setLayoutParams(params);
        cardView.setRadius((int)((width * 0.526 - 100) / 2));
        LinearLayout tags = (LinearLayout) findViewById(R.id.newOfferFoodTags);
        List<String> tagList = Food.csvToList(food.tags);
        tags.removeAllViews();
        for(String tag : tagList) {
            int resID = Helpers.getTagDrawable(Data.tagColors.get(tag));
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextSize(20);
            tagView.setBackgroundResource(resID);
            tagView.setPadding(7,5,7,5);
            LinearLayout.LayoutParams tagParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagParams.setMargins(0,0,20,0);
            tags.addView(tagView, tagParams);
        }
        ((TextView) findViewById(R.id.newOfferFoodName)).setText(food.name);
        ((TextView) findViewById(R.id.newOfferFoodDesc)).setText(food.description);
        final Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        String curDate = Data.standardDateFormat.format(date);
        curDate += " 23:59";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd kk:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        try {
            date = sdf.parse(curDate);
        } catch (ParseException e) {
            Log.d("dateparse", e.toString());
        }
        ((TextView) findViewById(R.id.newOfferExpire)).setText(Data.standardDateFormat.format(date));
        findViewById(R.id.newOfferChangeExpire).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewOffer.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        try {
                            String d = String.valueOf(year) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(dayOfMonth) + " 23:59";
                            date = new SimpleDateFormat("yyyy/MM/dd kk:mm").parse(d);
                        } catch (Exception e) {
                            Log.d("Dateparse", e.toString());
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        ((RadioButton) findViewById(R.id.newOfferSelectOther)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.newOfferOtherAddress).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.newOfferOtherAddress).setVisibility(View.GONE);
                }
                defaultAddress = !isChecked;
            }
        });
    }

    public void Submit(View v) {

        String price = ((EditText) findViewById(R.id.newOfferPrice)).getText().toString();
        String comment = ((EditText) findViewById(R.id.newOfferComment)).getText().toString();

        if(price.equals("")) {
            Toast.makeText(this, "Please enter a price", Toast.LENGTH_SHORT).show();
            return;
        }
        if(comment.equals("")) {
            Toast.makeText(this, "Please enter a description for this item", Toast.LENGTH_SHORT).show();
            return;
        }

        offer.put("price", price);
        offer.put("expire", String.valueOf(date.getTime()));
        offer.put("ownerId", Data.user.getObjectId());
        String city, address, zip;
        if (defaultAddress) {
            if (Data.user.getProperty("city") == null || Data.user.getProperty("address") == null || Data.user.getProperty("zipCode") == null) {
                Toast.makeText(this, "Please enter your address information under your profile", Toast.LENGTH_LONG).show();
            }
            city = Data.user.getProperty("city").toString();
            address = Data.user.getProperty("address").toString();
            zip = Data.user.getProperty("zipCode").toString();
        } else {
            city = ((EditText) findViewById(R.id.newOfferCity)).getText().toString();
            address = ((EditText) findViewById(R.id.newOfferAddress)).getText().toString();
            zip = ((EditText) findViewById(R.id.newOfferZip)).getText().toString();
        }
        offer.put("city", city);
        offer.put("address", address);
        offer.put("zipCode", zip);
        offer.put("comment", comment);
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
                if (!Data.user.getProperty("portrait").equals("")) {
                    try {
                        File newFile = new File(Data.fileDir + "/offers/offerers/" + Data.user.getObjectId() + ".png");
                        Bitmap bitmap = BitmapFactory.decodeFile(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait").toString());
                        if (!newFile.getParentFile().exists()) {
                            newFile.getParentFile().mkdirs();
                        }
                        newFile.createNewFile();
                        OutputStream out = new FileOutputStream(newFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
                        Back.upload(newFile, "offers/offerers/", true);
                    } catch (Exception e) {
                        Log.e("IO", e.toString(), e);
                    }
                }
                Data.menu.add(new Offer(o));
                return 0;
            }

            @Override
            public void onPostExecute(Integer i) {
                Data.menuUpdated = true;
                Toast.makeText(NewOffer.this, "Success", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                finish();
            }
        }.execute();
    }
}
