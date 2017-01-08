package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.backendless.persistence.local.UserTokenStorageFactory;

public class Welcome extends AppCompatActivity {

    private void Proceed() {
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        String appVersion = "v1";
        //Backendless.Init ommitted
        new Thread() {
            @Override
            public void run() {
                Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
                    @Override
                    public void handleResponse(Boolean aBoolean) {

                        if(aBoolean) {
                            String userID = UserIdStorageFactory.instance().getStorage().get();
                            Backendless.Data.of(BackendlessUser.class).findById(userID, new AsyncCallback<BackendlessUser>() {
                                @Override
                                public void handleResponse(BackendlessUser backendlessUser) {
                                    Data.user = backendlessUser;
                                    Proceed();
                                }

                                @Override
                                public void handleFault(BackendlessFault backendlessFault) {
                                }
                            });
                        }else {
                            findViewById(R.id.CoverImage).setVisibility(View.GONE);
                            findViewById(R.id.WelcomeBG).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        findViewById(R.id.CoverImage).setVisibility(View.GONE);
                        findViewById(R.id.WelcomeBG).setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Your login session has expired!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.run();
    }

    public void Login(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View popup = inflater.inflate(R.layout.login, null);
        popup.findViewById(R.id.loginName).setVisibility(View.GONE);
        builder.setView(popup)
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final ProgressDialog wait = new ProgressDialog(Welcome.this);
                        wait.setMessage("Loading...");
                        wait.show();
                        final String email = ((TextView)popup.findViewById(R.id.loginEmail)).getText().toString();
                        final String password = ((TextView)popup.findViewById(R.id.loginPassword)).getText().toString();
                        if(email == null || password == null || email.equals("") || password.equals("")) {
                            new AlertDialog.Builder(Welcome.this).setTitle("Error").setMessage("Please enter your email and password!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                        }else {
                            final boolean stayLogged = ((CheckBox) popup.findViewById(R.id.stayLogged)).isChecked();
                            new Thread() {
                                @Override
                                public void run() {
                                    Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
                                        @Override
                                        public void handleResponse(BackendlessUser backendlessUser) {
                                            Data.user = backendlessUser;
                                            Proceed();
                                            wait.dismiss();
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault backendlessFault) {
                                            wait.dismiss();
                                            String errorCode = backendlessFault.getCode();
                                            String message;
                                            final boolean reset;
                                            if (errorCode.equals("3003")) {
                                                message = "Invalid login or password!";
                                                reset = false;
                                            } else if (errorCode.equals("3006")) {
                                                message = "Please enter your email and password!";
                                                reset = false;
                                            } else if (errorCode.equals("3036")) {
                                                message = "Too many failed attempts, account locked!";
                                                reset = true;
                                            } else {
                                                message = "Error code" + errorCode + ", please contact z.aoran@gmail.com";
                                                reset = false;
                                            }
                                            final AlertDialog.Builder secondAlert = new AlertDialog.Builder(Welcome.this).setTitle(message)
                                                    .setPositiveButton(reset ? "Retrieve Your Password" : "OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            if (reset) {
                                                                new AlertDialog.Builder(Welcome.this).setTitle("Check your email for your password.").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        Backendless.UserService.restorePassword(email);
                                                                        dialogInterface.dismiss();
                                                                    }
                                                                }).show();
                                                                dialogInterface.dismiss();
                                                            } else {
                                                                dialogInterface.dismiss();
                                                            }
                                                        }
                                                    });
                                            if (reset) {
                                                secondAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                });
                                            }
                                            secondAlert.show();
                                        }
                                    }, stayLogged);
                                }
                            }.run();

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
                        final String email = ((TextView)popup.findViewById(R.id.loginEmail)).getText().toString();
                        String password = ((TextView)popup.findViewById(R.id.loginPassword)).getText().toString();
                        String name = ((TextView)popup.findViewById(R.id.loginName)).getText().toString();
                        if(email == null || password == null || email.equals("") || password.equals("") || name.equals("")) {
                            new AlertDialog.Builder(Welcome.this).setTitle("Try Again").setMessage("Please fill all the text boxes!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                            wait.dismiss();
                        }else {
                            final BackendlessUser user = new BackendlessUser();
                            user.setEmail(email);
                            user.setPassword(password);
                            user.setProperty("name", name);
                            user.setProperty("portrait", "");
                            new Thread() {
                                @Override
                                public void run(){
                                    Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                                        @Override
                                        public void handleResponse(BackendlessUser backendlessUser) {
                                            wait.dismiss();
                                            Data.user = backendlessUser;
                                            new AlertDialog.Builder(Welcome.this).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).setTitle("Your account has been created!").setMessage("Please check your email to activate your account").show();
                                            //Go to next activity
                                            Proceed();
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault backendlessFault) {
                                            wait.dismiss();
                                            String errorCode = backendlessFault.getCode();
                                            String message;
                                            if (errorCode.equals("3011")) {
                                                message = "Please enter a password.";
                                            } else if (errorCode.equals("3013")) {
                                                message = "Please enter your email.";
                                            } else if (errorCode.equals("3033")) {
                                                message = "This email address is already taken.";
                                            } else if (errorCode.equals("3040")) {
                                                message = "Please enter a valid email address.";
                                            } else {
                                                message = "Error code" + errorCode + ", please contact z.aoran@gmail.com";
                                            }
                                            new AlertDialog.Builder(Welcome.this).setTitle("Error").setMessage(message).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            }).show();
                                        }
                                    });
                                }
                            }.run();

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
