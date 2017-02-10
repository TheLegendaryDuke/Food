package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.exceptions.BackendlessException;
import com.backendless.persistence.local.UserIdStorageFactory;


public class Welcome extends AppCompatActivity {

    private ProgressDialog wait;

    private void Proceed() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo == null || !netInfo.isConnectedOrConnecting()) {
            new AlertDialog.Builder(this).setCancelable(false).setMessage("Please check your network connection!").setTitle("No Internet").setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
        wait = new ProgressDialog(Welcome.this);
        wait.setMessage("Please wait...");
        String appVersion = "v1";
        Backendless.initApp(this, "0020F1DC-E584-AD36-FF74-6D3E9E917400", "7DCC75D9-058A-6830-FF54-817317E0C000", appVersion);
        //Check for previous login session
        new AsyncTask<Void, Void, Integer>() {
            //0 is success, 1 is failed(no previous login session available), 2 is error(login session no longer valid)
            @Override
            public Integer doInBackground(Void... voids) {
                try {
                    boolean aBoolean = Backendless.UserService.isValidLogin();
                    if (aBoolean) {
                        String userID = UserIdStorageFactory.instance().getStorage().get();
                        Data.user = Backendless.Data.of(BackendlessUser.class).findById(userID);
                        Proceed();
                        return 0;
                    } else {
                        return 1;
                    }
                } catch (Exception e) {
                    Log.d("backendless", e.toString());
                    return 2;
                }
            }

            @Override
            public void onPostExecute(Integer result) {
                if (result == 2) {
                    Toast.makeText(getApplicationContext(), "Your login session has expired!", Toast.LENGTH_SHORT).show();
                }
                findViewById(R.id.CoverImage).setVisibility(View.GONE);
                findViewById(R.id.WelcomeBG).setVisibility(View.GONE);
            }
        }.execute(new Void[]{});
    }

    public void Login(View view) {
        //create a really big form as an alertdialog(not sure what other option is available without starting a new activity)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View popup = inflater.inflate(R.layout.login, null);
        popup.findViewById(R.id.loginName).setVisibility(View.GONE);
        builder.setView(popup)
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        wait.show();
                        final String email = ((TextView) popup.findViewById(R.id.loginEmail)).getText().toString();
                        final String password = ((TextView) popup.findViewById(R.id.loginPassword)).getText().toString();
                        if (email.equals("") || password.equals("")) {
                            new AlertDialog.Builder(Welcome.this).setTitle("Error").setMessage("Please enter your email and password!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                        } else {
                            final boolean stayLogged = ((CheckBox) popup.findViewById(R.id.stayLogged)).isChecked();
                            new AsyncTask<Void, Void, Integer>() {
                                String message;

                                @Override
                                //0 is success, 1 is failure
                                public Integer doInBackground(Void... voids) {
                                    try {
                                        Data.user = Backendless.UserService.login(email, password, stayLogged);
                                        return 0;
                                    } catch (BackendlessException e) {
                                        String errorCode = e.getCode();
                                        if (errorCode.equals("3003")) {
                                            message = "Invalid login or password! Please try again.";
                                        } else if (errorCode.equals("3006")) {
                                            message = "Please enter your email and password!";
                                        } else if (errorCode.equals("3036")) {
                                            message = "Too many failed attempts, account is reset! Check your entered email for new password";
                                            Backendless.UserService.restorePassword(email);
                                        } else {
                                            message = "Error code " + errorCode + ", please contact developer at contactfoodapp@gmail.com";
                                        }
                                        return 1;
                                    }
                                }

                                @Override
                                public void onPostExecute(Integer result) {
                                    if (result == 0) {
                                        Proceed();
                                    } else {
                                        new AlertDialog.Builder(Welcome.this)
                                                .setTitle(message)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).show();
                                        wait.dismiss();
                                    }
                                }
                            }.execute(new Void[]{});
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public void Register(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View popup = inflater.inflate(R.layout.login, null);
        popup.findViewById(R.id.stayLogged).setVisibility(View.GONE);
        builder.setView(popup)
                .setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog wait = new ProgressDialog(Welcome.this);
                        wait.setMessage("Loading...");
                        wait.show();
                        final String email = ((TextView) popup.findViewById(R.id.loginEmail)).getText().toString();
                        String password = ((TextView) popup.findViewById(R.id.loginPassword)).getText().toString();
                        String name = ((TextView) popup.findViewById(R.id.loginName)).getText().toString();
                        if (email.equals("") || password.equals("") || name.equals("")) {
                            new AlertDialog.Builder(Welcome.this).setTitle("Try Again").setMessage("Please fill all the text boxes!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                            wait.dismiss();
                        } else {
                            final BackendlessUser user = new BackendlessUser();
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setProperty("name", name);
                            user.setProperty("portrait", "");
                            new AsyncTask<Void, Void, Integer>() {
                                String message;

                                @Override
                                public Integer doInBackground(Void... voids) {
                                    try {
                                        Data.user = Backendless.UserService.register(user);
                                        return 0;
                                    } catch (BackendlessException e) {
                                        String errorCode = e.getCode();
                                        if (errorCode.equals("3011")) {
                                            message = "Please enter a password.";
                                        } else if (errorCode.equals("3013")) {
                                            message = "Please enter your email.";
                                        } else if (errorCode.equals("3033")) {
                                            message = "This email address is already taken.";
                                        } else if (errorCode.equals("3040")) {
                                            message = "Please enter a valid email address.";
                                        } else {
                                            message = "Error code" + errorCode + ", please contact contactfoodapp@gmail.com";
                                        }
                                        return 1;
                                    }
                                }

                                @Override
                                public void onPostExecute(Integer result) {
                                    if (result == 0) {
                                        new AlertDialog.Builder(Welcome.this).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                                Proceed();
                                            }
                                        }).setTitle("Your account has been created!").setMessage("Please check your email to activate your account or you won't be able to login next time").show();
                                    } else {
                                        wait.dismiss();
                                        new AlertDialog.Builder(Welcome.this).setTitle("Error").setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).show();
                                    }
                                }
                            }.execute(new Void[]{});
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    public void Guest(View view) {
        Data.user = null;
        //Go to next activity
        Proceed();
    }
}
