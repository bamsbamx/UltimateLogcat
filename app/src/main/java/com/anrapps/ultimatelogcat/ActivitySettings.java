package com.anrapps.ultimatelogcat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.anrapps.ultimatelogcat.util.PrefUtils;

public class ActivitySettings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(R.string.label_settings);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    navigateUpToFromChild(ActivitySettings.this,
                            IntentCompat.makeMainActivity(new ComponentName(ActivitySettings.this,
                                    ActivityMain.class)));
                } else {
                    Intent intent = new Intent(ActivitySettings.this, ActivityMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }

    public static void start(Activity from, boolean finish) {
        from.startActivity(new Intent(from, ActivitySettings.class));
        if (finish) from.finish();
    }
    public static class SettingsFragment extends PreferenceFragment {

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_LOG_FORMAT));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_LOG_BUFFER));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_TEXT_SIZE));
            bindPreferenceSummaryToValue(findPreference(PrefUtils.PREFERENCE_KEY_TEXT_FONT));
        }

        private static void bindPreferenceSummaryToValue(Preference preference) {
            if (preference == null) return; //The user is lower API level
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PrefUtils.sp(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(value.toString());

                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                } else {
                    preference.setSummary(value.toString());
                }
                return true;
            }
        };



    }
}
