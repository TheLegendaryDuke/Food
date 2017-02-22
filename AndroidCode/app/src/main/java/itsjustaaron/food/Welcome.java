package itsjustaaron.food;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;


public class Welcome extends AppCompatActivity {

    private ProgressDialog wait;

    Timer timer;

    Boolean timerTrigger = false;

    boolean existedUser = false;

    private void Proceed() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Back.init(getApplicationContext());
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (timerTrigger) {
                    if (timerTrigger) {
                        if (existedUser) {
                            Proceed();
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.CoverImage).setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        timerTrigger = true;
                    }
                }
            }
        }, 2000);
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
        //Check for previous login session
        new AsyncTask<Void, Void, Integer>() {
            //0 is success, 1 is failed(no previous login session available), 2 is error(login session no longer valid)
            @Override
            public Integer doInBackground(Void... voids) {
                int userStats = Back.checkUserSession();
                existedUser = false;
                if(userStats == 0) {
                    existedUser = true;
                }else if(userStats == 1) {
                    return 2;
                }
                if(existedUser){
                    return 0;
                } else {
                    return 1;
                }
            }

            @Override
            public void onPostExecute(Integer result) {
                if(result == 2) {
                    Toast.makeText(Welcome.this, "Your login session has expired.", Toast.LENGTH_SHORT);
                    return;
                }
                if(result != 0) {
                    synchronized (timerTrigger) {
                        if (timerTrigger) {
                            findViewById(R.id.CoverImage).setVisibility(View.GONE);
                        } else {
                            timerTrigger = true;
                        }
                    }
                }else {
                    synchronized (timerTrigger) {
                        if (timerTrigger) {
                            Proceed();
                        } else {
                            timerTrigger = true;
                        }
                    }
                }
            }
        }.execute(new Void[]{});
    }

    public void Login(View view) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.welcome_dialog_login);
        LinearLayout container = (LinearLayout)dialog.findViewById(R.id.welcomeLoginDialog);
        dialog.setCancelable(true);
        container.setLayoutParams(new FrameLayout.LayoutParams((int)Math.round(width * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT));
        Button proceed = (Button)dialog.findViewById(R.id.welcomeDialogLogin);
        EditText passwordBox = (EditText) dialog.findViewById(R.id.loginPassword);
        passwordBox.setTypeface(Typeface.DEFAULT_BOLD);
        passwordBox.setTransformationMethod(new PasswordTransformationMethod());
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = ((TextView) dialog.findViewById(R.id.loginEmail)).getText().toString();
                final String password = ((TextView) dialog.findViewById(R.id.loginPassword)).getText().toString();
                final TextView error = (TextView) dialog.findViewById(R.id.loginError);
                if (email.equals("")) {
                    error.setText("The email seems missing");
                    error.setVisibility(View.VISIBLE);
                } else if (password.equals("")) {
                    error.setText("The password seems missing");
                    error.setVisibility(View.VISIBLE);
                }else{
                    wait.show();
                    error.setVisibility(View.INVISIBLE);
                    final boolean stayLogged = ((CheckBox) dialog.findViewById(R.id.loginRemember)).isChecked();
                    new AsyncTask<Void, Void, Integer>() {
                        String message;

                        @Override
                        //0 is success, 1 is failure
                        public Integer doInBackground(Void... voids) {
                            String errorCode = Back.login(email, password, stayLogged);
                            if(errorCode.equals("")) {
                                return 0;
                            }else if (errorCode.equals("3003")) {
                                message = "Invalid login or password! Please try again.";
                            } else if (errorCode.equals("3006")) {
                                message = "Please enter your email and password!";
                            } else if (errorCode.equals("3036")) {
                                message = "Too many failed attempts, account is reset! Check your entered email for new password";
                                Back.resetPassword(email);
                            } else {
                                message = "Error code " + errorCode + ", please contact developer at contactfoodapp@gmail.com";
                            }
                            return 1;
                        }

                        @Override
                        public void onPostExecute(Integer result) {
                            if (result == 0) {
                                wait.dismiss();
                                dialog.dismiss();
                                Proceed();
                            } else {
                                error.setText(message);
                                error.setVisibility(View.VISIBLE);
                                wait.dismiss();
                            }
                        }
                    }.execute(new Void[]{});
                }
            }
        });
        dialog.findViewById(R.id.welcomeDialogLoginCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void Register(View view) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.welcome_dialog_register);
        LinearLayout container = (LinearLayout)dialog.findViewById(R.id.welcomeRegisterDialog);
        dialog.setCancelable(true);
        container.setLayoutParams(new FrameLayout.LayoutParams((int)Math.round(width * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT));
        Button proceed = (Button) dialog.findViewById(R.id.welcomeDialogRegister);
        EditText passwordBox = (EditText) dialog.findViewById(R.id.registerPassword);
        passwordBox.setTypeface(Typeface.DEFAULT_BOLD);
        passwordBox.setTransformationMethod(new PasswordTransformationMethod());
        EditText passwordBox2 = (EditText) dialog.findViewById(R.id.registerPasswordAgain);
        passwordBox2.setTypeface(Typeface.DEFAULT_BOLD);
        passwordBox2.setTransformationMethod(new PasswordTransformationMethod());
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog wait = new ProgressDialog(Welcome.this);
                wait.setMessage("Loading...");
                final String email = ((TextView) dialog.findViewById(R.id.registerEmail)).getText().toString();
                final String password = ((TextView) dialog.findViewById(R.id.registerPassword)).getText().toString();
                final String name = ((TextView) dialog.findViewById(R.id.registerName)).getText().toString();
                String passwordAgain = ((TextView) dialog.findViewById(R.id.registerPasswordAgain)).getText().toString();
                final TextView error = (TextView) dialog.findViewById(R.id.registerError);
                if (email.equals("")) {
                    error.setText("The email seems missing");
                    error.setVisibility(View.VISIBLE);
                } else if (password.equals("")) {
                    error.setText("The password seems missing");
                    error.setVisibility(View.VISIBLE);
                } else if (name.equals("")) {
                    error.setText("The nickname seems missing");
                    error.setVisibility(View.VISIBLE);
                } else if (passwordAgain.equals("")) {
                    error.setText("Please enter the password again in the second box");
                    error.setVisibility(View.VISIBLE);
                } else if (!passwordAgain.equals(password)) {
                    error.setText("The two passwords entered didn't match");
                    error.setVisibility(View.VISIBLE);
                } else {
                    error.setVisibility(View.INVISIBLE);
                    wait.show();
                    new AsyncTask<Void, Void, Integer>() {
                        String message;

                        @Override
                        public Integer doInBackground(Void... voids) {
                            String errorCode = Back.registerUser(email, password, name);
                            if(errorCode.equals("")) {
                                return 0;
                            }else if (errorCode.equals("3011")) {
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

                        @Override
                        public void onPostExecute(Integer result) {
                            if (result == 0) {
                                new AlertDialog.Builder(Welcome.this).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        dialog.dismiss();
                                        Proceed();
                                    }
                                }).setTitle("Your account has been created!").setMessage("Please check your email to activate your account or you won't be able to login next time").show();
                            } else {
                                wait.dismiss();
                                error.setText(message);
                                error.setVisibility(View.VISIBLE);
                            }
                        }
                    }.execute(new Void[]{});
                }
            }
        });
        dialog.findViewById(R.id.welcomeDialogRegisterCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void Guest(View view) {
        Data.user = null;
        //Go to next activity
        Proceed();
    }
}
