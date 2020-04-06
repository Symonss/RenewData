package com.dennohpeter.renewdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "renew_data.db";
    private static final String TIMELINE_QUERY = "CREATE TABLE timeline (purchase_time INTEGER, expiry_time INTEGER);";
    private  static final  String LOGS_QUERY = "CREATE TABLE logs_table (msg_from TEXT, msg_body TEXT, received_date INTEGER);";
    private ContentValues contentValues;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TIMELINE_QUERY);
        db.execSQL(LOGS_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    void create_or_update_timeline(SQLiteDatabase db, long purchase_time, long expiry_time) {
        contentValues = new ContentValues();
        contentValues.put("purchase_time", purchase_time);
        contentValues.put("expiry_time", expiry_time);

        int id = (int) db.insertWithOnConflict("timeline", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1){
            db.update("games_table", contentValues, "purchase_time=?",new String[] {String.valueOf(purchase_time)});
        }
    }
    Cursor get_timeline(SQLiteDatabase db) {
        String[] columns = {"purchase_time", "expiry_time"};
        return db.query("timeline", columns, null, null, null, null, null);
    }

    void create_or_update_logs(SQLiteDatabase db, String msg_from, String msg_body, long received_date) {
        contentValues =  new ContentValues();
        contentValues.put("msg_from", msg_from);
        contentValues.put("msg_body", msg_body);
        contentValues.put("received_date", received_date);
        int id = (int) db.insertWithOnConflict("logs_table", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1){
            db.update("logs_table", contentValues, "received_date=?",new String[] {String.valueOf(received_date)});
        }
    }
     Cursor log_messages(SQLiteDatabase db){
        String[] columns = {"msg_from", "msg_body", "received_date"};
        return db.query("logs_table", columns, null, null, null, null, null);
    }

}
