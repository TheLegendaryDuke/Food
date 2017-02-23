package itsjustaaron.food;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import itsjustaaron.food.Back.Back;
import itsjustaaron.food.Back.Data;

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
        this.fileDir = Data.fileDir.toString();
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
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_list_item, parent, false);
        }
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final View v = holder.view;
        if (source == 'c') {
            final ImageView image = (ImageView) v.findViewById(R.id.cravingItemImage);
            TextView description = (TextView) v.findViewById(R.id.cravingItemDescription);
            TextView tags = (TextView) v.findViewById(R.id.cravingItemTags);
            final ImageView likeOrNot = (ImageView) v.findViewById(R.id.cravingFollowingOrNot);
            final TextView count = (TextView) v.findViewById(R.id.cravingFollowerCount);
            final Craving craving = (Craving) mDataset.get(position);
            final String imagePath = fileDir + "/foods/" + craving.food.image;
            image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            ((TextView)v.findViewById(R.id.cravingItemName)).setText(craving.food.name);
            description.setText(craving.food.description);
            tags.setText(craving.food.tags);
            if (craving.following) {
                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.favorite, null));
            } else {
                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_favorite_border_black_48dp, null));
            }
            likeOrNot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(Data.user != null) {
                        craving.followSwitch();
                        if (craving.following) {
                            likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.favorite, null));
                            count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                        } else {
                            likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_favorite_border_black_48dp, null));
                            count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                        }
                    }else {
                        new AlertDialog.Builder(context).setMessage("Please login first!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
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
        } else {
            ImageView image = (ImageView) v.findViewById(R.id.offerFoodImage);
            final FoodOffer foodOffer = (FoodOffer) mDataset.get(position);
            ((TextView)v.findViewById(R.id.offerFoodOfferer)).setText(foodOffer.offerer);
            ((TextView)v.findViewById(R.id.offerFoodName)).setText(foodOffer.food.name);
            ((TextView)v.findViewById(R.id.offerFoodCity)).setText(foodOffer.city);
            ((TextView)v.findViewById(R.id.offerFoodPrice)).setText("$" + String.valueOf(foodOffer.price));
            if(foodOffer.offererPortrait != null && !foodOffer.offererPortrait.equals("")) {
                final String path = "/offers/offerers/" + foodOffer.offererPortrait;
                File file = new File(Data.fileDir + path);
                if(!file.exists()) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        public Void doInBackground(Void... voids) {
                            Back.downloadToLocal(path);
                            return null;
                        }
                        public void onPostExecute(Void voi) {
                            ((ImageView)v.findViewById(R.id.offerOffererImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + path));
                        }
                    }.execute(new Void[]{});
                }else {
                    ((ImageView) v.findViewById(R.id.offerOffererImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + path));
                }
            }
            Date exp = foodOffer.expire;
            ((TextView)v.findViewById(R.id.offerFoodExpire)).setText("Expiring: " + Data.standardDateFormat.format(exp));
            image.setImageBitmap(BitmapFactory.decodeFile(fileDir + "/foods/" + foodOffer.food.image));
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detail = new Intent(context, OfferDetails.class);
                    detail.putExtra("offerID", foodOffer.offerID);
                    context.startActivity(detail);
                }
            });
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
