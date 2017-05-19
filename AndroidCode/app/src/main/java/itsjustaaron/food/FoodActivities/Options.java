package itsjustaaron.food.FoodActivities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;
import itsjustaaron.food.R;

public class Options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        ((Switch)findViewById(R.id.switchDefault)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    Data.user.setProperty("defaultFood", false);
                }else {
                    Data.user.setProperty("defaultFood", true);
                }
                new AsyncTask<Void, Void, Integer>() {
                    @Override
                    public Integer doInBackground(Void... voids) {
                        Back.updateUserData();
                        return 0;
                    }
                }.execute();
            }
        });
        ((Switch)findViewById(R.id.switchDefault)).setChecked(!(Boolean) Data.user.getProperty("defaultFood"));
    }
}
