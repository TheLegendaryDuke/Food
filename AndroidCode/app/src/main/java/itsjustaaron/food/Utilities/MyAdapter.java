package itsjustaaron.food.Utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backendless.geo.Units;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Semaphore;

import itsjustaaron.food.Back.Data;
import itsjustaaron.food.FoodActivities.CravingDetails;
import itsjustaaron.food.FoodActivities.OfferDetails;
import itsjustaaron.food.Model.Craving;
import itsjustaaron.food.Model.Food;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;

/**
 * Created by aozhang on 1/18/2017.
 */

public class MyAdapter<T> extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private final Semaphore update = new Semaphore(1, true);
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
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        // create a new view
        switch (source) {
            case 'c':
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.craving_list_item, parent, false);
                break;
            case 'o':
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_list_item, parent, false);
                break;
            case 'd':
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.demand_list_item, parent, false);
                break;
            case 'm':
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_list_item, parent, false);
                break;
            default:
                v = null;
        }
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final View v = holder.view;
        switch (source) {
            case 'c': {
                final ImageView image = (ImageView) v.findViewById(R.id.cravingItemImage);
                TextView description = (TextView) v.findViewById(R.id.cravingItemDescription);
                LinearLayout tags = (LinearLayout) v.findViewById(R.id.cravingItemTags);
                final ImageView likeOrNot = (ImageView) v.findViewById(R.id.cravingFollowingOrNot);
                final TextView count = (TextView) v.findViewById(R.id.cravingFollowerCount);
                final Craving craving = (Craving) mDataset.get(position);
                final String imagePath = fileDir + "/foods/" + craving.food.image;
                image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                ((TextView) v.findViewById(R.id.cravingItemName)).setText(craving.food.name);
                description.setText(craving.food.description);
                List<String> tagList = Food.csvToList(craving.food.tags);
                for(String tag : tagList) {
                    int resID = Helpers.getTagDrawable(Data.tagColors.get(tag));
                    TextView tagView = new TextView(context);
                    tagView.setText(tag);
                    tagView.setTextSize(5);
                    tagView.setPadding(0,0,0,0);
                    tagView.setBackgroundResource(resID);
                    tags.addView(tagView);
                }
                if (craving.following) {
                    likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.favorite, null));
                } else {
                    likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.heart_grey, null));
                }
                likeOrNot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Data.user != null) {
                            craving.followSwitch();
                            if (craving.following) {
                                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.favorite, null));
                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) + 1));
                            } else {
                                likeOrNot.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.mipmap.heart_grey, null));
                                count.setText(String.valueOf(Integer.parseInt(count.getText().toString()) - 1));
                            }
                        } else {
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
                break;
            }
            case 'o': {
                ImageView image = (ImageView) v.findViewById(R.id.offerFoodImage);
                final Offer offer = (Offer) mDataset.get(position);
                ((TextView) v.findViewById(R.id.offerFoodOfferer)).setText(offer.offerer);
                ((TextView) v.findViewById(R.id.offerFoodName)).setText(offer.food.name);
                ((TextView) v.findViewById(R.id.offerFoodCity)).setText(offer.city);
                ((TextView) v.findViewById(R.id.offerFoodPrice)).setText("$" + String.valueOf(offer.price));
                if (offer.offererPortrait != null && !offer.offererPortrait.equals("")) {
                    final String path = "/offers/offerers/" + offer.offererPortrait;
                    ((ImageView) v.findViewById(R.id.offerOffererImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + path));
                }
                Date exp = offer.expire;
                ((TextView) v.findViewById(R.id.offerFoodExpire)).setText("Expiring: " + Data.standardDateFormat.format(exp));
                image.setImageBitmap(BitmapFactory.decodeFile(fileDir + "/foods/" + offer.food.image));
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detail = new Intent(context, OfferDetails.class);
                        detail.putExtra("offerID", offer.objectId);
                        context.startActivity(detail);
                    }
                });
                break;
            }
            case 'm': {
                ImageView imageView = (ImageView) v.findViewById(R.id.menuItemImage);
                Offer menuItem = (Offer) mDataset.get(position);
                ((TextView)v.findViewById(R.id.menuItemName)).setText(menuItem.food.name);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
                sdf.setTimeZone(TimeZone.getDefault());
                ((TextView)v.findViewById(R.id.menuItemDescription)).setText("Expiring on: " + sdf.format(menuItem.expire));
                imageView.setImageBitmap(BitmapFactory.decodeFile(fileDir + "/foods/" + menuItem.food.image));
                break;
            }
            case 'd': {
                final ImageView image = (ImageView) v.findViewById(R.id.cravingItemImage);
                TextView description = (TextView) v.findViewById(R.id.cravingItemDescription);
                TextView tags = (TextView) v.findViewById(R.id.cravingItemTags);
                final TextView count = (TextView) v.findViewById(R.id.cravingFollowerCount);
                final Craving craving = (Craving) mDataset.get(position);
                final String imagePath = fileDir + "/foods/" + craving.food.image;
                image.setImageBitmap(BitmapFactory.decodeFile(imagePath));
                ((TextView) v.findViewById(R.id.cravingItemName)).setText(craving.food.name);
                description.setText(craving.food.description);
                tags.setText(craving.food.tags);
                count.setText(String.valueOf(craving.numFollowers));
                break;
            }
            //TODO: add a new design for demand
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
