package app.stepwin.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.stepwin.DailyFragment;
import app.stepwin.MainActivity;
import app.stepwin.R;
import app.stepwin.constants.Constants;
import app.stepwin.pedometer.StepDetector;
import app.stepwin.pedometer.StepListener;
import app.stepwin.utils.AppPreference;

/**
 * Created by natraj on 12/10/17.
 */

public class CounterService extends Service implements SensorEventListener, StepListener {
    public static final String ACTION_STOP = "com.stepwin.ACTION_STOP";

    public static CounterService mInstance;
    public IBinder binder = new MyBinder();
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String IS_TIMER_ACTIVE = "TIMER_ACTIVE";
    private long timeNs = 0L;
    private float oldKmValue = -1;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    private StepViewUpdateListner viewUpdateListner;

    private String time = "00:00:00";
    private int calories = 0;
    private float distance = 0f;
    private int numSteps = 0;
    private int sPoints = 0;

    private NotificationManager notificationmanager;
    Notification notification;
    private final int NOTIFICATION_ID = 1;
    private boolean isServiceStoped = false;
    private boolean isDestory = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("In BIND", "onBind");
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("In service", "onCreate");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);

        timeSwapBuff = AppPreference.getLongPreference(getApplicationContext(), Constants.TIMER_TIME);
        if (timeSwapBuff != 0) {
            setTime(milliTimeToString(timeSwapBuff));
        }

        AppPreference.setStringPreference(getApplicationContext(), Constants.MINUTS, getTime());
        setNumSteps(AppPreference.getIntegerPreference(getApplicationContext(), Constants.STEPS));
        setDistance(getDistanceRun(getNumSteps()));
        oldKmValue = getDistance();
        setsPoints(AppPreference.getIntegerPreference(getApplicationContext(), Constants.POINTS));
        setDistance(AppPreference.getFloatPreference(getApplicationContext(), Constants.DISTANCE));
        setCalories(AppPreference.getIntegerPreference(getApplicationContext(), Constants.CALORIES));
        notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        startForgound();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e("In service", "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("In service", "onStartCommand");
        if (isServiceStoped) {
            Log.e("In service", "Service was Stopped");
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            simpleStepDetector = new StepDetector();
            simpleStepDetector.registerListener(this);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);

            timeSwapBuff = AppPreference.getLongPreference(getApplicationContext(), Constants.TIMER_TIME);
            if (timeSwapBuff != 0) {
                setTime(milliTimeToString(timeSwapBuff));
            }

            AppPreference.setStringPreference(getApplicationContext(), Constants.MINUTS, getTime());
            setNumSteps(AppPreference.getIntegerPreference(getApplicationContext(), Constants.STEPS));
            setDistance(getDistanceRun(getNumSteps()));
            oldKmValue = getDistance();
            //setsPoints(AppPreference.getIntegerPreference(getApplicationContext(), Constants.POINTS));
            setDistance(AppPreference.getFloatPreference(getApplicationContext(), Constants.DISTANCE));
            setCalories(AppPreference.getIntegerPreference(getApplicationContext(), Constants.CALORIES));
            notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            startForgound();
            isServiceStoped = false;
        }
        setsPoints(AppPreference.getIntegerPreference(getApplicationContext(), Constants.POINTS));
        // executorService.submit(new SencerActivity());
        mInstance = this;
        stopTimer();
        sendBroadcast(new Intent(DailyFragment.SERVICE_CREATED));
        if (intent != null) {
            String action = intent.getAction();
            System.out.println("action  " + action);
            if (!TextUtils.isEmpty(action)) {
                if (action.equals(ACTION_STOP)) {
                    System.out.println("sReceived Stop_Service Event");
                    onSelfStop();
                }
            }
        }

        return START_NOT_STICKY; //super.onStartCommand(intent, flags, startId);//}
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void step(long timeNs) {
        if (AppPreference.getBooleanPreference(mInstance, "isLogout")) {
            return;
        }
        this.timeNs = timeNs;
        executorService.submit(new SencerActivity());
        numSteps++;
        setDistance(getDistanceRun(getNumSteps()));
        setCalories(calculateCalories(getNumSteps()));
        setsPoints(calPoints(getDistance()));

        Log.d("step is --- ", numSteps + "");
        //Log.d("oldKmValue is --- ", oldKmValue + "");

      //  if (!AppPreference.getBooleanPreference(mInstance, "isLogout")) {
            updateNotificaion();
       // }
    }

    private void updateView() {
        if (getViewUpdateListner() == null)
            return;
        viewUpdateListner.onStepUpdate(getNumSteps(), getDistance(), getCalories(), getsPoints());
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private class SencerActivity implements Runnable {
        @Override
        public void run() {
            System.out.println("Running my task.");
            try {
                final long oldTime = timeNs;
                Thread.sleep(1000);
                Log.e("oldTime ", oldTime + "");
                Log.e("timeNs ", timeNs + "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (oldTime == timeNs) stopTimer();
                        else if (oldTime < timeNs) startTimer();
                    }
                }).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public float getDistanceRun(int steps) {
        return (float) (Math.round(((steps * 78) / (float) 100000) * 100.0) / 100.0);
    }

    public int calculateCalories(int steps) {
        return Math.round(steps / 21);
    }

    public int calPoints(float km) {
        Log.d("km is --- ", km + "");
        Log.d("oldKmValue is --- ", oldKmValue + "");

        if (km != 0.0 && km != oldKmValue)
            if ((km * 1000) % 200 == 0) {
                sPoints++;
                oldKmValue = km;
            }

        return sPoints;
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = Long.parseLong(arabicToDecimal(String.valueOf(SystemClock.uptimeMillis()))) - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            setTime(milliTimeToString(updatedTime));
            sendTimerBroadcast(getTime());
            customHandler.postDelayed(this, 0);
        }
    };
    private static String arabicToDecimal(String number) {
        char[] chars = new char[number.length()];
        for(int i=0;i<number.length();i++) {
            char ch = number.charAt(i);
            if (ch >= 0x0660 && ch <= 0x0669)
                ch -= 0x0660 - '0';
            else if (ch >= 0x06f0 && ch <= 0x06F9)
                ch -= 0x06f0 - '0';
            chars[i] = ch;
        }
        return new String(chars);
    }
    private String milliTimeToString(long milliTime) {
        int secs = (int) (milliTime / 1000);
        int mins = secs / 60;
        int hour = mins / 60;
        secs = secs % 60;
        mins = mins % 60;
        String time = String.format("%02d", hour) + ":" + String.format("%02d", mins) + ":"
                + String.format("%02d", secs);
        return time;
    }

    private void sendTimerBroadcast(String time) {
        Intent intent = new Intent(DailyFragment.UPDATE_TIMER);
        intent.putExtra("TIME", time);
        sendBroadcast(intent);
    }

    private void startTimer() {
        if (AppPreference.getBooleanPreference(getApplicationContext(), IS_TIMER_ACTIVE))
            return;
        AppPreference.setBooleanPreference(getApplicationContext(), IS_TIMER_ACTIVE, true);
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
    }

    private void stopTimer() {
        AppPreference.setBooleanPreference(getApplicationContext(), IS_TIMER_ACTIVE, false);
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("From stopTimer");
                saveStatus();
            }
        }).start();
    }

    public void resetValue() {
        timeSwapBuff = 0;
        timeInMilliseconds = 0;
        updatedTime = 0;
        setNumSteps(0);
        setDistance(0);
        setCalories(0);
        isServiceStoped = true;
        Log.e("Vlalue resterd", "resetValue");
        setTime(milliTimeToString(timeSwapBuff));
        sendTimerBroadcast(getTime());
        System.out.println("From resetValue");
        saveStatus();

        onDestroy();
    }

    public void resetLog() {
        setsPoints(0);
        resetValue();
    }

    public StepViewUpdateListner getViewUpdateListner() {
        return viewUpdateListner;
    }

    public void setViewUpdateListner(StepViewUpdateListner viewUpdateListner) {
        this.viewUpdateListner = viewUpdateListner;
    }

    public class MyBinder extends Binder {
        public CounterService getService() {
            return CounterService.this;
        }
    }

    @Override
    public void onDestroy() {
        customHandler.removeCallbacks(updateTimerThread);
        stopForeground(true);
        notificationmanager.cancelAll();
        stopSelf();
        super.onDestroy();
    }

    private void onSelfStop() {
        notificationmanager.cancelAll();
        stopSelf();
    }

    public void saveData() {
        AppPreference.setBooleanPreference(getApplicationContext(), IS_TIMER_ACTIVE, false);
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
        System.out.println("From saveData");
        saveStatus();
    }

    public void saveStatus() {
        Log.e("State", "saveStatus   timeSwapBuff -- " + timeSwapBuff);
        AppPreference.setLongPreference(getApplicationContext(), Constants.TIMER_TIME, timeSwapBuff);
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.STEPS, getNumSteps());
        AppPreference.setFloatPreference(getApplicationContext(), Constants.DISTANCE, getDistance());
        AppPreference.setStringPreference(getApplicationContext(), Constants.MINUTS, milliTimeToString(timeSwapBuff));
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.CALORIES, getCalories());
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.POINTS, getsPoints());
        if (AppPreference.getBooleanPreference(mInstance, "isLogout")) {
            return;
        }
        updateNotificaion();
    }

    public void updatePoints() {
        setsPoints(AppPreference.getIntegerPreference(getApplicationContext(), Constants.POINTS));
    }

    public Notification getNotification() {
        Intent stop_intent = new Intent(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, stop_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.customnotification);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_icon_svg)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(false)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(getString(R.string.app_name)))
                .setContentIntent(pIntent)
                .setContent(remoteViews).getNotification();

        notification.flags = Notification.FLAG_ONGOING_EVENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = remoteViews;
        }

        remoteViews.setImageViewResource(R.id.imgClear, R.drawable.ic_clear_black_24dp);

        remoteViews.setOnClickPendingIntent(R.id.imgClear, pendingIntent);
        remoteViews.setTextViewText(R.id.steps, "Steps - " + String.valueOf(getNumSteps()));
        remoteViews.setTextViewText(R.id.calories, "Calories - " + String.valueOf(getCalories()));
        //  remoteViews.setTextViewText(R.id.time, time);

        return notification;
    }

    private void startForgound() {
        startForeground(NOTIFICATION_ID, getNotification());
    }

    public void pStopForgound() {
        notificationmanager.cancelAll();
        AppPreference.setBooleanPreference(mInstance, "isLogout", true);
        onDestroy();
    }

    private void updateNotificaion() {
        notificationmanager.notify(NOTIFICATION_ID, getNotification());
        updateView();
    }

    /*public void stopThread(){
        if(mInstance.getThread()!=null){
    getThread().interrupt();
            myService.setThread(null);
        }
    }*/


    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(int numSteps) {
        this.numSteps = numSteps;
    }

    public int getsPoints() {
        return sPoints;
    }

    public void setsPoints(int sPoints) {
        this.sPoints = sPoints;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

}
/*

    void adlfjasdoj() {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i("LOG_TAG", "Received Start Foreground Intent ");

            Intent notificationIntent = new Intent(this, MainActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            Intent previousIntent = new Intent(this, ForegroundService.class);
            previousIntent.setAction(Constants.ACTION.PREV_ACTION);
            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, previousIntent, 0);



            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Truiton Music Player")
                    .setTicker("Truiton Music Player")
                    .setContentText("My Music")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .addAction(android.R.drawable.ic_media_previous,
                            "Previous", ppreviousIntent)
                    .addAction(android.R.drawable.ic_media_play, "Play",
                            pplayIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next",
                            pnextIntent).build();
            startForeground(NOTIFICATION_ID, notification);


        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Log.i(LOG_TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Log.i(LOG_TAG, "Clicked Next");
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
    }
*/

