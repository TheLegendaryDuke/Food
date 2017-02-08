package itsjustaaron.food;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

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
        ImageView image = (ImageView) findViewById(R.id.newOfferFoodImage);
        image.setImageBitmap(BitmapFactory.decodeFile(getFilesDir() + "/foods/" + food.image));
        ((TextView)findViewById(R.id.newofferFoodName)).setText(food.name);
        ((TextView)findViewById(R.id.newofferFoodDesc)).setText(food.description);
        ((TextView)findViewById(R.id.newofferFoodTags)).setText(food.tags);
    }
}
