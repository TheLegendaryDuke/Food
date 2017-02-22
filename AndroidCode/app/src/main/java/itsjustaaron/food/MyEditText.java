package itsjustaaron.food;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Aaron-Home on 2017-02-21.
 */

public class MyEditText extends EditText {
    private Main main;

    public MyEditText(Context context, AttributeSet aa) {
        super(context, aa);
    }

    public void setMain(Main main) {
        this.main = main;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            this.clearFocus();
            this.setHint("Search for a Food");
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode==KeyEvent.KEYCODE_ENTER)
        {
            main.search();
        }
        // Handle all other keys in the default way
        return super.onKeyDown(keyCode, event);
    }
}
