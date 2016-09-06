package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;


public class ProfileSetup extends AppCompatActivity {
    protected boolean imageUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageUpdated = false;
        setContentView(R.layout.activity_profile_setup);
        Toolbar toolbar = (Toolbar)findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Back");
        ((TextView)findViewById(R.id.profileEmail)).setText(Data.user.getEmail());
        ((EditText)findViewById(R.id.profileName)).setText(Data.user.getProperty("name").toString());
        Object address = Data.user.getProperty("address");
        ((EditText)findViewById(R.id.profileAddress)).setText(address == null ? "" : address.toString());
        final File portrait = new File(getFilesDir() + "/" + Data.user.getProperty("portrait").toString());
        //portrait must be there for custom-portrait user since Main downloads it
        if(portrait.exists()) {
            ((ImageView)findViewById(R.id.profileImage)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0) {
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
        }else {
            try {
                imageUpdated = true;
                Bitmap result = data.getExtras().getParcelable("data");
                File dest = new File(getFilesDir() + "/" + Data.user.getProperty("portrait").toString());
                OutputStream out = new FileOutputStream(dest);
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
                ((ImageView) findViewById(R.id.profileImage)).setImageBitmap(result);
            }catch (Exception e) {
                Log.d("1", e.getMessage());
            }
        }
    }

    public void saveProfile(View view) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        String name = ((EditText)findViewById(R.id.profileName)).getText().toString();
        String address = ((EditText)findViewById(R.id.profileAddress)).getText().toString();

        Data.user.setProperty("name", name);
        Data.user.setProperty("address", address);

        new Thread() {
            @Override
            public void run() {
                Backendless.UserService.update(Data.user, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {
                        if(imageUpdated) {
                            final File image = new File(getFilesDir() + "/" + Data.user.getProperty("portrait").toString());
                            Backendless.Files.upload(image, "users/" + Data.user.getEmail() + "/", true, new AsyncCallback<BackendlessFile>() {
                                @Override
                                public void handleResponse(BackendlessFile backendlessFile) {
                                    startActivity(new Intent(ProfileSetup.this, Main.class));
                                    finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {
                                    new AlertDialog.Builder(ProfileSetup.this).setMessage(backendlessFault.getMessage()).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).show();
                                }
                            });
                        }else {
                            startActivity(new Intent(ProfileSetup.this, Main.class));
                            finish();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.d(backendlessFault.getCode(), backendlessFault.getMessage());
                    }
                });
            }
        }.run();
    }

    public void pickPhoto(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 0);
    }

    public void passwordReset(View view) {
        new AlertDialog.Builder(ProfileSetup.this).setTitle("Are you sure?").setMessage("Click \"OK\" to change your password").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Backendless.UserService.restorePassword(Data.user.getEmail());
                new AlertDialog.Builder(ProfileSetup.this).setTitle("Success").setMessage("Please check you email to change you password").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }
}
