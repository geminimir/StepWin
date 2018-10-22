package app.stepwin;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ApplicationErrorReport;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.opencsv.CSVWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.stepwin.alarm_services.AlarmService;
import app.stepwin.constants.Constants;
import app.stepwin.services.CounterService;
import app.stepwin.utils.AppAlerts;
import app.stepwin.utils.NetworkHandler;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.unlock_counter.DB;
import app.stepwin.unlock_counter.LockerService;
import app.stepwin.utils.AppPreference;
import app.stepwin.utils.SocialLoginProvider;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WebResponse {

    float steps = 0;
    int seconds = 0;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    private FloatingActionButton history;
    private TextView username, email;
    public int numberlock = 0;
    //  PedometerService mBoundService;
    boolean mServiceBound = true;
    private ProgressDialog mProgressDialog;
    private RetrofitApiClient retrofitApiClient;
    GpsService gps;
    float savedsteps;
    private NetworkHandler networkHandler;
    private Context mContext;
    CallbackManager callbackManager;
    ShareDialog shareDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(MainActivity.this, LockerService.class));
        setContentView(R.layout.activity_main);
        retrofitApiClient = RetrofitService.getRetrofit();
        mContext = this;
        //  refresh_all();
        FacebookSdk.sdkInitialize(MainActivity.this);
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(MainActivity.this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Log.e("CallbackManager", "onSuccess");
                int points = AppPreference.getIntegerPreference(MainActivity.this, Constants.POINTS);
                points = points + 200;
                Log.e("CallbackManager", "points - " + points);
                AppPreference.setIntegerPreference(MainActivity.this, Constants.POINTS, points);
                if (CounterService.mInstance != null) {
                    CounterService.mInstance.updatePoints();
                }
            }

            @Override
            public void onCancel() {
                Log.e("CallbackManager", "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("CallbackManager", "onError");
            }
        });

        setAlarmForService();

        // mProgressDialog = new ProgressDialog(getApplicationContext(),null);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        String MyPREFERENCES = "MyPref";
        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        savedsteps = sharedPreferences.getFloat("savedsteps", 0);
        history = (FloatingActionButton) findViewById(R.id.history);
        history.setVisibility(View.INVISIBLE);

        networkHandler = new NetworkHandler(MainActivity.this);


        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(MainActivity.this.getString(R.string.dialog_name));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                history.setVisibility(View.VISIBLE);
                history.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_in));
            }
        }, 1000);



        if (mServiceBound) {


            Fragment frag = new DailyFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame, frag);
            transaction.commit();
        }
        history.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                presentActivity(history);
            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);

        username = (TextView) hView.findViewById(R.id.user_nav);
        email = (TextView) hView.findViewById(R.id.email_nav);

        String uName = sharedPreferences.getString("user", "");
        String uEmail = sharedPreferences.getString("email", "");

        username.setText(uName);
        email.setText(uEmail);

        AppPreference.setStringPreference(mContext, Constants.USER_NAME, uName);
        AppPreference.setStringPreference(mContext, Constants.EMAIL, uEmail);
    }

    private void setAlarmForService() {



        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 0);
        PendingIntent pi = PendingIntent.getService(MainActivity.this, 0,
                new Intent(MainActivity.this, AlarmService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.history) {
            Intent i = new Intent(MainActivity.this, GiftActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment frag = null;
        if (id == R.id.daily) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mServiceBound) {
                        history.setVisibility(View.VISIBLE);
                        history.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_in));

                        Fragment frag = new DailyFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                        // frag.setArguments(bundle);
                        transaction.commit();
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.closeDrawer(GravityCompat.START);
                    }

                }
            }, 300);
        } /*else if (id == R.id.map) {
            frag = new MapFragment();
            history.setAnimation((AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_out)));
            history.setVisibility(View.INVISIBLE);

            gps = new GpsService(MainActivity.this);
            if (!gps.isGPSEnabled) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Do you want to enable your location?");
                alertDialog.setMessage(getResources().getString(R.string.app_name) + " needs your location to track your exercising path.");
                final Fragment finalFrag = frag;
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ENABLE LOCATION",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                if (finalFrag != null)
                                    transaction.replace(R.id.frame, finalFrag); // replace a Fragment with Frame Layout
                                transaction.commit(); // commit the changes
                                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
            if (gps.isGPSEnabled) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if (frag != null)
                    transaction.replace(R.id.frame, frag); // replace a Fragment with Frame Layout
                transaction.commit(); // commit the changes
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        } */else if (id == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } /*else if (id == R.id.share) {
             //share();
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("StepWin Health App")
                    .setContentDescription("Your very best personal trainer!")
                    .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=app.stepwin")).build();
            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    Log.e("CallbackManager", "onSuccess");
                    int points = AppPreference.getIntegerPreference(MainActivity.this, Constants.POINTS);
                    points = points + 200;
                    Log.e("CallbackManager", "points - " + points);
                    AppPreference.setIntegerPreference(MainActivity.this, Constants.POINTS, points);
                    if (CounterService.mInstance != null) {
                        CounterService.mInstance.updatePoints();
                    }
                }

                @Override
                public void onCancel() {
                    Log.e("CallbackManager", "onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("CallbackManager", "onError");
                }
            });
            shareDialog.show(linkContent);

        } else if (id == R.id.logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Are you sure ?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (networkHandler.isNetworkAvailable()) {
                        sendProgressToServer();
                        mContext.stopService(new Intent(mContext,
                                CounterService.class));
                        CounterService.mInstance.pStopForgound();
                        AppPreference.setBooleanPreference(mContext, "isLogout", true);
                    } else {
                        AppAlerts.showAlertMessage(mContext, "Network Error", "Please Check your Network Connection");
                    }
                }
            });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialogBuilder.setCancelable(true);
            AlertDialog alertD = alertDialogBuilder.create();
            alertD.show();
        }*/
        return true;
    }

    private void share() {
        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "fitness.course")
                .putString("og:title", "StepWin Health App")
                .putString("og:description", "Your very best personal trainer!")
                .build(); // "https://play.google.com/store/apps/details?id=app.stepwin"

        ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
                .setActionType("fitness.walks").putObject("course", object)
                .build();
        ShareOpenGraphContent linkContent = new ShareOpenGraphContent.Builder()
                .setPreviewPropertyName("course")
                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=app.stepwin"))
                .setAction(action)
                .build();
        //=========================================================================>>
        shareDialog.show(linkContent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void resetPref() {
        Log.e("else", "CounterService == null");
        AppPreference.setLongPreference(getApplicationContext(), Constants.TIMER_TIME, 0);
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.STEPS, 0);
        AppPreference.setFloatPreference(getApplicationContext(), Constants.DISTANCE, 0);
        AppPreference.setStringPreference(getApplicationContext(), Constants.MINUTS, "00:00:00");
        AppPreference.setIntegerPreference(getApplicationContext(), Constants.CALORIES, 0);
        AppPreference.setIntegerPreference(mContext, Constants.POINTS, 0);
        //AppPreference.setBooleanPreference(getApplicationContext(),Constants.FB_SHARE_DONE,false);
    }

    private void sendProgressToServer() {
        if (CounterService.mInstance != null) {
            CounterService.mInstance.saveData();
        }
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(c.getTime());
        String steps = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.STEPS));
        String distance = String.valueOf(AppPreference.getFloatPreference(mContext, Constants.DISTANCE));
        String minutes = (AppPreference.getStringPreference(mContext, Constants.MINUTS));
        long time_milli = (AppPreference.getLongPreference(mContext, Constants.TIMER_TIME));
        String cals = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.CALORIES));
        String points = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.POINTS));
        String unlock_conter = String.valueOf(numberlock);
        String username = AppPreference.getStringPreference(mContext, Constants.USER_NAME);
        String email = AppPreference.getStringPreference(mContext, Constants.EMAIL);


        RetrofitService.getServerResponseForProgress(null
                , retrofitApiClient.postUserProgress(steps, distance, cals, points, minutes, time_milli, unlock_conter, date
                        , username, email), this);
    }


    @Override
    public void onResponseSuccess(Response<?> result) {
        Response<ResponseBody> response = (Response<ResponseBody>) result;
        try {
            JSONObject jsonObject = new JSONObject(response.body().string());
            String msg = String.valueOf(jsonObject.get("msg"));
            boolean status = (boolean) jsonObject.get("status");
            if (status) {
                if (CounterService.mInstance != null) {
                    Log.e("CounterService", "!= null");
                    CounterService.mInstance.resetLog();
                    /*mContext.stopService(new Intent(mContext,
                            CounterService.class));*/
                } else {
                    resetPref();
                }
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                mContext.stopService(new Intent(mContext,
                        CounterService.class));
                editor.putBoolean("loggedin", false);
                editor.commit();
                editor.putString("distance", "0.0");
                editor.putLong("calories", 0);
                editor.commit();
                SocialLoginProvider.facebookLogout(mContext);
                Intent i = new Intent(MainActivity.this, LogInActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                MainActivity.this.finish();
            } else {
                AppAlerts.showAlertMessage(mContext, "Error", msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponseFailed(String error) {

    }

    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);

        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(HistoryActivity.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(HistoryActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // PedometerService.MyBinder myBinder = (PedometerService.MyBinder) service;
            // mBoundService = myBinder.getService();
            //mServiceBound = true;
        }
    };


    @Override
    protected void onStop() {
        super.onStop();

        String steps = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.STEPS));
        String distance = String.valueOf(AppPreference.getFloatPreference(mContext, Constants.DISTANCE));
        String minutes = String.valueOf(TimeUnit.SECONDS
                .toMinutes(sharedPreferences.getInt("secs", 0)));
        String cals = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.CALORIES));
        String points = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.POINTS));


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.putFloat("savedsteps", steps);
        editor.putBoolean("reset", false);
        editor.commit();
        String steps = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.STEPS));
        String distance = String.valueOf(AppPreference.getFloatPreference(mContext, Constants.DISTANCE));
        String minutes = String.valueOf(TimeUnit.SECONDS
                .toMinutes(sharedPreferences.getInt("secs", 0)));
        String cals = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.CALORIES));
        String points = String.valueOf(AppPreference.getIntegerPreference(mContext, Constants.POINTS));


    }




    @Override
    protected void onResume() {
        super.onResume();
        refresh_all();
    }

    //=================================================================>>
   /* private boolean isMyServiceRunning(Class<?> CounterService) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CounterService.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/


    private void refresh_all() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Long now = Calendar.getInstance().getTimeInMillis();
                DB db = new DB(MainActivity.this);
                List<String[]> d = db.get_log();
                List<String[]> d_aftertime = new ArrayList<String[]>();

                if (d.size() > 0) {
                    numberlock = 0;
                    for (int i = 0; i < d.size(); i++) {
                        //Log.d(LOG_TAG, "lock = " + d.get(i)[0] + " other is = " + Double.toString(now - timestamp_to_reduce[current_selection]));
                        if (Double.parseDouble(d.get(i)[0]) >= now - 86400000d) {
                            numberlock++;
                            //Log.d(LOG_TAG, "this one ok");
                            d_aftertime.add(new String[]{d.get(i)[0], d.get(i)[1]});
                        }
                    }
                }

                AppPreference.setIntegerPreference(mContext, Constants.UNLOCK_COUNT, numberlock);
            }
        }).start();
        // int current_selection = get_current_spinner_selection();
        // Toast.makeText(mContext, " " + numberlock, Toast.LENGTH_SHORT).show();
    }
}
