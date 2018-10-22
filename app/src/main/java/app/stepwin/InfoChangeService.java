package app.stepwin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by user on 29/08/2017.
 */

public class InfoChangeService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    String email, Parameters, pref = "MyPref";
    SharedPreferences shared;
    Bundle extras;
    String fullname, sexe, height, weight;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        extras = intent.getExtras();
        shared = getSharedPreferences(pref, Context.MODE_PRIVATE);
        if(extras != null) {
            fullname = extras.getString("name", "");
            sexe = extras.getString("sexe", "");
            height = extras.getString("height", "");
            weight = extras.getString("weight", "");
            email = shared.getString("email", "");
            Parameters = "Fullname=" + fullname +"&sexe=" + sexe + "&height=" + height + "&weight=" + weight + "&email=" + email;
            new PostData().execute("http://stepwin.000webhostapp.com/UpdatePersonalDetails.php", Parameters);
        }
            else {
            email = shared.getString("email", "");
            Parameters = "email=" + email;
            new PostData().execute("http://stepwin.000webhostapp.com/delete.php", Parameters);
            Log.i("params", Parameters);
        }
        return super.onStartCommand(intent, flags, startId);
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
            Log.i("resultingpost", result + " ");
            InfoChangeService.this.stopSelf();
        }
    }

}
