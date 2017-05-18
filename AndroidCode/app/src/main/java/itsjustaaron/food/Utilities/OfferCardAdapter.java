package itsjustaaron.food.Utilities;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import itsjustaaron.food.Back.Data;
import itsjustaaron.food.FoodActivities.CravingDetails;
import itsjustaaron.food.Model.Offer;
import itsjustaaron.food.R;

/**
 * Created by Aaron-Home on 2017/5/18.
 */

public class OfferCardAdapter extends RecyclerView.Adapter<ViewHolder> {
    private CravingDetails cravingDetails;
    private List<Offer> data;

    public OfferCardAdapter(CravingDetails c, List d) {
        cravingDetails = c;
        data = d;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.offer_inside_craving, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final View v = holder.view;
        if(position == 0) {
            v.findViewById(R.id.firstCard).setVisibility(View.VISIBLE);
            v.findViewById(R.id.normalCase).setVisibility(View.GONE);
            v.findViewById(R.id.firstCard).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cravingDetails.proposeNewOffer();
                }
            });
        }else {
            final Offer offer = data.get(position);
            if (offer.offererPortrait != null && !offer.offererPortrait.equals("")) {
                final String path = "/offers/offerers/" + offer.offererPortrait;
                ((ImageView) v.findViewById(R.id.userIcon)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + path));
            }
            ((TextView)v.findViewById(R.id.userName)).setText(offer.offerer);
            ((ImageView) v.findViewById(R.id.foodImage)).setImageBitmap(BitmapFactory.decodeFile(Data.fileDir + "/foods/" + offer.food.image));
            ((TextView)v.findViewById(R.id.price)).setText(String.valueOf(offer.price));
            ((TextView)v.findViewById(R.id.location)).setText(offer.city);
            v.findViewById(R.id.normalCase).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cravingDetails.goToOffer(offer.objectId);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
