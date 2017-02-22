package itsjustaaron.food.Back;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import itsjustaaron.food.R;

/**
 * Created by aozhang on 2/22/2017.
 */

public class MyHandler implements Thread.UncaughtExceptionHandler {
    private Activity activity;

    public MyHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, R.string.error, Toast.LENGTH_LONG).show();
            }
        });
        Log.e("Backend", "An error has happened", throwable);
    }
}
