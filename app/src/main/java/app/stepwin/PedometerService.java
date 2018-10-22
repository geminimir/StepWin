package app.stepwin;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import app.stepwin.pedometer.SensorFilter;


public class PedometerService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isSensorPresent = false;
    float steps = 0, totalsteps = 0, saved;
    private IBinder mBinder = new MyBinder();

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor ;
    String pref = "MyPref";
    public class MyBinder extends Binder {
        PedometerService getService() {
            return PedometerService.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        startTime = SystemClock.uptimeMillis();
        sharedPreferences = getSharedPreferences(pref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v("", "in onBind");
        return mBinder;

    }

    DatabaseHandler dbHandler = new DatabaseHandler(this);
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //startTime = SystemClock.uptimeMillis();


        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Toast.makeText(getApplicationContext(), ""+ saved, Toast.LENGTH_SHORT).show();
                startTime = SystemClock.uptimeMillis();
            }
            mSensorManager = (SensorManager)
                    this.getSystemService(Context.SENSOR_SERVICE);
           /* if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                    != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                } else {
                    previousY = 0;
                    currentY = 0;
                    totalsteps = 0;
                    threshold = 10;
                    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                }
                isSensorPresent = true;
            }*/
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    != null) {
                previousY = 0;
                currentY = 0;
                totalsteps = 0;
                threshold = 10;

                isSensorPresent = true;
            }else {
                isSensorPresent = false;
            }
            if (isSensorPresent) {
                mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        return START_STICKY;
    }

    GpsService gps;
    double longitude, latitude;
    private float previousY, currentY;
    private int numbersteps, threshold;
    //=========================================>
    private static final int STEP_DELAY_NS = 250000000;
    private static final int ACCEL_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;
    private static final float STEP_THRESHOLD = 20;
    private int accelRingCounter = 0;
    private float[] accelRingX = new float[ACCEL_RING_SIZE];
    private float[] accelRingY = new float[ACCEL_RING_SIZE];
    private float[] accelRingZ = new float[ACCEL_RING_SIZE];
    private int velRingCounter = 0;
    private float[] velRing = new float[VEL_RING_SIZE];
    private long lastStepTimeNs = 0;
    private float oldVelocityEstimate = 0;

   // private StepListener listener;

    //public void registerListener(StepListener listener) {
    //    this.listener = listener;
   // }
    //=========================================>
    @Override
    public void onSensorChanged(SensorEvent event) {
       /* *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            steps = event.values[0];

            totalsteps += steps;
        }
        else {*//*
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            currentY = y;

            if(Math.abs(currentY - previousY) > threshold) {
                steps++;
            }
            previousY = y;
            totalsteps += steps;
       // }*/
//================================================================>
       // public void updateAccel(long timeNs, float x, float y, float z) {
            float[] currentAccel = new float[3];
        long timeNs=0;
        float x=0;
        float y=0;
        float z=0;
            currentAccel[0] = x;
            currentAccel[1] = y;
            currentAccel[2] = z;

            // First step is to update our guess of where the global z vector is.
            accelRingCounter++;
            accelRingX[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
            accelRingY[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
            accelRingZ[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

            float[] worldZ = new float[3];
            worldZ[0] = SensorFilter.sum(accelRingX) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
            worldZ[1] = SensorFilter.sum(accelRingY) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
            worldZ[2] = SensorFilter.sum(accelRingZ) / Math.min(accelRingCounter, ACCEL_RING_SIZE);

            float normalization_factor = SensorFilter.norm(worldZ);

            worldZ[0] = worldZ[0] / normalization_factor;
            worldZ[1] = worldZ[1] / normalization_factor;
            worldZ[2] = worldZ[2] / normalization_factor;

            float currentZ = SensorFilter.dot(worldZ, currentAccel) - normalization_factor;
            velRingCounter++;
            velRing[velRingCounter % VEL_RING_SIZE] = currentZ;

            float velocityEstimate = SensorFilter.sum(velRing);

            if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                    && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {
              //  listener.step(timeNs);
                lastStepTimeNs = timeNs;
               /* steps=timeNs;
                steps++;
                Log.e("Step Counter Start",""+steps);*/
            }
            oldVelocityEstimate = velocityEstimate;
        //}
//================================================================>

        Calendar updateTime = Calendar.getInstance();
        updateTime.set(Calendar.HOUR_OF_DAY, 23);
        updateTime.set(Calendar.MINUTE, 59);
        updateTime.set(Calendar.SECOND, 40);

        Intent downloader = new Intent(getApplicationContext(), BootBroadcast.class);
        downloader.putExtra("savedsteps", steps);
        downloader.putExtra("secs", secs);
        PendingIntent recurringDownload = PendingIntent.getBroadcast(getApplicationContext(),
                0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) getSystemService(
                Context.ALARM_SERVICE);
        alarms.setRepeating(AlarmManager.RTC_WAKEUP,
                updateTime.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, recurringDownload);

        customHandler.postDelayed(updateTimerThread, 0);
        customHandler.removeCallbacks(null);
        timeSwapBuff = 0L;
        //binding with activity.
        Intent intent = new Intent();
        intent.putExtra("steps", steps);
        intent.putExtra("secs", secs);
        intent.setAction("com.app.pedometer");
        sendBroadcast(intent);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gps = new GpsService(getApplicationContext());
                if (gps.canGetLocation) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    Log.i("LocationServices", "Latitude " + latitude);
                    Log.i("LocationServices", "Longitude " + longitude);
                    dbHandler.AddLocation(latitude, longitude);
                }
            }
        }, 10000);

        //Longitude | Latitude
        editor.putFloat("steps", steps);
        editor.putString("latitude", String.valueOf(latitude));
        editor.putString("longitude", String.valueOf(longitude));
        editor.putInt("secs", secs);
        editor.commit();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float getSteps() {
        return steps;
    }
    //public Location getLocation(){return loc;};
    public int getSeconds() {return secs;}

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private long startTime = 0L;
    int secs, mins, hours;
    private Handler customHandler = new Handler();

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
             secs = (int) (updatedTime / 1000);
            customHandler.postDelayed(this, 0);
        }

    };
}
