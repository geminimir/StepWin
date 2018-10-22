package app.stepwin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivityOne extends AppCompatActivity {

    private Animation fab_in, fab_out;
    private FloatingActionButton next;
    private EditText username, email, password;
    private TextInputLayout user, ema, pass;
    private String pref = "MyPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up_one);

        next = (FloatingActionButton)findViewById(R.id.next);
        username = (EditText)findViewById(R.id.usernameedit);
        email = (EditText)findViewById(R.id.emailedit);
        password = (EditText)findViewById(R.id.passwordedit);
        user = (TextInputLayout)findViewById(R.id.username);
        ema = (TextInputLayout)findViewById(R.id.Email);
        pass = (TextInputLayout)findViewById(R.id.password);


        /*loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
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
                                            final String mFullname = object.optString("name");
                                            final String email = object.optString("email");
                                            final String sexe = object.optString("gender");
                                            final String birthdate = object.optString("user_birthday");
                                            Log.i("fbinfo", mFbid);
                                            Log.i("fbinfo", mFullname);
                                            Log.i("fbinfo", email);
                                            Log.i("fbinfo", sexe + " ");
                                            Log.i("fbinfo", birthdate + " ");
                                            next.startAnimation(fab_out);
                                            next.setVisibility(View.INVISIBLE);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent i = new Intent(SignUpActivityOne.this, HeightWeightActivity.class);
                                                    i.putExtra("email", email);
                                                    i.putExtra("name", mFullname);
                                                    i.putExtra("sexe", sexe);
                                                    i.putExtra("birthday", birthdate);
                                                    startActivity(i);
                                                    SignUpActivityOne.this.finish();
                                                }
                                            }, 400);

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
                        if(!isNetworkConnected())
                            NetworkAlert();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.i("facebookerror", error.toString());
                    }
                });
*/
        fab_in = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_in);
        fab_out = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_out);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isValidEmail(email.getText().toString()))
                    ema.setError("Email is not valid!");
                else if(email.getText().toString().isEmpty() )
                    ema.setError("Email is required!");
                else
                    ema.setError("");

                if(!isValidUsername(username.getText().toString()))
                    user.setError("Username is not valid!");
                else if (username.getText().toString().isEmpty() )
                    user.setError("Username is required!");
                else
                    user.setError("");

                if(!isValidPassword(password.getText().toString()))
                    pass.setError("Password is too short!");
                else if(password.getText().toString().isEmpty())
                    pass.setError("Password is required!");
                else
                    pass.setError("");

                if(isValidEmail(email.getText().toString()) && isValidUsername(username.getText().toString()) && isValidPassword(password.getText().toString())) {
                    next.startAnimation(fab_out);
                    next.setVisibility(View.INVISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OnReadyLaunchActivity(email.getText().toString(),
                                    username.getText().toString(), password.getText().toString());
                        }
                    }, 400);


                }
            }
        });
    }
    private void OnReadyLaunchActivity(String email, String username, String password) {
        Intent i = new Intent(SignUpActivityOne.this, SignUpActivityTwo.class);
        i.putExtra("email", email);
        i.putExtra("username", username);
        i.putExtra("password", password);
        startActivity(i);
        SignUpActivityOne.this.finish();
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
                SignUpActivityOne.this.finish();
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
    private static boolean isValidEmail(String target) {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    private static boolean isValidUsername(String target) {
        Pattern pattern;
        Matcher matcher;
        final String USERNAME_PATTERN = "^[A-Za-z0-9_-]{3,15}$";

        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(target);

        return matcher.matches() && target.length() > 5;
    }
    private static boolean isValidPassword(String target) {
        return target.length() > 5;
    }
}
