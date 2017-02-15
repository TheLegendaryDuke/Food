package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;


public class ProfileSetup extends AppCompatActivity {
    protected boolean imageUpdated;
    private ProgressDialog d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageUpdated = false;
        setContentView(R.layout.activity_profile_setup);
        Data.UI = this;
        d = new ProgressDialog(this);
        d.setMessage("Please wait...");
        Toolbar toolbar = (Toolbar) findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");
        ((TextView) findViewById(R.id.profileEmail)).setText(Data.user.getEmail());
        ((EditText) findViewById(R.id.profileName)).setText(Data.user.getProperty("name").toString());
        ((EditText)findViewById(R.id.profileCity)).setText(Data.user.getProperty("city").toString());
        ((EditText)findViewById(R.id.profileZip)).setText(Data.user.getProperty("zipCode").toString());
        Object address = Data.user.getProperty("address");
        ((EditText) findViewById(R.id.profileAddress)).setText(address == null ? "" : address.toString());
        final File portrait = new File(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait").toString());
        //portrait must be there for custom-portrait user since Main downloads it
        if (portrait.exists()) {
            ((ImageView) findViewById(R.id.profileImage)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                Uri image = data.getData();
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
            try {
                imageUpdated = true;
                Bitmap result = data.getExtras().getParcelable("data");
                File dest = new File(Data.fileDir + "/" + Data.user.getProperty("portrait").toString());
                OutputStream out = new FileOutputStream(dest);
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
                ((ImageView) findViewById(R.id.profileImage)).setImageBitmap(result);
            } catch (Exception e) {
                Log.d("1", e.getMessage());
            }
        }
    }

    public void saveProfile(View view) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        String name = ((EditText) findViewById(R.id.profileName)).getText().toString();
        String address = ((EditText) findViewById(R.id.profileAddress)).getText().toString();

        Data.user.setProperty("name", name);
        Data.user.setProperty("address", address);
        Data.user.setProperty("city", ((EditText)findViewById(R.id.profileCity)).getText().toString());
        Data.user.setProperty("zipCode", ((EditText)findViewById(R.id.profileZip)).getText().toString());

        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... voids) {
                Back.updateUserData();
                if (imageUpdated) {
                    final File image = new File(Data.fileDir + "/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait").toString());
                    Back.upload(image, "users/" + Data.user.getObjectId() + "/", true);
                }
                return 0;
            }

            @Override
            public void onPostExecute(Integer i) {
                Toast.makeText(getApplicationContext(), "Update successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProfileSetup.this, Main.class));
                finish();
            }
        }.execute(new Void[]{});
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
                }.execute(new Void[]{});
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }
}
