package itsjustaaron.food.Utilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by aozhang on 3/3/2017.
 */

public class DefaultTextView extends TextView {

    public DefaultTextView(Context context) {
        super(context);
    }

    public DefaultTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont();
    }

    public DefaultTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont();
    }

    private void setCustomFont() {
        Typeface face=Typeface.createFromAsset(getContext().getAssets(), "STXINWEI.TTF");
        this.setTypeface(face);
    }
}