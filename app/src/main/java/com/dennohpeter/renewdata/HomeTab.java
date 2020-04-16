package com.dennohpeter.renewdata;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Locale;

public class HomeTab extends androidx.fragment.app.Fragment {

    private static final String TAG = "Renew Data";
    private TextView purchased_tm, expiry_tm, tm_left;
    private long time_left = 0;
    private long expiry_time;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private boolean timerCounterRunning;
    private DateUtil dateUtil;
    private BroadcastReceiver smsReceivedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            if (intent.getAction().equals("android.intent.action.SmsReceiver")) {
                String msg_from = intent.getStringExtra("msg_from");
                String message = intent.getStringExtra("msg_body");
                long received_date = intent.getLongExtra("timestampMillis", -1);

                if (msg_from.toLowerCase().contains(getString(R.string.telkom).toLowerCase())) {
                    Log.d(TAG, "from: " + msg_from + ", message: " + message + "timestamp: " + time_left);
                    // Save New Received Date to db
                    Log.d(TAG, "updateTimelineTable DB ");
                    db = databaseHelper.getWritableDatabase();
                    databaseHelper.create_or_update_logs(db, msg_from, message, received_date);
                    db.close();

                    setTimeLineData();
                }

            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_tab, container, false);
        purchased_tm = root.findViewById(R.id.purchased_time);
        expiry_tm = root.findViewById(R.id.expiry_time);
        tm_left = root.findViewById(R.id.time_left);
        Button renew_now = root.findViewById(R.id.renew_now);
        dateUtil = new DateUtil();
        renew_now.setOnClickListener(v -> initRenewProcess());

        databaseHelper = new DatabaseHelper(getContext());
        setTimeLineData();
        return root;
    }

    private void initRenewProcess() {
        StkPush stkPush = new StkPush(getContext());
        stkPush.initiateRenewProcess();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAlarmManager();
    }

    private void setTimeLeft(long expiry_time) {
        time_left = expiry_time - new Date().getTime();
        new CountDownTimer(time_left, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left = millisUntilFinished;
                updateTimeLeftCountDown();
            }

            @Override
            public void onFinish() {
                timerCounterRunning = false;
                tm_left.setText(getString(R.string.expired));
            }
        }.start();

    }

    private void updateTimeLeftCountDown() {
        long days = time_left / (24 * 60 * 60 * 1000);
        long hours = time_left / (60 * 60 * 1000) % 24;
        long minutes = time_left / (60 * 1000) % 60;
        long seconds = time_left / 1000 % 60;
        String formatted_time_left = String.format(Locale.getDefault(), "%02d d, %02d hrs, %02d mins, %02d sec", days, hours, minutes, seconds);
        tm_left.setText(formatted_time_left);
    }

    private void setTimeLineData() {
        Log.d(TAG, "setTimeLineData: ");
        db = databaseHelper.getReadableDatabase();
        Cursor cursor = databaseHelper.log_messages(db);
        while (cursor.moveToNext()) {
            long purchase_time = cursor.getLong(cursor.getColumnIndex("received_date"));
            expiry_time = dateUtil.add24Hours(purchase_time);

            purchased_tm.setText(dateUtil.getFormattedDate(purchase_time).replace("\n", " "));
            expiry_tm.setText(dateUtil.getFormattedDate(expiry_time).replace("\n", " "));
            setTimeLeft(expiry_time);
            Log.d(TAG, "setTimeLineData: in while.");
            if (cursor.isFirst()) {
                break;
            }
        }
        cursor.close();
        db.close();
    }

    private void setAlarmManager() {
        Log.d(TAG, "setAlarmManager: ");
        // time in Milliseconds to the of next reminder.
        long remindBeforeInMins = 1;
        long time_to_ring = expiry_time - dateUtil.toMillis(remindBeforeInMins);

//        long timeMs = time_to_ring - dateUtil.currentDate();
        long timeMs = 60000;
        if (timeMs > 0) {
            Log.d(TAG, "setAlarmManager: " + timeMs);
            Intent intent = new Intent(getContext(), BroadcastManager.class);
            intent.putExtra("remindBeforeInMins", remindBeforeInMins);
            intent.setAction("android.intent.startAlarm");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);

            Log.d(TAG, "setAlarmManager: " + dateUtil.toMinutes(timeMs));
            Log.d(TAG, "setAlarmManager: " + dateUtil.getFormattedDate(timeMs));
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the receiver to save unnecessary system overhead
        // Paused activities cannot receive broadcasts anyway
        try {
            if (smsReceivedListener != null) {
                getContext().unregisterReceiver(smsReceivedListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SmsReceiver");
        getContext().registerReceiver(smsReceivedListener, filter);
    }
}
