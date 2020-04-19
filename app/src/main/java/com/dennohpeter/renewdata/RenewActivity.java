package com.dennohpeter.renewdata;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class RenewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renew);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("renewDataFromBroadcast")) {
                int notificationId = intent.getIntExtra("notificationId", 0);
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(notificationId);
                }
            }
        }
    }
}
