package itsjustaaron.food.Utilities;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Aaron-Home on 2017/5/18.
 */

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
public class ViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    public View view;

    public ViewHolder(View v) {
        super(v);
        view = v;
    }
}
