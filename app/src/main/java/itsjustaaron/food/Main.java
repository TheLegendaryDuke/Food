package itsjustaaron.food;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private int tabSelected = 0;

    private static ProgressDialog progressDialog;

    public static void showWait() {
        progressDialog.show();
    }

    public static void hideWait() {
        progressDialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        Data.tags = new ArrayList<String>();
        new Thread() {
            @Override
            public void run() {
                Backendless.Persistence.of("tags").find(new AsyncCallback<BackendlessCollection<Map>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<Map> mapBackendlessCollection) {
                        List<Map> result = mapBackendlessCollection.getCurrentPage();
                        for(int i = 0; i < result.size(); i++) {
                            Data.tags.add(result.get(i).get("tag").toString());
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.d(backendlessFault.getMessage(), "handleFault: ");
                    }
                });
            }
        }.start();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        tabSelected = 0;
                        Main.this.getSupportActionBar().setTitle("What others are craving");
                        return;
                    case 1:
                        tabSelected = 1;
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
        final PagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Data.user == null) {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.DrawerName)).setText("Guest");
        } else {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.DrawerName)).setText((String) Data.user.getProperty("name"));
            if (Data.user.getProperty("portrait") != "") {
                final File portrait = new File(getFilesDir() + "/" + Data.user.getProperty("portrait").toString());
                if (portrait.exists()) {
                    ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.userPortrait)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
                } else {
                    new Thread() {
                        public void run() {
                            if (Data.user.getProperty("portrait") != "") {
                                String path  = "https://api.backendless.com/0020F1DC-E584-AD36-FF74-6D3E9E917400/v1/files/users/" + Data.user.getEmail() + "/" + Data.user.getProperty("portrait");
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
                                } catch(Exception e) {
                                    new AlertDialog.Builder(Main.this).setMessage(e.getMessage()).show();
                                }
                            }
                        }
                    }.start();
                ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.userPortrait)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
                }
            }
        }
    }

    public void ProfileSetup(View view) {
        if (Data.user == null) {
            new AlertDialog.Builder(this).setTitle("Hello Guest").setMessage("Please log in!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent goBack = new Intent(Main.this, Welcome.class);
                    startActivity(goBack);
                    finish();
                }
            }).show();
        } else {
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

        if(tabSelected == 0) {
            switch (id) {
                case R.id.addNew:
                    Intent next = new Intent(this, NewCraving.class);
                    startActivity(next);
                    break;
                case R.id.search:
                    onSearchRequested();
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
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
        }

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
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
