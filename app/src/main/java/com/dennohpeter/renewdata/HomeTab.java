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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeTab extends androidx.fragment.app.Fragment {

    private TextView purchased_tm, expiry_tm, tm_left;
    private static final String TAG = "Renew Data";
    private long time_left = 0;
    private long purchase_time, expiry_time;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    private boolean timerCounterRunning;
    private DateUtil dateUtil;

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.home_tab, container, false);

        purchased_tm = root.findViewById(R.id.purchased_time);
        expiry_tm = root.findViewById(R.id.expiry_time);
        tm_left = root.findViewById(R.id.time_left);
        Button renew_now = root.findViewById(R.id.renew_now);
        dateUtil = new DateUtil();
        renew_now.setOnClickListener(v -> Toast.makeText(getContext(), "Coming soon", Toast.LENGTH_SHORT).show());

        databaseHelper = new DatabaseHelper(getContext());
        setTimeLineData();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAlarmManager();
    }

    private BroadcastReceiver smsReceivedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender = intent.getStringExtra("msg_from");
            String message = intent.getStringExtra("msg_body");
            Date purchased_time = new Date(intent.getLongExtra("timestampMillis", -1));
            Log.d(TAG, "onReceive: purchased_time "+ purchased_time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(purchased_time);
            // Add 24 hours to get expiry time
            calendar.add(Calendar.HOUR_OF_DAY, 24);
            Date expiry_time = calendar.getTime();

            setTimeLeft(expiry_time.getTime());
            Toast toast = Toast.makeText(getContext(),  "from: " + sender + " message: " + time_left, Toast.LENGTH_LONG);
            toast.show();
            Log.d(TAG, "from: " + sender + ", message: " + message + "timestamp: " + time_left);
            if (sender.toLowerCase().contains(getString(R.string.telkom).toLowerCase())) {
                Log.d(TAG, "from: " + sender + ", message: " + message + "timestamp: " + time_left);
            }

            String formatted_purchase_time = dateUtil.getFormattedDate(purchased_time).replace("\n", " ");
            String formatted_expiry_time = dateUtil.getFormattedDate(expiry_time).replace("\n", " ");
            purchased_tm.setText(formatted_purchase_time);
            expiry_tm.setText(formatted_expiry_time);

//            Save New Received Date to db
            updateTimelineTable(purchased_time.getTime(), expiry_time.getTime());
        }
    };
    private void setTimeLeft(long expiry_time){
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
    private void updateTimeLeftCountDown(){
        long days  = time_left / (24 * 60 * 60 * 1000);
        long hours =  time_left /(60 * 60 * 1000) % 24;
        long minutes = time_left /(60 * 1000) % 60;
        long seconds =  time_left /1000 % 60;
        String formatted_time_left = String.format(Locale.getDefault(),"%02d d, %02d hrs, %02d mins, %02d sec", days, hours, minutes,seconds);
        tm_left.setText(formatted_time_left);
    }
    private void updateTimelineTable(long purchased_time, long expiry_time){
        Log.d(TAG, "updateTimelineTable: ");
        db = databaseHelper.getWritableDatabase();
        databaseHelper.create_or_update_timeline(db, purchased_time, expiry_time);
        db.close();
    }
    private void setTimeLineData(){
        Log.d(TAG, "setTimeLineData: ");
        db = databaseHelper.getReadableDatabase();
        Cursor cursor = databaseHelper.get_timeline(db);
        while (cursor.moveToNext()){
            purchase_time = cursor.getLong(cursor.getColumnIndex("purchase_time"));
            expiry_time = cursor.getLong(cursor.getColumnIndex("expiry_time"));

            purchased_tm.setText(dateUtil.getFormattedDate(purchase_time).replace("\n", " "));
            expiry_tm.setText(dateUtil.getFormattedDate(expiry_time).replace("\n", " "));
            setTimeLeft(expiry_time);
        }
        cursor.close();
        db.close();
    }
    private void setAlarmManager(){
        Log.d(TAG, "setAlarmManager: ");
        // time in Milliseconds to the of next reminder.
        long timeMs = 5000;
        Intent intent = new Intent(getContext(), ReminderHandler.class);
        intent.putExtra("time_left1 ", timeMs);
        PendingIntent pendingIntent =  PendingIntent.getBroadcast(getContext(),0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);

        Log.d(TAG, "setAlarmManager: "+ dateUtil.getFormattedDate(timeMs));
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMs, pendingIntent);

    }

    @Override
    public void onPause() {
        super.onPause();
          // Unregister the receiver to save unnecessary system overhead
        // Paused activities cannot receive broadcasts anyway
        try {
            if(smsReceivedListener != null){
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
        getContext().registerReceiver(smsReceivedListener , filter);
    }
}
