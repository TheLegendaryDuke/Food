package itsjustaaron.food;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Aaron-Home on 2017-02-21.
 */

public class MyEditText extends EditText {
    public MyEditText(Context context, AttributeSet aa) {
        super(context, aa);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            this.clearFocus();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
