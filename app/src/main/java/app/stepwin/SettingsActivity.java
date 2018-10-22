package app.stepwin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.preference.CheckBoxPreference;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

/**
 * Created by user on 12/08/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            /*Intent i = new Intent(SettingsPreferencesActivity.this, ChatHeadService.class);
            startService(i);*/
           onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    public static class SettingsFragment extends PreferenceFragment
    {
        SharedPreferences sharedPreferences;
        String pref = "MyPref";
        SharedPreferences.Editor editor;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            sharedPreferences = getActivity().getSharedPreferences(pref, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            PreferenceScreen contact = (PreferenceScreen)findPreference("contact");
            PreferenceScreen personal = (PreferenceScreen)findPreference("personal");
            PreferenceScreen units = (PreferenceScreen)findPreference("units");

            personal.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), PersonalDetailsSettingsActivity.class));
                    return false;
                }
            });

            units.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), UnitsPreferenceActivity.class);
                    startActivity(i);
                    return false;
                }
            });
            contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("plain/text");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact.stepwin@gmail.com"});
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Question");
                    startActivity(Intent.createChooser(sendIntent, ""));

                    return false;
                }
            });
        }
    }
}