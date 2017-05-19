package itsjustaaron.food.Utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by aozhang on 3/3/2017.
 */

public class PrettyTextView extends android.support.v7.widget.AppCompatTextView {

    public PrettyTextView(Context context) {
        super(context);
    }

    public PrettyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont();
    }

    public PrettyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont();
    }

    private void setCustomFont() {
        Typeface face=Typeface.createFromAsset(getContext().getAssets(), "STXINWEI.TTF");
        this.setTypeface(face);
    }
}