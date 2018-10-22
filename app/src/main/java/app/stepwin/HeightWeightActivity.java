package app.stepwin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HeightWeightActivity extends AppCompatActivity {

    EditText height, weight;
    Button register;
    String email, fullname, sexe, hei, wei, birthday;
    private ProgressDialog mProgressDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String pref = "MyPref";
    private TextInputLayout heightlayout, weightlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_weight);

        heightlayout = (TextInputLayout)findViewById(R.id.hei);
        weightlayout = (TextInputLayout)findViewById(R.id.wei);

        sharedPreferences = getSharedPreferences(pref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            email = extras.getString("email");
            fullname = extras.getString("name");
            sexe = extras.getString("sexe");
            birthday = extras.getString("birthday", " ");
        }

        mProgressDialog = new ProgressDialog(HeightWeightActivity.this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(HeightWeightActivity.this.getString(R.string.dialog_name));


        height = (EditText)findViewById(R.id.height);
        weight = (EditText)findViewById(R.id.weight);
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected()) {
                    hei = height.getText().toString();
                    wei = weight.getText().toString();
                    if(wei.isEmpty())
                        weightlayout.setError("Weight is required!");
                    else
                        weightlayout.setError("");
                    if(hei.isEmpty())
                        heightlayout.setError("Height is required!");
                    else
                        heightlayout.setError("");
                    if(!wei.isEmpty() && !hei.isEmpty()) {
                        mProgressDialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RegisterProcess(fullname, birthday, sexe, hei,
                                        wei, email, fullname);
                                mProgressDialog.dismiss();
                            }
                        }, 2000);
                    }
                } else {
                    NetworkAlert();
                }
            }
        });
    }
    private void RegisterProcess(final String name, final String birthdate, final String sexe, final String height, final String weight, final String email, final String fullnam) {

            new PostData().execute("http://stepwin.000webhostapp.com/registerFacebookUsers.php", "Fullname=" + name + "&Email=" + email);
            new PostData().execute("https://stepwin.000webhostapp.com/registerData.php", "Fullname=" + fullnam +"&Email=" + email + "&Birthdate=" + birthdate + "&sexe=" + sexe + "&height=" + height + "&weight=" + weight);
            editor.putString("user", fullnam);
            editor.putString("name", name);
            editor.putString("email", email);
            editor.putString("birthdate", birthdate);
            editor.putString("weight", weight);
            editor.putString("height", height);
            editor.putString("sexe", sexe);
            editor.putBoolean("loggedin", true);
            editor.commit();
            Intent i = new Intent(HeightWeightActivity.this, MainActivity.class);
            startActivity(i);
            HeightWeightActivity.this.finish();

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
                HeightWeightActivity.this.finish();
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
    private class PostData extends AsyncTask<String, String, String> {

        Context mContext;
        @Override
        protected void onPreExecute() {
            Log.d("PreExceute", "On pre Exceute......");
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
            Log.i("resultingpost", result);
        }
    }

}
