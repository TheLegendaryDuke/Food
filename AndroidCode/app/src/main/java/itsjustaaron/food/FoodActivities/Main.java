package itsjustaaron.food.FoodActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.FoodOffer;
import itsjustaaron.food.R;


//TODO: side nav actions, guest restriction

//not so urgent TODO: caching Data

//future add-ons: in-app communication, in-app payment

public class Main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Toast backPressed = null;

    public CravingFragment cravingFragment;
    public OfferFragment offerFragment;

    private static ProgressDialog progressDialog;
    protected PagerAdapter adapter;

    public void doMySearch(String query) {
        progressDialog.show();
        ArrayList searchCriteria;
        if(Data.onCraving) {
            searchCriteria = Data.cSearchCriteria;
        }else {
            searchCriteria = Data.oSearchCriteria;
        }
        searchCriteria.clear();
        List<String> tagResult = Food.csvToList(query.toUpperCase());
        boolean tagCheck = true;
        for (int i = 0; i < tagResult.size(); i++) {
            if (!Data.tags.contains(tagResult.get(i))) {
                tagCheck = false;
                break;
            }
        }
        String whereClause = "";
        if (tagCheck) {
            searchCriteria.addAll(tagResult);
            for (int i = 0; i < tagResult.size(); i++) {
                whereClause = whereClause + "tags LIKE '%" + tagResult.get(i) + "%'";
                if (i != tagResult.size() - 1) {
                    whereClause = whereClause + " and ";
                }
            }
        } else {
            searchCriteria.add(query);
            whereClause = "name LIKE '%" + query + "%'";
        }
        final String where = whereClause;
        new AsyncTask<Void, Void, Integer>() {
            @Override
            public Integer doInBackground(Void... voids) {
                List<Map> maps = Back.findObjectByWhere(where, Back.object.food).getCurPage();
                List<String> foodIDs = new ArrayList<String>();
                if (maps.size() != 0) {
                    for (int i = 0; i < maps.size(); i++) {
                        foodIDs.add(maps.get(i).get("objectId").toString());
                    }
                    String where = "foodID in (";
                    for (int i = 0; i < foodIDs.size(); i++) {
                        where = where + "'" + foodIDs.get(i) + "'";
                        if (i != foodIDs.size() - 1) {
                            where = where + ", ";
                        }
                    }
                    where = where + ")";
                    if(Data.onCraving) {
                        Data.cravingPaged = Back.findObjectByWhere(where, Back.object.craving);
                        List<Map> mapResult = Data.cravingPaged.getCurPage();
                        if(mapResult.size() == 0) {
                            return 1;
                        }else {
                            Data.cravings.clear();
                            for (int i = 0; i < mapResult.size(); i++) {
                                Map obj = mapResult.get(i);
                                final Craving craving = new Craving(obj);
                                Data.cravings.add(craving);
                            }
                        }
                    }else {
                        Data.offerPaged = Back.findObjectByWhere(where, Back.object.foodoffer);
                        List<Map> mapResult = Data.offerPaged.getCurPage();
                        if(mapResult.size() == 0) {
                            return 1;
                        }else {
                            Data.foodOffers.clear();
                            for (int i = 0; i < mapResult.size(); i++) {
                                Map obj = mapResult.get(i);
                                final FoodOffer foodOffer = new FoodOffer(obj);
                                Data.foodOffers.add(foodOffer);
                            }
                        }
                    }
                }else {
                    return 1;
                }
                return 0;
            }

            @Override
            public void onPostExecute(Integer v) {
                if(Data.onCraving) {
                    if (v == 1) {
                        Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                    }else {
                        searchCallBack();
                    }
                }else {
                    if (v == 1) {
                        Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                    }else {
                        searchCallBack();
                    }
                }
            }
        }.execute(new Void[]{});
    }

    public void Scan(View view) {
        offerFragment.start();
    }

    public void search() {
        MyEditText editText = (MyEditText) findViewById(R.id.menuSearchBar);
        String search = editText.getText().toString();
        if(search.equals("")) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            editText.clearFocus();
            editText.setHint("Search for a Food");
            return;
        }
        doMySearch(search);
    }

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
        Data.handler = new MyHandler(this);
        Data.cravings = new ArrayList<>();
        Data.foods = new ArrayList<Food>();
        Data.fileDir = getFilesDir().toString();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");


        TimeZone tz = TimeZone.getDefault();
        Data.standardDateFormat.setTimeZone(tz);

        Data.tags = new ArrayList<String>();
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... voids) {
                //download all the available food tags
                List<Map> result = Back.getAll(Back.object.tag).getCurPage();
                for (int i = 0; i < result.size(); i++) {
                    Data.tags.add(result.get(i).get("tag").toString());
                }
                return null;
            }
        }.execute(new Void[]{});

        final MyEditText searchBar = (MyEditText) findViewById(R.id.menuSearchBar);
        searchBar.setMain(this);
        searchBar.setCursorVisible(false);
        searchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    searchBar.setCursorVisible(true);
                    searchBar.setHint("Press ENTER to search");
                }else {
                    searchBar.setCursorVisible(false);
                }
            }
        });

        findViewById(R.id.search_bar_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (Data.user == null) {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.DrawerName)).setText("Guest");
        } else {
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.DrawerName)).setText((String) Data.user.getProperty("name"));
            if (!Data.user.getProperty("portrait").equals("")) {
                //download the user portrait if there is one
                final File portrait = new File(Data.fileDir + "/users/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait").toString());
                if (portrait.exists()) {
                    ((ImageView) navigationView.getHeaderView(0).findViewById(R.id.userPortrait)).setImageBitmap(BitmapFactory.decodeFile(portrait.getAbsolutePath()));
                } else {

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public Void doInBackground(Void... voids) {
                            String path = "/users/" + Data.user.getObjectId() + "/" + Data.user.getProperty("portrait");
                            Back.downloadToLocal(path);
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
                        findViewById(R.id.oSearchCriterias).setVisibility(View.GONE);
                        findViewById(R.id.cSearchCriterias).setVisibility(View.VISIBLE);
                        Data.onCraving = true;
                        return;
                    case 1:
                        findViewById(R.id.cSearchCriterias).setVisibility(View.GONE);
                        findViewById(R.id.oSearchCriterias).setVisibility(View.VISIBLE);
                        Data.onCraving = false;
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
        adapter = new MainPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

    }

    public void ProfileSetup(View view) {
        if(checkUser(this)) {
            Intent intent = new Intent(this, ProfileSetup.class);
            Main.this.startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if(backPressed == null) {
            backPressed = Toast.makeText(this, "Press again to exit the application.", Toast.LENGTH_LONG);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(backPressed.getView().getWindowVisibility() == View.VISIBLE) {
                super.onBackPressed();
            }else {
                backPressed.show();
            }
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
        final boolean onCraving = Data.onCraving;
        switch (id) {
            case R.id.addNew:
                if(checkUser(this)) {
                    Intent next = new Intent(this, NewFood.class);
                    next.putExtra("onCraving", onCraving);
                    if(onCraving) {
                        startActivity(next);
                    }else {
                        startActivityForResult(next, 0);
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchCallBack() {
        final LinearLayout criteriaContainer;
        if(Data.onCraving) {
            criteriaContainer = (LinearLayout) findViewById(R.id.cSearchCriterias);
        }else {
            criteriaContainer = (LinearLayout) findViewById(R.id.oSearchCriterias);
        }
        criteriaContainer.removeAllViews();
        final ArrayList<String> searchCriteria;
        if(Data.onCraving) {
            searchCriteria = Data.cSearchCriteria;
            cravingFragment.notifyChanges();
        }else {
            searchCriteria = Data.oSearchCriteria;
            offerFragment.notifyChanges();
        }
        for(final String c : searchCriteria) {
            TextView textView = new TextView(this);
            final LinearLayout smaller = new LinearLayout(this);
            smaller.setOrientation(LinearLayout.HORIZONTAL);
            smaller.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            smaller.setBackground(ContextCompat.getDrawable(this, R.drawable.search_criteria_rectangle));
            smaller.setGravity(Gravity.CENTER);
            textView.setText(c);
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.remove));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(Math.round(textView.getTextSize()) + 20, Math.round(textView.getTextSize())));
            imageView.setPadding(20, 0, 0, 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchCriteria.remove(c);
                    if(searchCriteria.size() == 0) {
                        if(Data.onCraving) {
                            Data.cravings.clear();
                            cravingFragment.refresh(cravingFragment.swipeRefreshLayout);
                        }else {
                            Data.foodOffers.clear();
                            offerFragment.refresh(offerFragment.swipeRefreshLayout);
                        }
                    }else {
                        String query = Food.listToCsv(searchCriteria);
                        doMySearch(query);
                    }
                    criteriaContainer.removeAllViews();
                }
            });
            smaller.addView(textView);
            smaller.addView(imageView);
            criteriaContainer.addView(smaller);
        }
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                    Back.logOff();
                    Intent intent = new Intent(Main.this, Welcome.class);
                    startActivity(intent);
                    hideWait();
                    finish();
                    break;
                case R.id.contactDev:
                    Intent email = new Intent(Intent.ACTION_SENDTO);
                    email.setData(Uri.parse("mailto:"));
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{"contactfoodapp@gmail.com"});
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

                case R.id.drawerOption:
                    Intent options = new Intent(this, Options.class);
                    startActivity(options);
                    break;
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
