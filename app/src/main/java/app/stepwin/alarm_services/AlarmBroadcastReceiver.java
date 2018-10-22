package app.stepwin.alarm_services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by natraj on 15/6/17.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "AlarmBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "BroadcastReceiver::OnReceive() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        Intent intent1 = new Intent(context, AlarmService.class);
       // intent1.putExtra("SEND_NOTIFICATION", true);
        context.startService(intent1);
    }

}
