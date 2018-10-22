package app.stepwin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import app.stepwin.constants.Constants;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.utils.AppAlerts;
import app.stepwin.utils.AppPreference;
import app.stepwin.utils.AppProgressDialog;
import app.stepwin.utils.NetworkHandler;
import app.stepwin.utils.SocialLoginProvider;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class LogInActivity extends SocialLoginProvider implements WebResponse {

    private enum LoginType {EMAIL, USERNAME, FACEBOOK}

    private LoginType type;

    private FloatingActionButton next;
    private TextView signupnow;
    private EditText username, password;
    private String mFullname = "", email = "", strPassword = "", sexe, birthdate;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private RetrofitApiClient retrofitApiClient;


    @Override
    public void hideProgress() {

    }

    @Override
    public void googleSingInResult(boolean status, String[] person) {

    }

    @Override
    public void facebookSingInResult(boolean status, String[] user) {
        if (status) {
            type = LoginType.FACEBOOK;
            mFullname = user[SocialLoginProvider.USER_NAME];
            email = user[SocialLoginProvider.USER_EMAIL];
            sexe = user[SocialLoginProvider.FB_GENDER];
            birthdate = user[SocialLoginProvider.FB_BIRTH_DAY];
            RetrofitService.performLogin(new Dialog(mContext), retrofitApiClient.loginFb(), LogInActivity.this);
        } else {
            AppAlerts.showAlertMessage(mContext, "Error", "Something went wrong please Retry !!! ");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mContext = this;
        String myPREFERENCES = "MyPref";
        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        retrofitApiClient = RetrofitService.newUrlRetrofitService();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

        password = (EditText) findViewById(R.id.password);
        username = (EditText) findViewById(R.id.username);
        next = (FloatingActionButton) findViewById(R.id.next);

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    next.performClick();
                }
                return false;
            }
        });

        signupnow = (TextView) findViewById(R.id.signupNow);
        signupnow.setPaintFlags(signupnow.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        signupnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    LaunchActivity(SignUpActivityOne.class);
                } else
                    NetworkAlert();

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    if (username.length() == 0) {
                        Toast.makeText(mContext, "Enter username", Toast.LENGTH_SHORT).show();
                    } else if (password.length() == 0) {
                        Toast.makeText(mContext, "Enter password", Toast.LENGTH_SHORT).show();
                    } else {
                        email = username.getText().toString().trim();
                        strPassword = password.getText().toString().trim();
                        if (isEmail(username.getText().toString())) {
                            type = LoginType.EMAIL;
                            editor.putString("email", email);
                        } else {
                            type = LoginType.USERNAME;
                            editor.putString("user", email);
                        }
                        editor.commit();
                        AppPreference.setStringPreference(mContext, Constants.EMAIL, email);
                        RetrofitService.performLogin(new Dialog(mContext), retrofitApiClient.login(mFullname, email), LogInActivity.this);
                    }
                } else {
                    NetworkAlert();
                }
            }
        });
    }

    @Override
    public void onResponseSuccess(Response<?> result) {
        Response<ResponseBody> response = (Response<ResponseBody>) result;
        try {
            String strResult = response.body().string();
            new ParceJsonTask().execute(strResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResponseFailed(String error) {
        AppAlerts.showAlertMessage(mContext, "Error", error);
    }


    private class ParceJsonTask extends AsyncTask<String, String, Boolean> {
        private Dialog dialog;
        private String errorMsg = "";
        private String strName = "";
        private String strMail = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new Dialog(mContext);
            AppProgressDialog.show(dialog);
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            String strData = arg0[0];
            try {
                if (strData != null && !strData.isEmpty()) {
                    JSONObject jsonObject = new JSONObject(strData);
                    JSONArray response = jsonObject.optJSONArray("database_fetch_result");
                    switch (type) {
                        case EMAIL:
                            return login(response, "Email");
                        case USERNAME:
                            return login(response, "Username");
                        case FACEBOOK:
                            if (FacebookAccountExists(response, mFullname, email)) {
                                editor.putString("email", email);
                                editor.putString("name", mFullname);
                                editor.putString("user", mFullname);
                                editor.putBoolean("loggedin", true);
                                editor.commit();
                                return true;
                            } else {
                                errorMsg = "LOGIN_BY_FB_NEED_TO_CREATE_ACCOUNT";
                                return false;
                            }
                    }
                } else {
                    errorMsg = "Account not exist, Please login first.";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                errorMsg = "Parsing exception.";
            }

            return false;
        }

        private boolean login(JSONArray response, String type) {
            if (AccountExists(response, email, type)) {
                JSONObject json = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        json = response.getJSONObject(i);
                        if (email.equals(json.optString(type)) && strPassword.equals(json.optString("Password"))) {
                            editor.putString("email", json.optString("Email"));
                            editor.putString("user", json.optString("Username"));
                            editor.putBoolean("loggedin", true);
                            editor.commit();
                            strName = json.optString("Username");
                            strMail = json.optString("Email");
                            mFullname = strName;
                            email = strMail;
                            return true;
                        }

                        if (email.equals(json.optString(type)) && !strPassword.equals(json.optString("Password"))) {
                            errorMsg = "Incorrect password!";
                            return false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                errorMsg = "Account doesn't exist!";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            AppProgressDialog.hide(dialog);
            if (result) {
                getPreviousData();
            } else if (errorMsg.equals("LOGIN_BY_FB_NEED_TO_CREATE_ACCOUNT")) {
                Intent i = new Intent(LogInActivity.this, SignUpActivityTwo.class);
                i.putExtra(Constants.EMAIL, email);
                i.putExtra(Constants.NAME, mFullname);
                i.putExtra(Constants.SEX, sexe);
                i.putExtra(Constants.BIRTHDAY, birthdate);
                i.putExtra(Constants.FACEBOOK_LOGIN, true);
                startActivity(i);
                LogInActivity.this.finish();
            } else {
                AppAlerts.showAlertMessage(mContext, "Error", errorMsg);
            }
        }
    }

    private void getPreviousData() {
        RetrofitApiClient client = RetrofitService.getRetrofit();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
        String date = df.format(c.getTime());

        RetrofitService.getServerResponseForProgress(new Dialog(mContext)
                , client.userPreviousL(mFullname, email, date), new WebResponse() {
                    @Override
                    public void onResponseSuccess(Response<?> result) {
                        Response<ResponseBody> response = (Response<ResponseBody>) result;
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            int id = Integer.parseInt(jsonObject.getString("id"));
                            if (id > 0) {
                                int sPoints = Integer.parseInt(jsonObject.getString("points"));
                                int step = Integer.parseInt(jsonObject.getString("step"));
                                int calories = Integer.parseInt(jsonObject.getString("calories"));
                                float distant = Float.parseFloat(jsonObject.getString("distant"));
                                String minute = jsonObject.getString("minute");
                                String strDate = jsonObject.getString("date");
                                int time_milli_sec = jsonObject.getInt("time_milli");

                                AppPreference.setLongPreference(mContext, Constants.TIMER_TIME, time_milli_sec);
                                //AppPreference.setStringPreference(mContext, Constants.MINUTS, minute);
                                AppPreference.setIntegerPreference(mContext, Constants.STEPS, step);
                                AppPreference.setFloatPreference(mContext, Constants.DISTANCE, distant);
                                AppPreference.setIntegerPreference(mContext, Constants.CALORIES, calories);
                                AppPreference.setIntegerPreference(mContext, Constants.POINTS, sPoints);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        AppPreference.setBooleanPreference(mContext, "isLogout", false);
                        LaunchActivity(MainActivity.class);
                    }

                    @Override
                    public void onResponseFailed(String error) {
                        AppAlerts.showAlertMessage(mContext, "Please Login Again", error);
                    }
                });
    }

    ////////////////////////////////////////////

    private void NetworkAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Connection Failed");
        alertDialogBuilder.setMessage("Please check your internet connection");
        alertDialogBuilder.setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                startActivity(getIntent());
            }
        });
        alertDialogBuilder.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogInActivity.this.finish();
            }
        });
        alertDialogBuilder.setCancelable(false);
        AlertDialog alertD = alertDialogBuilder.create();
        alertD.show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private boolean isEmail(String text) {
        return text.contains("@");
    }

    private boolean AccountExists(JSONArray array, String username, String donno) {
        return array.toString().contains("\"" + donno + "\":\"" + username + "\"");
    }

    private boolean FacebookAccountExists(JSONArray array, String fullname, String email) {
        if (array.toString().contains(fullname))
            return true;
        else if (!email.equals("") && array.toString().contains(email)) {
            return true;
        } else
            return false;
    }

    private void LaunchActivity(Class clas) {
        Intent i = new Intent(LogInActivity.this, clas);
        startActivity(i);
        LogInActivity.this.finish();
    }
}
