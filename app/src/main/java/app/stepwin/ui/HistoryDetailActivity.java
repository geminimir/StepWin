package app.stepwin.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import app.stepwin.R;

import static app.stepwin.HistoryActivity.EXTRA_CIRCULAR_REVEAL_X;
import static app.stepwin.HistoryActivity.EXTRA_CIRCULAR_REVEAL_Y;

/**
 * Created by natraj on 11/10/17.
 */

public class HistoryDetailActivity extends AppCompatActivity{

    View rootLayout;
    private int revealX;
    private int revealY;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_icon);

        final Intent intent = getIntent();

        Intent i = getIntent();
        Bundle b = i.getExtras();
        String step = b.getString("step");
        String cal = b.getString("cal");
        String distance = b.getString("distance");
        String uc = b.getString("uc");
        String minute = b.getString("minute");
        String date = b.getString("date");
        String points = b.getString("point");

        ((TextView)findViewById(R.id.tvStep)).setText(step);
        ((TextView)findViewById(R.id.tvCal)).setText(cal);
        ((TextView)findViewById(R.id.tvDistance)).setText(distance);
        ((TextView)findViewById(R.id.tvUC)).setText(uc);
        ((TextView)findViewById(R.id.dataMinute)).setText(minute);
        ((TextView)findViewById(R.id.tvPoints)).setText(points);
        ((TextView)findViewById(R.id.tvDate)).setText(date);

        /*rootLayout = findViewById(R.id.root_layout);

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
        */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
          //  unRevealActivity();
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*protected void unRevealActivity() {
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
    }*/
}
