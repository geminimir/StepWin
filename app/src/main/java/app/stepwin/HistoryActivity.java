package app.stepwin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.stepwin.adapter.*;
import app.stepwin.constants.Constants;
import app.stepwin.model.UserHistory;
import app.stepwin.model.UserHistoryList;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.utils.AppPreference;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;

public class HistoryActivity extends AppCompatActivity implements WebResponse{

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    RecyclerView listView;

    String[] values= new String[100], stepsvalues = new String[100];
    View rootLayout;

    SharedPreferences sharedPreferences;
    private String username = "";
    private String email = "";
    private int revealX;
    private int revealY;
    private int stepsmonthly = 0;
    private RetrofitApiClient retrofitApiClient;

    private List<UserHistoryList> data;
    private HistoryAdapter adapter;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_icon);
        mContext=this;
        retrofitApiClient = RetrofitService.getRetrofit();




        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE);
        final Intent intent = getIntent();
        listView = (RecyclerView) findViewById(R.id.recyclerViewHistory);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        listView.setLayoutManager(layoutManager);


        getHistoryList();







        rootLayout = findViewById(R.id.root_layout);

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
        }
        revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
        revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


        ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    revealActivity(revealX, revealY);
                    rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });

        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getHistoryList() {
        String username = AppPreference.getStringPreference(getApplicationContext(), Constants.USER_NAME);
        String email = AppPreference.getStringPreference(getApplicationContext(),Constants.EMAIL);

        RetrofitService.getUserHistoryList(new Dialog(getApplicationContext())
                , retrofitApiClient.userHistoryL(username, email), this);


    }


    @Override
    public void onResponseSuccess(Response<?> result) {

        Response<UserHistory> response = (Response<UserHistory>) result;
        UserHistory userHistory = response.body();
        if (userHistory != null)
            data = userHistory.getUserHistory();
            adapter = new HistoryAdapter(mContext,data);
            listView.setAdapter(adapter);

    }

    @Override
    public void onResponseFailed(String error) {

    }



    protected void revealActivity(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

            // create the animator for this view (the start radius is zero)
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(300);
            circularReveal.setInterpolator(new AccelerateInterpolator());
            // make the view visible and start the animation
            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        } else {
            finish();
        }
    }
    protected void unRevealActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finish();
        } else {
            float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0);

            circularReveal.setDuration(300);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rootLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
            });


            circularReveal.start();
        }
    }
   /* private void ReadCSVFile(String path) {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + getApplicationContext().getResources().getString(R.string.app_name) + File.separator + path + ".csv");

        String[] date = new String[1000], distance = new String[1000], time = new String[1000], calories = new String[1000];
        String[] stepsArray = new String[1000];
//Get the text file

//Read text from file
        String[] columns = new String[1000];
        String[] rows = new String[5];
        int i = 0;
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                i++;
                columns[i] = line;
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        date[0] = "0";
        stepsArray[0] = "0";
        String defaultstring = "0";
        int b = 0;
        for(int a = 1; a < columns.length; a++) {
            if (columns[a] != null) {
                Log.i("filecontent", a + "  " + columns[a]);
                rows = columns[a].split(",");
                date[a] = rows[0].replace("\"", "");
                stepsArray[a] = rows[1].replace("\"", "");
                if (!date[a].equals(date[a - 1])) {
                    stepsmonthly += Math.round(Float.parseFloat(stepsArray[a]));
                    Log.i("monthlysteps", stepsvalues[b] + "");
                    //if(date[a-1] != "0")
                    // b++;
                        Log.i("numbermonths", b + " ");
                        defaultstring = date[a].substring(5, 7);
                        Log.i("defaultstring", defaultstring);

                }
            }
            // StepsText.setText(String.valueOf(stepsmonthly) +  " step");
        }
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            unRevealActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
