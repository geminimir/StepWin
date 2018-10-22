package app.stepwin;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.stepwin.constants.Constants;
import app.stepwin.pedometer.StepDetector;
import app.stepwin.pedometer.StepListener;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.services.CounterService;
import app.stepwin.services.StepViewUpdateListner;
import app.stepwin.utils.AppPreference;
import app.stepwin.utils.NetworkHandler;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static android.content.Context.SENSOR_SERVICE;

public class DailyFragment extends Fragment implements StepViewUpdateListner {

    private ProgressBar progressBar;
    private TextView steps, km, minuts, cals, points;
    private boolean isBound = false;
    public static DailyFragment dailyFragment;
    public static final String UPDATE_TIMER = "update_time";
    public static final String SERVICE_CREATED = "service_created";
    private CounterService counterService;
    View rootview;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UPDATE_TIMER)) {
                String progress = intent.getStringExtra("TIME");
                minuts.setText(progress);
            } else if (intent.getAction().equals(SERVICE_CREATED)) {
                isBound = true;
                counterService = CounterService.mInstance;
                counterService.setViewUpdateListner(DailyFragment.this);
                resetValue();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_daily, container, false);
        dailyFragment = this;
        progressBar = (ProgressBar) rootview.findViewById(R.id.progress);
        steps = (TextView) rootview.findViewById(R.id.steps);
        km = (TextView) rootview.findViewById(R.id.km);
        minuts = (TextView) rootview.findViewById(R.id.mins);
        cals = (TextView) rootview.findViewById(R.id.cals);
        points = (TextView) rootview.findViewById(R.id.points);
        progressBar.setMax(10000);

        return rootview;
    }


    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), CounterService.class);
        getActivity().startService(intent);
        // getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(UPDATE_TIMER));
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(SERVICE_CREATED));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isBound) {
            CounterService.mInstance.setViewUpdateListner(null);
            isBound = false;
        }
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStepUpdate(final int numSteps, final float distance, final int calories, final int point) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                steps.setText(String.valueOf(numSteps));
                km.setText(String.valueOf(distance));
                cals.setText(String.valueOf(calories));
                points.setText(String.valueOf(point));
                progressBar.setProgress(numSteps);
            }
        });
    }

    public void resetValue() {
        if (steps == null) return;

        steps.setText(String.valueOf(counterService.getNumSteps()));
        km.setText(String.valueOf(counterService.getDistance()));
        cals.setText(String.valueOf(counterService.getCalories()));
        minuts.setText(String.valueOf(counterService.getTime()));
        points.setText(String.valueOf(counterService.getsPoints()));
        progressBar.setProgress(counterService.getNumSteps());
    }
}


