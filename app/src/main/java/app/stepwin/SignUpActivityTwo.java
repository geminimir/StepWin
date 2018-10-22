package app.stepwin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.widget.Spinner;

import com.opencsv.CSVWriter;

import app.stepwin.constants.Constants;
import app.stepwin.utils.AppPreference;

public class SignUpActivityTwo extends AppCompatActivity {

    private Spinner sexeSpinner;
    private Button register;
    private EditText name, birthdate, weight, height, emailedit;
    private TextInputLayout nam, birth, wei, hei, Email;
    String username, email, password, fullname, sexestring, birthdatestring;
    private ProgressDialog mProgressDialog;
    String sexe;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean facebooklogin;
    String[] genders = {"Male", "Female"};
    int spinnerpostion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_two);

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            email = extras.getString("email");
            username = extras.getString("username");
            sexestring = extras.getString("sexe", " ");
            fullname = extras.getString("name", " ");
            birthdatestring = extras.getString("birthday", " ");
            password = extras.getString("password");
            facebooklogin = extras.getBoolean("facebooklogin", false);
        }
        sexeSpinner = (Spinner) findViewById(R.id.sexespinner);
        name = (EditText) findViewById(R.id.fullname);
        birthdate = (EditText) findViewById(R.id.birthdate);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        emailedit = (EditText) findViewById(R.id.emailedit);

        nam = (TextInputLayout) findViewById(R.id.nam);
        wei = (TextInputLayout) findViewById(R.id.hei);
        hei = (TextInputLayout) findViewById(R.id.wei);
        birth = (TextInputLayout) findViewById(R.id.birth);
        Email = (TextInputLayout) findViewById(R.id.Email);
        if (email == null || email.equals("")) {
            Email.setVisibility(View.VISIBLE);
        }

        if (!sexestring.isEmpty() && !fullname.isEmpty()) {
            name.setText(fullname);
            birthdate.setText(birthdatestring);
            for (int i = 0; i < genders.length; i++)
                if (genders[i].equals(sexestring))
                    spinnerpostion = i;
            sexeSpinner.setSelection(spinnerpostion);
        }

        weight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.performClick();
            }
        });
        sexeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) sexe = "Male";
                else sexe = "Female";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mProgressDialog = new ProgressDialog(SignUpActivityTwo.this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(SignUpActivityTwo.this.getString(R.string.dialog_name));


        birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(SignUpActivityTwo.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        birthdate.setText(selectedday + "-" + selectedmonth + "-" + selectedyear);
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });
        register = (Button) findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().length() < 3)
                    nam.setError("Name is too short!");
                else
                    nam.setErrorEnabled(false);

                if (height.getText().toString().isEmpty())
                    hei.setError("Height is required!");
                else
                    hei.setErrorEnabled(false);

                if (weight.getText().toString().isEmpty())
                    wei.setError("Weight is required!");
                else
                    wei.setErrorEnabled(false);

                if (email == null || email.equals("")) {
                    if (emailedit.getText().toString().isEmpty())
                        Email.setError("email is required!");
                    else
                        Email.setErrorEnabled(false);

                    email = emailedit.getText().toString().trim();
                }
                mProgressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        RegisterProcess(name.getText().toString(), birthdate.getText().toString(), sexe, height.getText().toString(),
                                weight.getText().toString(), email, username, password);
                        ExportCSV(getApplicationContext());
                        mProgressDialog.dismiss();
                    }
                }, 2000);

            }

        });
    }

    private void ExportCSV(Context context) {
        String date, csv = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        date = df.format(c.getTime());
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "." + context.getResources().getString(R.string.app_name));
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {

            csv = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/." + context.getResources().getString(R.string.app_name) + File.separator + sharedPreferences.getString("user", "") + ".csv";
            CSVWriter writer = null;
            try {
                writer = new CSVWriter(new FileWriter(csv, true));
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"date", "steps", "distance", "time", "calories", "points", "unlocks"});

            if (writer != null) {
                writer.writeAll(data);
            }
            try {
                if (writer != null) {
                    writer.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i("autosync", sharedPreferences.getBoolean("autosync", true) + "");
        if (sharedPreferences.getBoolean("autosync", true)) {
            Intent i = new Intent(context, FileUploadService.class);
            i.putExtra("path", csv);
            i.putExtra("email", sharedPreferences.getString("email", ""));
            context.startService(i);
        }
    }

    private void RegisterProcess(final String name, final String birthdate, final String sexe, final String height, final String weight, final String email, final String username, final String password) {
        if (name.length() > 3 && !height.isEmpty() && !weight.isEmpty() && !birthdate.isEmpty()) {


            if (facebooklogin) {
                new PostData().execute("http://stepwin.org/stepwin/registerFacebookUsers.php", "Fullname=" + name + "&Email=" + email);
                new PostData().execute("http://stepwin.org/stepwin/registerData.php", "Fullname=" + name + "&Email=" + email + "&Birthdate=" + birthdate + "&sexe=" + sexe + "&height=" + height + "&weight=" + weight);
                editor.putString("user", name);
            } else {
                new PostData().execute("http://stepwin.org/stepwin/registerUsers.php", "Email=" + email + "&Username=" + username + "&Password=" + password);
                new PostData().execute("http://stepwin.org/stepwin/registerData.php", "Fullname=" + name + "&Email=" + email + "&Birthdate=" + birthdate + "&sexe=" + sexe + "&height=" + height + "&weight=" + weight);
                editor.putString("user", username);
            }




            editor.putString("name", name);
            editor.putString("email", email);
            editor.putString("birthdate", birthdate);
            editor.putString("weight", weight);
            editor.putString("height", height);
            editor.putString("sexe", sexe);
            editor.putBoolean("loggedin", true);
            editor.commit();
            AppPreference.setStringPreference(this, Constants.USER_NAME, name);
            AppPreference.setStringPreference(this, Constants.EMAIL, email);
            Intent i = new Intent(SignUpActivityTwo.this, MainActivity.class);
            startActivity(i);
            SignUpActivityTwo.this.finish();

        }
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