package itsjustaaron.food.Utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import itsjustaaron.food.Back.Data;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.FoodActivities.CravingDetails;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.FoodOffer;
import itsjustaaron.food.FoodActivities.OfferDetails;
import itsjustaaron.food.R;

/**
 * Created by aozhang on 2/10/2017.
 */


public class BasicFoodAdapter extends ArrayAdapter {
    Context context;
    ArrayList<Craving> data;
    ArrayList<Food> foods = new ArrayList<>();

    public BasicFoodAdapter(Context context, ArrayList<Craving> data) {
        super(context, -1, data);
        this.context = context;
        this.data = data;
        for (Craving t : data) {
            foods.add(t.food);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Craving t = data.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.food_list_item, parent, false);
        ((ImageView) rowView.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/foods/" + foods.get(position).image));
        ((TextView) rowView.findViewById(R.id.foodName)).setText(foods.get(position).name);
        ((TextView) rowView.findViewById(R.id.foodDescription)).setText(foods.get(position).description);
        List<String> tags = Food.csvToList(foods.get(position).tags);
        final LinearLayout cont = (LinearLayout) rowView.findViewById(R.id.foodItemTags);
        for(String tag : tags) {
            TextView textView = new TextView(context);
            textView.setText(tag);
            cont.addView(textView);
        }
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go;
                go = new Intent(context, CravingDetails.class);
                go.putExtra("cravingID", ((Craving)t).objectId);
                context.startActivity(go);
            }
        });
        return rowView;
    }
}
