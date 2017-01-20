package itsjustaaron.food;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by aozhang on 1/18/2017.
 */

public class MyAdapter<T> extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private String fileDir;
    private Context context;
    private char source;
    private ArrayList<T> mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<T> myDataset, char s, Context context) {
        mDataset = myDataset;
        source = s;
        //TODO: check if fileDir is the dir I want
        this.fileDir = context.getFilesDir().toString();
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v;
        // create a new view
        if (source == 'c') {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.craving_list_item, parent, false);
        } else {
            //TODO: to be filled in
            v = null;
        }
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        View v = holder.view;
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (source == 'c') {
            final ImageView image = (ImageView) v.findViewById(R.id.cravingItemImage);
            TextView description = (TextView) v.findViewById(R.id.cravingItemDescription);
            TextView tags = (TextView) v.findViewById(R.id.cravingItemTags);
            final ImageView likeOrNot = (ImageView) v.findViewById(R.id.cravingFollowingOrNot);
            final TextView count = (TextView) v.findViewById(R.id.cravingFollowerCount);
            final Craving craving = (Craving) mDataset.get(position);
            final String imagePath = fileDir + "/foods/" + craving.food.image;
            final File file = new File(imagePath);
            image.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            description.setText(craving.food.name);
            tags.setText(craving.food.tags);
            if (craving.following) {
                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.favorite, null));
            } else {
                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_favorite_border_black_48dp, null));
            }
            likeOrNot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    craving.followSwitch();
                    if (craving.following) {
                        likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.favorite, null));
                        count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                    } else {
                        likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_favorite_border_black_48dp, null));
                        count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                    }
                }
            });
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent next = new Intent(context, CravingDetails.class);
                    next.putExtra("cravingID", craving.objectId);
                    context.startActivity(next);
                }
            };
            image.setOnClickListener(listener);
            description.setOnClickListener(listener);
            count.setText(String.valueOf(craving.numFollowers));
            image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        } else {

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }
}