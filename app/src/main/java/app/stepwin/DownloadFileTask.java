package app.stepwin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by user on 03/09/2017.
 */

public class DownloadFileTask extends AsyncTask<String, String, String> {

/**
 * Before starting background thread
 * Show Progress Bar Dialog
 * */

private Context mContext;
SharedPreferences preferences;
String pref = "MyPref";
SharedPreferences.Editor editor;
DownloadFileTask(Context context) {
    this.mContext = context;
}
@Override
protected void onPreExecute() {
        super.onPreExecute();
        }

/**
 * Downloading file in background thread
 * */
String filepath = "";
@Override
protected String doInBackground(String... f_url) {
        preferences = mContext.getSharedPreferences(pref, Context.MODE_PRIVATE);
        editor = preferences.edit();
        int count;
        try {
        URL url = new URL(f_url[0]);
        String username = f_url[1];
        URLConnection conection = url.openConnection();
        conection.connect();
        // getting file length
        int lenghtOfFile = conection.getContentLength();

        // input stream to read file - with 8k buffer
        InputStream input = new BufferedInputStream(url.openStream(), 8192);

        filepath = Environment.getExternalStorageDirectory() +
                        File.separator + mContext.getResources().getString(R.string.app_name) + File.separator + username + ".csv";
        OutputStream output = new FileOutputStream(filepath);

        byte data[] = new byte[1024];

        long total = 0;

        while ((count = input.read(data)) != -1) {
        total += count;
        // publishing the progress....
        // After this onProgressUpdate will be called
        publishProgress(""+(int)((total*100)/lenghtOfFile));

        // writing data to file
        output.write(data, 0, count);
        }

        // flushing output
        output.flush();

        // closing streams
        output.close();
        input.close();

        } catch (Exception e) {
        Log.e("Error: ", e.getMessage());
        }

        return null;
        }

/**
 * After completing background task
 * Dismiss the progress dialog
 * **/
@Override
protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        Log.i("downloaded", "downloaded");
        ReadCSVFile(filepath);
        }
        private void ReadCSVFile(String path) {
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + mContext.getResources().getString(R.string.app_name) + File.separator + path + ".csv");

//Get the text file

//Read text from file
                String[] columns = new String[100];
                String[] rows;
                String step = "0", time = "0";
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
                for(int a = 0; a < columns.length; a++) {
                        if(columns[a] != null) {
                                Log.i("filecontent", a + "  " + columns[a]);
                                rows =  columns[a].split(",");

                                step = rows[1].replace("\"", "");
                                time = rows[3].replace("\"", "");
                        }

                }
                editor.putFloat("steps", Float.parseFloat(step));
                editor.putInt("secs", Integer.parseInt(time) * 60 );
                editor.commit();


        }


}