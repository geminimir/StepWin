package app.stepwin.alarm_services;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.stepwin.DailyFragment;
import app.stepwin.FileUploadService;
import app.stepwin.GiftActivity;
import app.stepwin.MainActivity;
import app.stepwin.R;
import app.stepwin.constants.Constants;
import app.stepwin.database.SqliteDatabase;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.services.CounterService;
import app.stepwin.utils.AppPreference;
import app.stepwin.utils.NetworkHandler;
import okhttp3.ResponseBody;
import retrofit2.Response;


/**
 * Created by natraj on 15/6/17.
 */

public class AlarmService extends Service implements WebResponse {

    private MainActivity mainActivity;
    SharedPreferences sharedPreferences;
    private RetrofitApiClient retrofitApiClient;
    private NetworkHandler networkHandler;
    private SqliteDatabase database;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainActivity = new MainActivity();
        networkHandler = new NetworkHandler(getApplicationContext());
        database = new SqliteDatabase(getApplicationContext());
        retrofitApiClient = RetrofitService.getRetrofit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("AlarmService", "SEND_NOTIFICATION");
        //Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        clearPrefs();
        return START_STICKY;
    }


    private void clearPrefs() {
        if (CounterService.mInstance != null) {
            CounterService.mInstance.saveData();
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(c.getTime());
        String steps = String.valueOf(AppPreference.getIntegerPreference(getApplicationContext(), Constants.STEPS));
        String distance = String.valueOf(AppPreference.getFloatPreference(getApplicationContext(), Constants.DISTANCE));
        String minutes = (AppPreference.getStringPreference(getApplicationContext(), Constants.MINUTS));
        long time_milli = (AppPreference.getLongPreference(getApplicationContext(), Constants.TIMER_TIME));
        String cals = String.valueOf(AppPreference.getIntegerPreference(getApplicationContext(), Constants.CALORIES));
        String points = String.valueOf(AppPreference.getIntegerPreference(getApplicationContext(), Constants.POINTS));
        String unlock_conter = String.valueOf(mainActivity.numberlock);
        String username = AppPreference.getStringPreference(getApplicationContext(), Constants.USER_NAME);
        String email = AppPreference.getStringPreference(getApplicationContext(), Constants.EMAIL);

        AppPreference.setBooleanPreference(getApplicationContext(),Constants.FB_SHARE_DONE,false);

        if (CounterService.mInstance != null) {
            Log.e("CounterService", "!= null");
            CounterService.mInstance.resetValue();
        } else {
            resetPref();
        }

        if (networkHandler.isNetworkAvailable()) {
            RetrofitService.getServerResponseForProgress(null, retrofitApiClient.postUserProgress(steps, distance, cals, points, minutes, time_milli, unlock_conter, date
                    , username, email), this);
        }
    }

    private void resetPref() {
        Log.e("else", "CounterService == null");
        AppPreference.setLongPreference(getApplicationContext(), Constants.TIMER_TIME, 0);
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.STEPS, 0);
        AppPreference.setFloatPreference(getApplicationContext(), Constants.DISTANCE, 0);
        AppPreference.setStringPreference(getApplicationContext(), Constants.MINUTS, "00:00:00");
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.CALORIES, 0);
    }

    public void ExportCSV(Context context, String steps, String distance, String time, String calories, String points, String unlocks) {
        String date, csv = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        date = df.format(c.getTime());
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "." + context.getResources().getString(R.string.app_name));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {

            csv = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/." + context.getResources().getString(R.string.app_name) + File.separator + AppPreference.getStringPreference(getApplicationContext(), Constants.USER_NAME) + ".csv";
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(csv, true));
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{date, steps, distance, time, calories, points, unlocks});

            if (writer != null) {
                writer.writeAll(data);
            }
            try {
                if (writer != null) {
                    writer.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent i = new Intent(context, FileUploadService.class);
        i.putExtra("path", csv);
        i.putExtra("email", AppPreference.getStringPreference(getApplicationContext(), Constants.EMAIL));
        context.startService(i);

    }

    @Override
    public void onResponseSuccess(Response<?> result) {
        Response<ResponseBody> response = (Response<ResponseBody>) result;
        try {
            JSONObject jsonObject = new JSONObject(response.body().string());
            String msg = String.valueOf(jsonObject.get("msg"));
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

            //stopSelf();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponseFailed(String error) {

    }
}
