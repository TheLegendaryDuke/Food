package itsjustaaron.food.FoodActivities;

import android.app.ProgressDialog;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.Offerer;
import itsjustaaron.food.R;
import itsjustaaron.food.Utilities.Helpers;


public class ProfileSetup extends AppCompatActivity {
    private boolean imageUpdated;
    private ProgressDialog d;
    private String portrait;
    private boolean fromShop;
    private Offerer offerer;

    private ArrayList<Boolean> tagChecks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fromShop = getIntent().getStringExtra("source").equals("shop");
        if(fromShop) {
            findViewById(R.id.shopContent).setVisibility(View.VISIBLE);
        }
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        imageUpdated = false;
        setContentView(R.layout.activity_profile_setup);
        d = new ProgressDialog(this);
        d.setMessage("Please wait...");
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolbar);
        portrait = Data.user.getProperty("portrait").toString();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");
        ((TextView) findViewById(R.id.profileEmail)).setText(Data.user.getEmail());
        ((TextView) findViewById(R.id.profileName)).setText(Data.user.getProperty("name") == null ? "" : Data.user.getProperty("name").toString());
        ((EditText) findViewById(R.id.profileCity)).setText(Data.user.getProperty("city") == null ? "" : Data.user.getProperty("city").toString());
        ((EditText) findViewById(R.id.profileZip)).setText(Data.user.getProperty("zipCode") == null ? "" : Data.user.getProperty("zipCode").toString());
        Object address = Data.user.getProperty("address");
        ((EditText) findViewById(R.id.profileAddress)).setText(address == null ? "" : address.toString());
        if (!Data.user.getProperty("portrait").equals("")) {
            final File portrait = new File(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait").toString());
            //portrait must be there for custom-portrait user since Main downloads it
            ((ImageView) findViewById(R.id.profileImage)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
        }
        if(fromShop) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                public Void doInBackground(Void... voids) {
                    offerer = (Offerer) Back.getObjectByID(Data.user.getProperty("offerer").toString(), Back.object.offerer);
                    return null;
                }

                @Override
                public void onPostExecute(Void v) {
                    ((EditText) findViewById(R.id.profileDesc)).setText(offerer.description);

                    ArrayList checkedTags = new ArrayList(Food.csvToList(offerer.tags));
                    final LinearLayout container = (LinearLayout) findViewById(R.id.tagContainer);
                    for (int i = 0; i < Data.tags.size(); i++) {
                        String tag = Data.tags.get(i);
                        final CheckedTextView checkedTextView = new CheckedTextView(ProfileSetup.this);
                        checkedTextView.setText(tag);
                        checkedTextView.setBackgroundResource(Helpers.getTagDrawable(Data.tagColors.get(tag)));
                        checkedTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (checkedTextView.isChecked()) {
                                    checkedTextView.setCheckMarkDrawable(null);
                                    checkedTextView.setChecked(false);
                                    tagChecks.set(checkedTextView.getId(), false);
                                } else {
                                    checkedTextView.setCheckMarkDrawable(ContextCompat.getDrawable(ProfileSetup.this, R.drawable.ic_check));
                                    checkedTextView.setChecked(true);
                                    tagChecks.set(checkedTextView.getId(), true);
                                }
                            }
                        });
                        checkedTextView.setChecked(checkedTags.contains(tag));
                        if (!checkedTextView.isChecked()) {
                            checkedTextView.setCheckMarkDrawable(null);
                        } else {
                            checkedTextView.setCheckMarkDrawable(ContextCompat.getDrawable(ProfileSetup.this, R.drawable.ic_check));
                        }
                        checkedTextView.setId(i);
                        container.addView(checkedTextView);
                        tagChecks.add(i, checkedTags.contains(tag));
                    }
                    final Button button = new Button(ProfileSetup.this);
                    button.setText("Add");

                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileSetup.this);
                            final EditText input = new EditText(ProfileSetup.this);
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
                                        Toast.makeText(ProfileSetup.this, "There is already a tag with the same name!", Toast.LENGTH_LONG).show();
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
                                                final CheckedTextView ctv = new CheckedTextView(ProfileSetup.this);
                                                ctv.setText(tag);
                                                ctv.setChecked(true);
                                                ctv.setId(Data.tags.size() - 1);
                                                ctv.setCheckMarkDrawable(ContextCompat.getDrawable(ProfileSetup.this, R.drawable.ic_check));
                                                ctv.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (ctv.isChecked()) {
                                                            ctv.setCheckMarkDrawable(null);
                                                            ctv.setChecked(false);
                                                        } else {
                                                            ctv.setCheckMarkDrawable(ContextCompat.getDrawable(ProfileSetup.this, R.drawable.ic_check));
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
            }.execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                Uri image = data.getData();
                //crop image activity written by ArthurHub
                //https://github.com/ArthurHub/Android-Image-Cropper
                CropImage.activity(image)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio(1, 1)
                        .setFixAspectRatio(true)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        } else {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    if (portrait.equals("")) {
                        portrait = Data.user.getObjectId() + ".png";
                    }
                    try {
                        Uri resultUri = result.getUri();
                        Bitmap pic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                        imageUpdated = true;
                        File dest = new File(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + portrait);
                        OutputStream out = new FileOutputStream(dest);
                        pic.compress(Bitmap.CompressFormat.PNG, 100, out);
                        ((ImageView) findViewById(R.id.profileImage)).setImageBitmap(pic);
                    }catch (Exception e) {
                        Data.handler.uncaughtException(Thread.currentThread(), e);
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Data.handler.uncaughtException(Thread.currentThread(), result.getError());
                }
            }
        }
    }

    public void saveProfile(View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        String address = ((EditText) findViewById(R.id.profileAddress)).getText().toString();

        Data.user.setProperty("address", address);
        Data.user.setProperty("city", ((EditText) findViewById(R.id.profileCity)).getText().toString());
        Data.user.setProperty("zipCode", ((EditText) findViewById(R.id.profileZip)).getText().toString());
        Data.user.setProperty("portrait", portrait);

        final HashMap<String, String> map = new HashMap<>();
        if(fromShop) {
            map.put("objectId", offerer.objectId);
            map.put("description", ((EditText) findViewById(R.id.profileDesc)).getText().toString());
            map.put("userID", offerer.userID);
            final ArrayList<String> tags = new ArrayList<>();
            for (int i = 0; i < Data.tags.size(); i++) {
                if (tagChecks.get(i)) {
                    tags.add(Data.tags.get(i));
                }
            }
            map.put("tags", Food.listToCsv(tags));
        }

        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... voids) {
                Back.updateUserData();
                if(fromShop) {
                    Back.store(map, Back.object.offerer);
                }
                if (imageUpdated) {
                    try {
                        final File image = new File(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + portrait);
                        Back.upload(image, "users/" + Data.user.getObjectId() + "/", true);
                        File newFile = new File(Data.fileDir + "/offers/offerers/" + portrait);
                        if (!newFile.getParentFile().exists()) {
                            newFile.getParentFile().mkdirs();
                        }
                        OutputStream out = new FileOutputStream(newFile);
                        BitmapFactory.decodeFile(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + portrait).compress(Bitmap.CompressFormat.PNG, 10, out);
                        Back.upload(newFile, "offers/offerers/", true);
                    } catch (Exception e) {
                        Log.e("file", e.toString(), e);
                    }
                }
                return 0;
            }

            @Override
            public void onPostExecute(Integer i) {
                Toast.makeText(getApplicationContext(), "Update successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProfileSetup.this, Main.class));
                progressDialog.dismiss();
                finish();
            }
        }.execute();
    }

    public void pickPhoto(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 0);
    }

    public void passwordReset(View view) {
        new AlertDialog.Builder(ProfileSetup.this).setTitle("Are you sure?").setMessage("Click \"OK\" to reset your password").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                new AsyncTask<Void, Void, Integer>() {

                    @Override
                    public void onPreExecute() {
                        d.show();
                    }

                    @Override
                    public Integer doInBackground(Void... v) {
                        Back.resetPassword();
                        return 0;
                    }

                    @Override
                    public void onPostExecute(Integer i) {
                        d.dismiss();
                        Toast.makeText(getApplicationContext(), "Success, please check your email to set a new password", Toast.LENGTH_LONG).show();
                    }
                }.execute();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }
}
