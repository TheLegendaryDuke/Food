package itsjustaaron.food.FoodShopActivities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import itsjustaaron.food.Back.Data;
import itsjustaaron.food.R;

public class FoodShopMain extends AppCompatActivity {
    private Toast backPressed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_shop_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    @Override
    public void onBackPressed()
    {
        if(backPressed == null) {
            backPressed = Toast.makeText(this, "Press again to exit the application.", Toast.LENGTH_LONG);
        }
        if((Boolean) Data.user.getProperty("defaultFood")) {
            this.finish();
            overridePendingTransition(R.anim.left_slide_in, R.anim.left_slide_out);
        }else {
            if(backPressed.getView().getWindowVisibility() == View.VISIBLE) {
                super.onBackPressed();
            }else {
                backPressed.show();
            }
        }
    }
}
