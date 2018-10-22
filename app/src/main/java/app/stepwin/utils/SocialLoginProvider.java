package app.stepwin.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import app.stepwin.LogInActivity;
import app.stepwin.R;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pc6 on 4/11/2017.
 */

public abstract class SocialLoginProvider extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final int USER_ACCESS_TOKEN = 0;
    public static final int USER_NAME = 1;
    public static final int USER_EMAIL = 2;
    public static final int USER_PROFILE_PIC = 3;
    public static final int FB_USER_ID = 4;
    public static final int FB_GENDER = 5;
    public static final int FB_BIRTH_DAY = 6;

    public static final String TAG = "SocialLoginProvider";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager fbCallbackManager;

    public abstract void hideProgress();

    public abstract void googleSingInResult(boolean status, String[] person);

    public abstract void facebookSingInResult(boolean status, String[] user);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFb();
        buildNewGoogleApiClient();
    }

    private void buildNewGoogleApiClient() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //  .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            *//*  If the user's cached credentials are valid, the OptionalPendingResult will be "done"
             and the GoogleSignInResult will be available instantly.*//*
            Log.e(TAG, "onStart  :::::  Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgress();
            Log.e(TAG, "onStart  :::: Dint get cache sign-in");
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgress();
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        */
    }

    /**
     * START signIn
     */
    public void signIn() {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            //   If the user 's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.e(TAG, "signIn  :::::  Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    /**
     * START signOut
     */
    public void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false, null);
                    }
                });
    }

    /**
     * START revokeAccess // call for disconnect..
     */
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // START_EXCLUDE
                        updateUI(false, null);
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        hideProgress();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            super.onActivityResult(requestCode, responseCode, intent);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            handleSignInResult(result);
        } else {
            fbCallbackManager.onActivityResult(requestCode, responseCode, intent);
        }
    }


    /**
     * START handleSignInResult
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.e(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            updateUI(true, acct);
        } else {
            updateUI(false, null);
        }
    }


    private void updateUI(boolean signedIn, GoogleSignInAccount acct) {
        hideProgress();
        if (signedIn) {
            String idToken = acct.getIdToken();
            String name = acct.getDisplayName();
            String email = acct.getEmail();
            String pic = acct.getPhotoUrl().toString();
            googleSingInResult(true, new String[]{idToken, name, email, pic});
        } else {
            googleSingInResult(false, null);
        }
    }

    /**
     * FACEBOOK LOGIN SETUP
     */

    private void setupFb() {
        FacebookSdk.sdkInitialize(this);
        fbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                makeGraphRequest(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("Fb Access Token: ", error.getMessage());
                // Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeGraphRequest(final AccessToken getAccessToken) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(getAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {
                    Log.e("getFacebookProfile", "JSONObject: " + object.toString());
                    try {
                        String name = object.getString("name");
                        String id = "";
                        String userPic = "";
                        String email = "";
                        String gender = "";
                        String user_birthday = "";

                        if (object.has("id")) {
                            id = object.getString("id");
                        }
                        if (object.has("picture")) {
                            userPic = object.getJSONObject("picture").getJSONObject("data").getString("url");
                        }
                        if (object.has("email")) {
                            email = object.getString("email");
                        }
                        if (object.has("gender")) {
                            gender = object.getString("gender");
                        }
                        if (object.has("birthday")) {
                            user_birthday = object.getString("birthday");
                        }
                        facebookSingInResult(true, new String[]{getAccessToken.getToken(), name, email, userPic, id, gender, user_birthday});
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    facebookSingInResult(false, null);
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,birthday,email,gender");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    public static void facebookLogout(Context context) {
        FacebookSdk.sdkInitialize(context);
        LoginManager.getInstance().logOut();
    }
}
