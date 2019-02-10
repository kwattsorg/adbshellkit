package net.kwatts.android.droidcommandpro;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import timber.log.Timber;
/**
 * Created by kwatts on 7/14/17.
 */

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {


    public static final String KEY_PREF_TEXTSIZE = "textSize";
    public static final String KEY_PREF_RUNASSUPERUSER = "runAsSuperUser";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_TEXTSIZE)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            Timber.d("Setting text size: " + key);
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }

    }
}
