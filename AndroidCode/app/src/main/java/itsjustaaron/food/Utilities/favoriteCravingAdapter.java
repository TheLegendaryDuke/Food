package itsjustaaron.food.Utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import itsjustaaron.food.Back.Data;
import itsjustaaron.food.FoodActivities.CravingDetails;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.R;

/**
 * Created by aozhang on 2/10/2017.
 */


public class favoriteCravingAdapter extends ArrayAdapter<Craving> {
    Context context;
    ArrayList<Craving> data;

    public favoriteCravingAdapter(Context context, ArrayList<Craving> data) {
        super(context, -1, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Craving t = data.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.favorite_food_list_item, parent, false);
        ((ImageView) rowView.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/foods/" + t.food.image));
        ((TextView) rowView.findViewById(R.id.foodName)).setText(t.food.name);
        ((TextView) rowView.findViewById(R.id.foodDescription)).setText(t.food.description);
        LinearLayout tags = (LinearLayout) rowView.findViewById(R.id.foodItemTags);
        List<String> tagList = Food.csvToList(t.food.tags);
        rowView.findViewById(R.id.remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favoriteCravingAdapter.this.remove(t);
                favoriteCravingAdapter.this.notifyDataSetChanged();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... voids) {
                        t.followSwitch();
                        return null;
                    }
                    @Override
                    public void onPostExecute(Void v) {
                        Toast.makeText(context, "craving removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                }.execute();
            }
        });
        tags.removeAllViews();
        for(String tag : tagList) {
            int resID = Helpers.getTagDrawable(Data.tagColors.get(tag));
            TextView tagView = new TextView(context);
            tagView.setText(tag);
            tagView.setTextSize(10);
            tagView.setBackgroundResource(resID);
            tagView.setPadding(7,5,7,5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0,0,5,0);
            tagView.setLayoutParams(params);
            tags.addView(tagView);
        }
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go;
                go = new Intent(context, CravingDetails.class);
                go.putExtra("cravingID", t.objectId);
                context.startActivity(go);
            }
        });
        return rowView;
    }
}
