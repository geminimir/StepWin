package app.stepwin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SplashScreen extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String username, day, steps = "0", distance, time = "0", calories, email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        String pref = "MyPref";
        sharedPreferences = getSharedPreferences(pref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        //ReadCSVFile(username);
        ActivityCompat.requestPermissions(SplashScreen.this,
                new String[]{/*Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,*/
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE},
                0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case 0:
                if (hasAllPermissionsGranted(grantResults)) {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!sharedPreferences.getBoolean("loggedin", false)) {
                                Intent i = new Intent(SplashScreen.this, LogInActivity.class);
                                startActivity(i);
                            }
                            else {
                                Intent i  = new Intent(SplashScreen.this, MainActivity.class);
                                startActivity(i);
                            }
                            SplashScreen.this.finish();
                        }
                    }, 2800);

                } else {
                    SplashScreen.this.finish();

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
