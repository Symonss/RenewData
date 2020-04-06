package com.dennohpeter.renewdata;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderHandler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long time_left = intent.getExtras().getLong("time_left");
        Log.d("ddef", "onReceive: "+ intent.getExtras());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Renew Data Reminder")
                .setContentText(time_left + " minutes left before data expires, Renew now.");

        Intent intentToFire = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentToFire, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
        Log.d("Receive ", "" +time_left);
    }
}
