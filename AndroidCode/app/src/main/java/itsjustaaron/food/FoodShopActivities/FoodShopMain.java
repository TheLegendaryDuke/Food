package itsjustaaron.food.FoodShopActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.FoodActivities.Main;
import itsjustaaron.food.FoodActivities.MainPagerAdapter;
import itsjustaaron.food.FoodActivities.MyCravings;
import itsjustaaron.food.FoodActivities.Options;
import itsjustaaron.food.FoodActivities.Welcome;
import itsjustaaron.food.R;

public class FoodShopMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public MenuFragment menuFragment;
    public DemandFragment demandFragment;
    private Toast backPressed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_shop_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //provide title for tabs
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        findViewById(R.id.cSearchCriterias).setVisibility(View.VISIBLE);
                        findViewById(R.id.sort).setVisibility(View.VISIBLE);
                        findViewById(R.id.search).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.actionBarTitle)).setText("");
                        return;
                    case 1:
                        findViewById(R.id.cSearchCriterias).setVisibility(View.GONE);
                        findViewById(R.id.sort).setVisibility(View.GONE);
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
        }else {
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

    //TODO: implement the logic for search and sort
    public void barOnClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.search:
                break;
            case R.id.sort:
                break;
        }
    }
}
