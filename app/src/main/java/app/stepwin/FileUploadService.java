package app.stepwin;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileUploadService extends Service {
    Bundle extras;
    String audioPath, path, fileName, email;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            extras = intent.getExtras();
            if (extras != null) {
                audioPath = extras.getString("path");
                email = extras.getString("email");
            }
            fileName = audioPath.substring(audioPath.lastIndexOf("/") + 1);
            new FileUpload(getApplicationContext()).execute("http://stepwin.000webhostapp.com/upload_server.php", audioPath, "uploadedfile", "text/csv", email + "/");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public class FileUpload  extends AsyncTask<String, Integer, Void> {

        private Context mContext;

        public FileUpload(Context context) {
            mContext = context;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("fileupload", "uploaded");
        }

        @Override
        protected Void doInBackground(String... params) {
            String urlTo = params[0], filepath = params[1], filefield = params[2], Type = params[3], Server_path = params[4];
            String twoHyphens = "--";
            String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
            String lineEnd = "\r\n";

            String result = "";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 50 * 1024 * 1024;

            String[] q = filepath.split("/");
            int idx = q.length - 1;

            try {
                File file = new File(filepath);

                FileInputStream fileInputStream = new FileInputStream(file);

                URL url = new URL(urlTo);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(1024 * 500);
                connection.setReadTimeout(30000);

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"path\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(Server_path + lineEnd);
                outputStream.flush();

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + lineEnd);
                //audio :audio/mpeg
                //image image/jpeg
                // outputStream.writeBytes("Content-Type: " + Type + lineEnd);
                outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);

                buffer = new byte[bufferSize];

                int sentBytes = 0;
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    sentBytes += bufferSize;
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                // Upload POST Data

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                InputStream inputStream = connection.getInputStream();

                fileInputStream.close();
                inputStream.close();
                outputStream.flush();
                outputStream.close();

                connection.disconnect();
                return null;
            } catch (Exception e) {
                Log.e("MultipartRequest", "Multipart Form Upload Error");
                e.printStackTrace();
                return null;
            }
        }
    }
}

