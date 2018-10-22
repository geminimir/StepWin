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
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import app.stepwin.constants.Constants;
import app.stepwin.retrofit_provider.RetrofitApiClient;
import app.stepwin.retrofit_provider.RetrofitService;
import app.stepwin.retrofit_provider.WebResponse;
import app.stepwin.utils.AppPreference;
import app.stepwin.utils.NetworkHandler;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class LogInActivityOld extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Animation fab_in, fab_out;
    private FloatingActionButton next;
    private TextView signupnow;
    private EditText username, password;
    private ProgressDialog mProgressDialog;
    private String type, mFullname, email, sexe, birthdate;
    private TextInputLayout user, pass;
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private boolean facebooklogin;
    private Context mContext;

    private NetworkHandler networkHandler;
    private RetrofitApiClient retrofitApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_log_in);
        mContext = this;
        String myPREFERENCES = "MyPref";
        sharedpreferences = getSharedPreferences(myPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        retrofitApiClient = RetrofitService.getRetrofit();
        networkHandler = new NetworkHandler(mContext);

        disconnectFromFacebook();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_birthday"));

        mProgressDialog = new ProgressDialog(LogInActivityOld.this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(LogInActivityOld.this.getString(R.string.dialog_name));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("userid", loginResult.getAccessToken().getUserId());
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (response != null) {
                                    String mFbid = object.optString("id");
                                    mFullname = object.optString("name");
                                    Log.i("Object", object.toString());
                                    email = object.optString("email");
                                    sexe = object.optString("gender");
                                    birthdate = object.optString("user_birthday");
                                    Log.i("fbinfo", mFbid);
                                    Log.i("fbinfo", mFullname);
                                    Log.i("fbinfo", email);
                                    Log.i("fbinfo", sexe + " ");
                                    Log.i("fbinfo", birthdate + " ");
                                    Toast.makeText(LogInActivityOld.this, mFbid + ", " + mFullname + ", " + email + ", " + sexe + ", " + birthdate, Toast.LENGTH_SHORT).show();
                                    next.startAnimation(fab_out);
                                    next.setVisibility(View.INVISIBLE);
                                    facebooklogin = true;
                                    new PostData().execute("http://stepwin.000webhostapp.com/FacebookLogin.php", "");
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,name,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                if (!isNetworkConnected())
                    NetworkAlert();
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("facebookerror", error.toString());
            }
        });

        fab_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_in);
        fab_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_out);

        password = (EditText) findViewById(R.id.password);
        username = (EditText) findViewById(R.id.username);
        user = (TextInputLayout) findViewById(R.id.editText);
        pass = (TextInputLayout) findViewById(R.id.editText2);


        next = (FloatingActionButton) findViewById(R.id.next);
        next.setVisibility(View.INVISIBLE);

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    next.performClick();
                }
                return false;
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                next.setVisibility(View.VISIBLE);
                next.startAnimation(fab_in);
            }
        }, 1000);
        signupnow = (TextView) findViewById(R.id.signupNow);
        signupnow.setPaintFlags(signupnow.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        signupnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LaunchActivity(SignUpActivityOne.class);
                        }
                    }, 450);
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
                        if (isEmail(username.getText().toString())) {
                            type = "Email";
                            editor.putString("email", username.getText().toString());
                            //============================================================>>
                            AppPreference.setStringPreference(mContext, Constants.EMAIL, username.getText().toString());
                            //============================================================>>
                        } else {
                            type = "Username";
                            editor.putString("user", username.getText().toString());
                            //============================================================>>
                            //  AppPreference.setStringPreference(mContext, Constants.USER_NAME, username.getText().toString());
                            //============================================================>>
                        }
                        editor.commit();
                        next.startAnimation(fab_out);
                        next.setVisibility(View.INVISIBLE);
                        facebooklogin = false;
                        new PostData().execute("https://stepwin.000webhostapp.com/login.php", "Fullname=" + mFullname + "&Email=" + email);
                    }
                } else {
                    NetworkAlert();
                }
            }
        });
    }

    public void disconnectFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
    }

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
                LogInActivityOld.this.finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void LaunchActivity(Class clas) {
        Intent i = new Intent(LogInActivityOld.this, clas);
        startActivity(i);
        LogInActivityOld.this.finish();
    }


    private class PostData extends AsyncTask<String, String, String> {
        Context mContext;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... arg0) {
            //mProgressDialog.show();
            String URI = arg0[0];
            String POST_PARAMS = arg0[1];
            String responseStr = null;
            HttpURLConnection connection = null;
            try {
// Create connection
                URL url = new URL(URI);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(POST_PARAMS.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
// Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(POST_PARAMS);
                wr.flush();
                wr.close();
// Get Response
                InputStream is = connection.getInputStream();

                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                responseStr = response.toString();

            } catch (Exception e) {

                e.printStackTrace();

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
            return responseStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (!facebooklogin) {
                try {
                    if (result != null) {
                        if (!result.isEmpty()) {
                            JSONObject jsonObject = new JSONObject(result);
                            JSONArray response = jsonObject.optJSONArray("database_fetch_result");
                            if (AccountExists(response, username.getText().toString(), type)) {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject json = response.getJSONObject(i);
                                    if (username.getText().toString().equals(json.optString(type)) && password.getText().toString().equals(json.optString("Password"))) {
                                        getPreviousData(json.optString("Username"), json.optString("Email"));
                                        LaunchActivity(MainActivity.class);
                                        editor.putString("email", json.optString("Email"));
                                        editor.putString("user", json.optString("Username"));
                                        editor.putBoolean("loggedin", true);
                                        editor.commit();

                                        //============================================================>>
//                                        AppPreference.setStringPreference(mContext, Constants.EMAIL, json.optString("Email"));
//                                        AppPreference.setStringPreference(mContext, Constants.USER_NAME, json.optString("Username"));
//                                        AppPreference.setBooleanPreference(mContext, Constants.LOGED_IN, true);
//                                        //============================================================>>


                                    }
                                    if (username.getText().toString().equals(json.optString(type)) && !password.getText().toString().equals(json.optString("Password"))) {
                                        pass.setError("Incorrect password!");
                                        user.setError("");
                                        mProgressDialog.dismiss();
                                        next.setVisibility(View.VISIBLE);
                                        next.setAnimation(fab_in);
                                    }
                                    if (username.getText().toString().isEmpty()) {
                                        user.setError("Emply Field!");
                                        mProgressDialog.dismiss();
                                        next.setVisibility(View.VISIBLE);
                                        next.setAnimation(fab_in);
                                    }
                                    if (password.getText().toString().isEmpty()) {
                                        pass.setError("Empty Field!");
                                        mProgressDialog.dismiss();
                                        next.setVisibility(View.VISIBLE);
                                        next.setAnimation(fab_in);
                                    }

                                }
                            } else {
                                user.setError("Account doesn't exist!");
                                pass.setError("");
                                mProgressDialog.dismiss();
                                next.setVisibility(View.VISIBLE);
                                next.setAnimation(fab_in);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    JSONArray response = jsonObject.optJSONArray("database_fetch_result");
                    if (FacebookAccountExists(response, mFullname, email)) {
                        mProgressDialog.dismiss();
                        getPreviousData(mFullname, email);
                        LaunchActivity(MainActivity.class);
                        editor.putString("email", email);
                        editor.putString("name", mFullname);
                        editor.putString("user", mFullname);
                        editor.putBoolean("loggedin", true);
                        editor.commit();
                        //========================================================================>>
                      /*  if (email!=null)
                        AppPreference.setStringPreference(mContext, Constants.EMAIL, email);
                        AppPreference.setStringPreference(mContext, Constants.NAME, mFullname);
                        AppPreference.setStringPreference(mContext, Constants.USER_NAME, mFullname);
                        AppPreference.setBooleanPreference(mContext, Constants.LOGED_IN, true);*/
                        //==========================================================================>>
                    } else {
                        mProgressDialog.dismiss();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(LogInActivityOld.this, SignUpActivityTwo.class);
                                i.putExtra(Constants.EMAIL, email);
                                i.putExtra(Constants.NAME, mFullname);
                                i.putExtra(Constants.SEX, sexe);
                                i.putExtra(Constants.BIRTHDAY, birthdate);
                                i.putExtra(Constants.FACEBOOK_LOGIN, true);
                                startActivity(i);
                                LogInActivityOld.this.finish();
                            }
                        }, 400);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

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
        else if (array.toString().contains(email))
            return true;
        else
            return false;
    }

    private void getPreviousData(String uname, String uemail) {
        RetrofitService.getServerResponseForProgress(new Dialog(mContext)
                , retrofitApiClient.userPreviousL(uname, uemail,""), new WebResponse() {
                    @Override
                    public void onResponseSuccess(Response<?> result) {
                        Response<ResponseBody> response = (Response<ResponseBody>) result;
                        
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            int id = Integer.parseInt(jsonObject.getString("id"));
                            if (id > 0) {
                                int sPoints = Integer.parseInt(jsonObject.getString("points"));
                                AppPreference.setIntegerPreference(mContext, Constants.POINTS, sPoints);
                                Toast.makeText(mContext, "Welcome Back..!", Toast.LENGTH_SHORT).show();
                            } else {
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
                });
    }
}
