package itsjustaaron.food;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Back.MyHandler;

public class Options extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Data.handler = new MyHandler(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
    }
}
