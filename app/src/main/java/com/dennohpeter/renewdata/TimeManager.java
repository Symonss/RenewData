package com.dennohpeter.renewdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class TimeManager {
    private DateUtil dateUtil;
    private long purchase_time;
    private long expiry_time;
    private DatabaseHelper databaseHelper;

    TimeManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
        dateUtil = new DateUtil();

        // Setting Expiry and Purchase date from messages
        setFieldMembers(context);
    }

    long getPurchase_time() {
        return purchase_time;
    }

    long getExpiry_time() {
        return expiry_time;
    }

    long getTimeLeftInMillis() {
        long currentTime = dateUtil.currentDate();
        return expiry_time - currentTime;
    }

    boolean isExpired() {
        return getTimeLeftInMillis() < 0;
    }

    private void setFieldMembers(Context context) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = databaseHelper.getLogMessages(db);
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("msg_body")).contains(context.getString(R.string.subscribed))) {
                this.purchase_time = cursor.getLong(cursor.getColumnIndex("received_date"));
                this.expiry_time = dateUtil.add24Hours(purchase_time);
                break;
            }
        }
        cursor.close();
        db.close();
    }
}
