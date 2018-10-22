package app.stepwin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.stepwin.constants.Constants;
import app.stepwin.utils.AppPreference;

public class BootBroadcast extends BroadcastReceiver {
    public BootBroadcast() {
    }

    SharedPreferences shared;
    SharedPreferences.Editor editor;
    String pref = "MyPref", username, email;
    String weight;
    int cals, points;
    long minutes;
    float savedsteps;
    double distance;
    boolean reset;


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        shared = context.getSharedPreferences(pref, Context.MODE_PRIVATE);
        //savedsteps = intent.getFloatExtra("savedsteps", 0);// - shared.getFloat("savedsteps", 0);
        savedsteps = AppPreference.getIntegerPreference(context, Constants.STEPS);
        weight = shared.getString("weight", "1");
        username = shared.getString("user", "");
        email = shared.getString("email", "");
        //points = shared.getInt("points", 0);
        points = AppPreference.getIntegerPreference(context, Constants.POINTS);
        minutes = TimeUnit.SECONDS
               .toMinutes(intent.getIntExtra("secs", 0));

        //cals = (int)(getDistanceRun((int)savedsteps) * Integer.parseInt(weight) * 1.036);
        cals = AppPreference.getIntegerPreference(context, Constants.CALORIES);
        //distance = getDistanceRun((int)savedsteps);
        distance = AppPreference.getFloatPreference(context, Constants.DISTANCE);
        Intent i = new Intent(context, PedometerService.class);
        editor = shared.edit();
        String unlocks = String.valueOf(shared.getInt("unlocks", 0));
        ExportCSV(context, String.valueOf(savedsteps), String.valueOf(distance), String.valueOf(minutes), String.valueOf(cals), String.valueOf(points), unlocks);
        editor.putBoolean("reset", true);

        editor.putFloat("savedsteps", savedsteps);
        editor.putInt("points", shared.getInt("points", 0) + (int) distance * 5 );
        editor.apply();
        Log.i("resettest", "reset from bootcast " + shared.getBoolean("reset", false));
        context.startService(i);
        /*Intent a = new Intent(context, CSVPostService.class);
        a.putExtra("steps", savedsteps);
        a.putExtra("distance", distance);
        a.putExtra("minutes", minutes);
        a.putExtra("email", email);
        a.putExtra("username", username);
        a.putExtra("calories", cals);
        context.startService(a);*/

    }

    private void ExportCSV(Context context, String steps, String distance, String time, String calories, String points, String unlocks) {
        String date, csv = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        date = df.format(c.getTime());
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "." + context.getResources().getString(R.string.app_name));
        boolean success = true;
        folder.setReadOnly();

        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {

            csv = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/." + context.getResources().getString(R.string.app_name) + File.separator +username + ".csv";
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(csv, true));
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] {date, steps, distance, time, calories, points, unlocks});

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
        Log.i("autosync", shared.getBoolean("autosync", true) + "");
        if(shared.getBoolean("autosync", true)) {
            Intent i = new Intent(context, FileUploadService.class);
            i.putExtra("path", csv);
            i.putExtra("email", email);
            context.startService(i);
        }
    }
    public double getDistanceRun(int steps){
        return Math.round(((steps*78)/(float)100000) * 100.0)/100.0;
    }
}
//accelerometer algorithm & facebook login & share
