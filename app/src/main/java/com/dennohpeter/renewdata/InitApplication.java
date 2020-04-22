package com.dennohpeter.renewdata;

import android.app.Application;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a wrapper class to fetch data from Firebase
 */

public class InitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Fetches data from firebase
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        // Default values;
        Map<String, Object> defaultValues = new HashMap<>();
        defaultValues.put(UpdateHelper.KEY_UPDATE_AVAILABLE, false);
        defaultValues.put(UpdateHelper.KEY_UPDATE_VERSION, Utils.getAppVersion(getApplicationContext()));
        defaultValues.put(UpdateHelper.KEY_UPDATE_URL, getApplicationContext().getString(R.string.app_source));
        remoteConfig.setDefaultsAsync(defaultValues);
        // keep in sync in 2 minutes
        remoteConfig.fetch(5)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        remoteConfig.activate();
                    }
                });

    }
}
