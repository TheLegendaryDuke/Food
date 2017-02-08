package itsjustaaron.food;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class NewOffer extends AppCompatActivity {
    private Food food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_offer);
        String foodId = getIntent().getStringExtra("id");
        for(int i = 0; i < Data.foods.size(); i++) {
            if(Data.foods.get(i).objectId == foodId) {
                food = Data.foods.get(i);
                break;
            }
        }

    }
}
