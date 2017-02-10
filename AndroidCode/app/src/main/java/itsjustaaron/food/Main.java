package itsjustaaron.food;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


//TODO: side nav actions, guest restriction

//not so urgent TODO: caching Data

//future add-ons: in-app communication, in-app payment

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static ProgressDialog progressDialog;
    protected PagerAdapter adapter;

    public boolean checkUser(final Context context) {
        if (Data.user == null) {
            new AlertDialog.Builder(context).setTitle("Hello Guest").setMessage("Please log in!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent goBack = new Intent(context, Welcome.class);
                    context.startActivity(goBack);
                    finish();
                }
            }).show();
            return false;
        }
        return true;
    }

    //helpers to implement wait
    public static void showWait() {
        progressDialog.show();
    }

    public static void hideWait() {
        progressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.cravings = new ArrayList<>();
        Data.foods = new ArrayList<Food>();
        Data.fileDir = getFilesDir().toString();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");


        TimeZone tz = TimeZone.getDefault();
        Data.standardDateFormat.setTimeZone(tz);

        Data.tags = new ArrayList<String>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                try {
                    //download all the available food tags
                    List<Map> result = Backendless.Persistence.of("tags").find().getCurrentPage();
                    for (int i = 0; i < result.size(); i++) {
                        Data.tags.add(result.get(i).get("tag").toString());
                    }
                } catch (BackendlessException e) {
                    Log.d("backendless", e.toString());
                }
                return null;
            }
        }.execute(new Void[]{});

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (Data.user == null) {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.DrawerName)).setText("Guest");
        } else {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.DrawerName)).setText((String) Data.user.getProperty("name"));
            if (Data.user.getProperty("portrait") != "") {
                //download the user portrait if there is one
                final File portrait = new File(Data.fileDir + "/" + Data.user.getProperty("portrait").toString());
                if (portrait.exists()) {
                    ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.userPortrait)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
                } else {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public Void doInBackground(Void... voids) {
                            String path = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files/users/" + Data.user.getEmail() + "/" + Data.user.getProperty("portrait");
                            try {
                                URL url = new URL(path);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setDoInput(true);
                                conn.connect();
                                InputStream is = conn.getInputStream();
                                Bitmap bm = BitmapFactory.decodeStream(is);
                                FileOutputStream fos = new FileOutputStream(portrait);
                                ByteArrayOutputStream outstream = new ByteArrayOutputStream();
                                bm.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                                byte[] byteArray = outstream.toByteArray();
                                fos.write(byteArray);
                                fos.close();
                            } catch (Exception e) {
                                Log.d("downloadPortrait", e.toString());
                            }
                            return null;
                        }

                        @Override
                        public void onPostExecute(Void v) {
                            ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.userPortrait)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
                        }
                    }.execute(new Void[]{});
                }
            }
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //provide title for tabs
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        Main.this.getSupportActionBar().setTitle("What others are craving");
                        return;
                    case 1:
                        Data.offerFragment.start();
                        Main.this.getSupportActionBar().setTitle("What's available");
                        return;
                    default:
                        return;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();


    }

    public void ProfileSetup(View view) {
        if(checkUser(this)) {
            Intent intent = new Intent(Main.this, ProfileSetup.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Action bar actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean onCraving = getSupportActionBar().getTitle() == "What others are craving";
        switch (id) {
            case R.id.addNew:
                if(checkUser(this)) {
                    Intent next = new Intent(this, NewFood.class);
                    next.putExtra("onCraving", onCraving);
                    if(onCraving) {
                        startActivity(next);
                        Data.cravingFragment.refresh(null);
                    }else {
                        startActivityForResult(next, 0);
                    }
                }
                    break;
            case R.id.search:
                Data.onCraving = onCraving;
                onSearchRequested();
                break;
            case R.id.menu_refresh:
                if (onCraving) {
                    Data.cravingFragment.refresh(null);
                } else {
                    Data.offerFragment.refresh(null);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0) {
            if(resultCode == RESULT_OK) {
                String id = data.getStringExtra("id");
                Intent intent = new Intent(this, NewOffer.class);
                intent.putExtra("food", id);
                startActivity(intent);
            }
        }
    }

    //Side drawer actions
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        if (Data.user == null) {
            new AlertDialog.Builder(this).setTitle("Hello Guest").setMessage("Please log in!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent goBack = new Intent(Main.this, Welcome.class);
                    startActivity(goBack);
                    finish();
                }
            }).show();
        }else {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            switch (id) {
                case R.id.logoff:
                    showWait();
                    Backendless.UserService.logout(new AsyncCallback<Void>() {
                        @Override
                        public void handleResponse(Void aVoid) {
                            Data.user = null;
                            Intent intent = new Intent(Main.this, Welcome.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {

                        }
                    });
                    break;
                case R.id.contactDev:
                    Intent email = new Intent(Intent.ACTION_SENDTO);
                    email.setData(Uri.parse("mailto:"));
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"z.aoran@gmail.com"});
                    email.putExtra(Intent.EXTRA_SUBJECT, "To the developer of \"Food\"");
                    email.putExtra(Intent.EXTRA_TEXT, "Please enter the message you want to send to me, any feedback is welcomed and appreciated :)");
                    if (email.resolveActivity(getPackageManager()) != null) {
                        startActivity(email);
                    }
                    break;

                case R.id.drawerFavorites:
                    Intent myCravings = new Intent(this, MyCravings.class);
                    startActivity(myCravings);
                    break;

                case R.id.drawerOffer:
                    Intent myOffers = new Intent(this, MyOffers.class);
                    startActivity(myOffers);
                    break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
