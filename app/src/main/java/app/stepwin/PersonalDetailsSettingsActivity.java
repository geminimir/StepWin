package app.stepwin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class PersonalDetailsSettingsActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    String MyPREFERENCES = "MyPref";
    static String name, se, wei, hei;
    static SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PersonalDetailsSettingsFramgent()).commit();

        sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        name = sharedPreferences.getString("name", "");
        wei = sharedPreferences.getString("weight", "");
        hei = sharedPreferences.getString("height",  "");
        se = sharedPreferences.getString("sexe", "");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent i = new Intent(PersonalDetailsSettingsActivity.this, InfoChangeService.class);
        i.putExtra("name", sharedPreferences.getString("name", ""));
        i.putExtra("sexe", sharedPreferences.getString("sexe", ""));
        i.putExtra("weight", sharedPreferences.getString("weight", ""));
        i.putExtra("height", sharedPreferences.getString("height", ""));
        startService(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent i = new Intent(PersonalDetailsSettingsActivity.this, InfoChangeService.class);
        i.putExtra("name", sharedPreferences.getString("name", ""));
        i.putExtra("sexe", sharedPreferences.getString("sexe", ""));
        i.putExtra("weight", sharedPreferences.getString("weight", ""));
        i.putExtra("height", sharedPreferences.getString("height", ""));
        startService(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    public static class PersonalDetailsSettingsFramgent extends PreferenceFragment     {
        EditTextPreference fullname, height, weight;
        ListPreference sexe;
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.personal_preferences);


            fullname = (EditTextPreference) findPreference("name");
            height = (EditTextPreference) findPreference("height");
            weight = (EditTextPreference) findPreference("weight");
            sexe = (ListPreference)findPreference("sexe");
            fullname.setSummary(name);
            height.setSummary(hei);
            weight.setSummary(wei);
            sexe.setValue(se);
            fullname.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    fullname.setSummary(newValue.toString());
                    editor.putString("name", newValue.toString());
                    editor.commit();
                    return false;
                }
            });
            height.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    height.setSummary(newValue.toString());
                    editor.putString("height", newValue.toString());
                    editor.commit();
                    return false;
                }
            });
            weight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    weight.setSummary(newValue.toString());
                    editor.putString("weight", newValue.toString());
                    editor.commit();
                    return false;
                }
            });

            sexe.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    sexe.setValue(newValue.toString());
                    editor.putString("sexe", newValue.toString());
                    editor.commit();
                    return false;
                }
            });

        }
    }
}
