package com.dennohpeter.renewdata;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BroadcastManager extends BroadcastReceiver {
    private static final String TAG = "BroadcastManager";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: " + intent.getAction());
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
                    // retrieve the SMS message received and pass it to the
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        String msg_from = smsMessage.getOriginatingAddress();
                        String msg_body = smsMessage.getMessageBody();
                        long timestampMillis = smsMessage.getTimestampMillis();
                        Intent i = new Intent("android.intent.action.SmsReceiver");
                        i.putExtra("msg_from", msg_from);
                        i.putExtra("msg_body", msg_body);
                        i.putExtra("timestampMillis", timestampMillis);
                        context.sendBroadcast(i);
                    }
                    break;
                case "android.intent.startAlarm":
                    long remindBeforeInMins = intent.getLongExtra("remindBeforeInMins", 0);
                    Intent homeTabIntent = new Intent(context, MainActivity.class);
                    PendingIntent HomeTabPendingIntent = PendingIntent.getActivity(context, 0, homeTabIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent renewNoIntent = new Intent(context, RenewActivity.class);
                    PendingIntent renewNoPendingIntent = PendingIntent.getActivity(context, 0, renewNoIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent dismissIntent = new Intent(context, BroadcastManager.class);
                    dismissIntent.setAction("snoozeReminder");
                    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, 0);
                    int color;
                    if (remindBeforeInMins < 10) {
                        color = Color.RED;
                    } else {
                        color = Color.GREEN;
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "renew_reminder")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Renew Data Reminder")
                            .setContentText(remindBeforeInMins + " minutes left before data expires, Renew now.")
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setColor(color)
                            .setContentIntent(HomeTabPendingIntent)
                            .setAutoCancel(true)
                            .addAction(R.drawable.ic_snooze, "Remind Later", dismissPendingIntent)
                            .addAction(R.drawable.ic_touch, "Renew Now", renewNoPendingIntent);
                    NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
                    break;
                case "snoozeReminder":
                    Toast.makeText(context, "Snooze Reminder", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
