package itsjustaaron.food.FoodShopActivities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.Back.PagedList;
import itsjustaaron.food.FoodActivities.Main;
import itsjustaaron.food.FoodActivities.Options;
import itsjustaaron.food.FoodActivities.Welcome;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;

public class FoodShopMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public MenuFragment menuFragment;
    public DemandFragment demandFragment;
    private Toast backPressed = null;
    private PagedList demands;
    private Dialog searchDialog;
    private int screenSizeX;
    private ProgressDialog progressDialog;
    private ArrayList<String> searchCriteria;

    public void doMySearch(String query) {
        progressDialog.show();
        searchCriteria = new ArrayList<>();
        final String where = Back.queryToSearchCriteria(query, searchCriteria);
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
                    demands = Back.findObjectByWhere(where, Back.object.craving);
                    List<Map> mapResult = demands.getCurPage();
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
                    return 1;
                }
                return 0;
            }

            @Override
            public void onPostExecute(Integer v) {
                if (v == 1) {
                    Toast.makeText(getApplicationContext(), "Your search yields no results", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {
                    searchCallBack();
                }
            }
        }.execute();
    }

    public void searchCallBack() {
        final LinearLayout criteriaContainer;
        criteriaContainer = (LinearLayout) findViewById(R.id.cSearchCriterias);
        criteriaContainer.removeAllViews();
        demandFragment.updateData();
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
                        Data.cravings.clear();
                        demandFragment.refresh(demandFragment.swipeRefreshLayout);
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
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_shop_main);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screenSizeX = size.x;
        searchDialog = new Dialog(this, R.style.DialogTheme);
        searchDialog.setContentView(R.layout.search_bar);
        WindowManager.LayoutParams params = searchDialog.getWindow().getAttributes();
        params.horizontalMargin = 0;
        params.verticalMargin = 0;
        params.width = screenSizeX;
        params.gravity = Gravity.LEFT|Gravity.TOP;
        params.x = 0;
        params.y = 0;

        searchDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                revealShow(true);
            }
        });

        searchDialog.findViewById(R.id.searchBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(false);
            }
        });

        searchDialog.findViewById(R.id.searchSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doMySearch(((EditText) searchDialog.findViewById(R.id.searchText)).getText().toString());
                revealShow(false);
            }
        });

        searchDialog.findViewById(R.id.searchClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) searchDialog.findViewById(R.id.searchText)).setText("");
            }
        });

        searchDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    revealShow(false);
                    return true;
                }
                return false;
            }
        });

        ((EditText) searchDialog.findViewById(R.id.searchText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String search = ((EditText) searchDialog.findViewById(R.id.searchText)).getText().toString();
                    if (!search.equals("")) {
                        doMySearch(search);
                        revealShow(false);
                    }else {
                        ((EditText) searchDialog.findViewById(R.id.searchText)).setHint("Enter a keyword or comma separated tags");
                    }
                }
                return false;
            }
        });

        searchDialog.findViewById(R.id.searchBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(false);
            }
        });

        searchDialog.findViewById(R.id.searchSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = ((EditText) searchDialog.findViewById(R.id.searchText)).getText().toString();
                if (!search.equals("")) {
                    doMySearch(search);
                    revealShow(false);
                }else {
                    ((EditText) searchDialog.findViewById(R.id.searchText)).setHint("Enter a keyword or some comma separated tags");
                }
            }
        });

        searchDialog.findViewById(R.id.searchClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((EditText) searchDialog.findViewById(R.id.searchText)).setText("");
            }
        });

        searchDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    revealShow(false);
                    return true;
                }
                return false;
            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //provide title for tabs
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        findViewById(R.id.cSearchCriterias).setVisibility(View.VISIBLE);
                        findViewById(R.id.search).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.actionBarTitle)).setText("Popular Demands");
                        return;
                    case 1:
                        findViewById(R.id.cSearchCriterias).setVisibility(View.GONE);
                        findViewById(R.id.search).setVisibility(View.GONE);
                        ((TextView)findViewById(R.id.actionBarTitle)).setText("Your Menu");
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
        FoodShopPagerAdapter adapter = new FoodShopPagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

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
                }.execute();
            }
        }
    }

    private void revealShow(boolean reveal) {
        final View view = searchDialog.findViewById(R.id.searchBar);
        View searchButton;
        if(Data.onCraving) {
            searchButton = searchDialog.findViewById(R.id.searchSearch);
        }else {
            searchButton = searchDialog.findViewById(R.id.searchClear);
        }
        int cx = searchButton.getLeft() + searchButton.getMeasuredWidth() / 2;
        int cy = searchButton.getTop() + searchButton.getMeasuredHeight() / 2;
        int finalRadius = screenSizeX - searchButton.getMeasuredWidth();

        if(reveal){
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            view.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, finalRadius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    searchDialog.dismiss();
                    view.setVisibility(View.INVISIBLE);

                }
            });
            anim.start();
        }
    }

    //Side drawer actions
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.logoff:
                Back.logOff();
                Intent intent = new Intent(this, Welcome.class);
                startActivity(intent);
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
            case R.id.drawerOption:
                Intent options = new Intent(this, Options.class);
                startActivity(options);
                break;
            case R.id.swap:
                startActivity(new Intent(this, Main.class));
                overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);
                this.finish();
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        if(backPressed == null) {
            backPressed = Toast.makeText(this, "Press again to exit the application.", Toast.LENGTH_LONG);
        }
        if((Boolean) Data.user.getProperty("defaultFood")) {
            startActivity(new Intent(this, Main.class));
            overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);
            this.finish();
        }else {
            if(backPressed.getView().getWindowVisibility() == View.VISIBLE) {
                super.onBackPressed();
            }else {
                backPressed.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            String id = data.getStringExtra("id");
            Intent intent = new Intent(this, NewOffer.class);
            intent.putExtra("food", id);
            startActivity(intent);
        }else if(requestCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Data.menuUpdated) {
            Data.menuUpdated = false;
            menuFragment.update();
        }
    }

    public void barOnClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.search:
                searchDialog.show();
                break;
        }
    }
}
