package com.dennohpeter.renewdata;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class OptionsTab extends PreferenceFragmentCompat {
    private static final String TAG = "OptionsTab";


    /*
     * Takes Activity Context and returns a String of the App Version e.g 1.0
     */
    private static String appVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        // 24 hour clock handler
        CheckBoxPreference twenty24_hours_checkbox = findPreference("twenty4_hour_clock");
        if (twenty24_hours_checkbox != null) {
            twenty24_hours_checkbox.setOnPreferenceClickListener(preference -> {
                if (twenty24_hours_checkbox.isChecked()) {
                    twenty24_hours_checkbox.setSummary(R.string.twelve_hour_fmt);
                } else {
                    twenty24_hours_checkbox.setSummary(R.string.twenty_four_hour_fmt);
                }
                return true;
            });
        }
        // Share app
        Preference shareApp = findPreference("shareApp");
        shareApp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "Check Out " + getString(R.string.app_name) + ", a simple and efficient reminder app to help you manage mobile data efficiently.");
                intent.putExtra(Intent.EXTRA_TEXT, "Get " + getString(R.string.app_name) + ": " + getString(R.string.app_source));
                intent.setType("text/plain");

                startActivity(Intent.createChooser(intent, "Share via"));
                return true;
            }
        });
        // about app handler
        Preference about = findPreference("about");
        if (about != null) {
            about.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }
}
