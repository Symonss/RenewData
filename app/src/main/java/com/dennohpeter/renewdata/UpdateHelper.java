package com.dennohpeter.renewdata;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

class UpdateHelper {
    static String KEY_UPDATE_AVAILABLE = "is_update";
    static String KEY_UPDATE_VERSION = "version";
    static String KEY_UPDATE_URL = "update_url";
    private Context context;
    private OnUpdateCheckListener onUpdateCheckListener;

    private UpdateHelper(Context context, OnUpdateCheckListener onUpdateCheckListener) {
        this.context = context;
        this.onUpdateCheckListener = onUpdateCheckListener;
    }

    static Builder with(Context context) {
        return new Builder(context);
    }


    static String getKeyUpdateUrl() {
        return KEY_UPDATE_URL;
    }

    static void setKeyUpdateUrl(String keyUpdateUrl) {
        KEY_UPDATE_URL = keyUpdateUrl;
    }

    private void check() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
        //  This is a helper class for checking app updates
        if (remoteConfig.getBoolean(KEY_UPDATE_AVAILABLE)) {
            String currentVersion = remoteConfig.getString(KEY_UPDATE_VERSION);

            String appVersion = Utils.getAppVersion(context);
            String updateURL = remoteConfig.getString(KEY_UPDATE_URL);
            if (!TextUtils.equals(currentVersion, appVersion) && onUpdateCheckListener != null) {
                onUpdateCheckListener.onUpdateCheckListener(updateURL);
            } else {
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Good job!")
                        .setMessage("You have the latest version!")
                        .create();
                dialog.show();
            }
        }

    }

    public interface OnUpdateCheckListener {
        void onUpdateCheckListener(String url);
    }

    public static class Builder {
        Context context;
        OnUpdateCheckListener onUpdateCheckListener;

        Builder(Context context) {
            this.context = context;
        }

        Builder onUpdateCheck(OnUpdateCheckListener onUpdateCheckListener) {
            this.onUpdateCheckListener = onUpdateCheckListener;
            return this;
        }

        UpdateHelper build() {
            return new UpdateHelper(context, onUpdateCheckListener);
        }

        UpdateHelper check() {

            UpdateHelper updateHelper = build();
            updateHelper.check();
            return updateHelper;
        }

    }

    static class CheckForSDCard {
        //Method to Check If SD Card is mounted or not
        static boolean isSDCardPresent() {
            return Environment.getExternalStorageState().equals(

                    Environment.MEDIA_MOUNTED);
        }
    }
}