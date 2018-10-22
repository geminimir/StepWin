package app.stepwin;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

import app.stepwin.constants.Constants;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.utils.AppAlerts;
import app.stepwin.utils.AppPreference;
import app.stepwin.utils.NetworkHandler;
import retrofit2.Response;

public class GiftActivity extends AppCompatActivity implements WebResponse{
    ListView listView;
    List<String> listTitles, buttonTitles;
    Button button;
    private Button share;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private CallbackManager callbackManager;
    private RetrofitApiClient retrofitApiClient;
    private NetworkHandler networkHandler;
    int points;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_gift);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
        mContext = this;

        networkHandler = new NetworkHandler(getApplicationContext());
        retrofitApiClient = RetrofitService.getRetrofit();

        FacebookSdk.sdkInitialize(GiftActivity.this);
        callbackManager = CallbackManager.Factory.create();



        listView = (ListView) findViewById(R.id.listview);
        button = (Button) findViewById(R.id.pay);
        share = (Button) findViewById(R.id.sharebutton);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean shareDone = AppPreference.getBooleanPreference(mContext,Constants.FB_SHARE_DONE);
                if(!shareDone){
                    share();
                }else{
                    AppAlerts.showAlertMessage(mContext,"Sorry", "You can share on Facebook only once per Day.");
                }

            }
        });
        // Defined Array values to show in ListView
        String[] values = {"$1",
                "$3",
                "$5",
                "$10"
        };
        String[] buttonvalues = {"3000", "10000", "15000", "28000"};
        listTitles = new ArrayList<String>();
        buttonTitles = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            listTitles.add(i, values[i]);
            buttonTitles.add(i, buttonvalues[i]);
        }
        ListAdapter adapter = new ListAdapter(getApplicationContext(), listTitles, buttonTitles, GiftActivity.this);
        listView.setAdapter(adapter);
    }

    private void share() {
        ShareDialog shareDialog;
        shareDialog = new ShareDialog(GiftActivity.this);
                /*ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("Your very best personal trainer!")
                        .setContentUrl(Uri.parse("http://freelancer.com/")).build();*/
        //=========================================================================>>
/*
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle("StepWin Health App\nYour very best personal trainer!")
                .setImageUrl(Uri.parse(""))
                .setContentDescription("")
                .setContentUrl(Uri.parse(""))
                .build();*/

        ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
                .putString("og:type", "fitness.course")
                .putString("og:title", "StepWin Health App")
                .putString("og:url", "https://play.google.com/store/apps/details?id=app.stepwin")
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
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {

                AppPreference.setBooleanPreference(mContext,Constants.FB_SHARE_DONE,true);

                points = sharedPreferences.getInt("points", 0) + 10;
                editor.putInt("points", points);
                editor.apply();

                int points = AppPreference.getIntegerPreference(GiftActivity.this, Constants.POINTS);
                points = points + 10;
                AppPreference.setIntegerPreference(GiftActivity.this, Constants.POINTS, points);
                shareCount();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        shareDialog.show(linkContent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void shareCount(){
        String uname = AppPreference.getStringPreference(mContext, Constants.USER_NAME);
        String uemail = AppPreference.getStringPreference(mContext, Constants.EMAIL);
        RetrofitService.getServerResponseForProgress(new Dialog(mContext)
                , retrofitApiClient.fbSharecount(uname, uemail),this);
    }

    @Override
    public void onResponseSuccess(Response<?> result) {

    }

    @Override
    public void onResponseFailed(String error) {

    }
}




