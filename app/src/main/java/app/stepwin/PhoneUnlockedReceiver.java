package app.stepwin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class PhoneUnlockedReceiver extends BroadcastReceiver {
    public PhoneUnlockedReceiver() {
    }

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean unlocked  = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        sharedPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            int number = sharedPreferences.getInt("unlocks", 0) + 1;
            Log.i("unlockeddevice", "unlocked  " + number);
            editor.putInt("unlocks", number);
            editor.apply();
        }
        /*Device is shutting down. This is broadcast when the device
         * is being shut down (completely turned off, not sleeping)
         * */
        else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {

        }
    }
}
