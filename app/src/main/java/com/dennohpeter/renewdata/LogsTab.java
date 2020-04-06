package com.dennohpeter.renewdata;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogsTab extends androidx.fragment.app.Fragment {
    private RecyclerView recyclerView;
    private TextView nothing_to_show;
    private DatabaseHelper databaseHelper;
    private LogsAdapter logsAdapter;
    private static final String TAG = "LogsTab";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
       View root = inflater.inflate(R.layout.logs_tab, container, false);
        recyclerView = root.findViewById(R.id.logs_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        nothing_to_show = root.findViewById(R.id.nothing_to_show);
        logsAdapter =  new LogsAdapter();
        recyclerView.setAdapter(logsAdapter);
        databaseHelper = new DatabaseHelper(getContext());
        Log.d(TAG, "onCreateView: ");
        new populateRecyclerView().execute();
        return root;
    }
    class populateRecyclerView extends AsyncTask<Void, MessageModel, Void>{
        Cursor cursor;
        SQLiteDatabase db;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            db = databaseHelper.getReadableDatabase();
        }

        @Override
        protected Void doInBackground(Void... voids) {
             cursor = databaseHelper.log_messages(db);
            while (cursor.moveToNext()){
                String msg_from = cursor.getString(cursor.getColumnIndex("msg_from"));
                String msg_body = cursor.getString(cursor.getColumnIndex("msg_body"));
                long dateInMilliseconds = cursor.getLong(cursor.getColumnIndex("received_date"));
                String formatted_date = new DateUtil().getFormattedDate(dateInMilliseconds);
                publishProgress( new MessageModel(formatted_date, msg_body, msg_from));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(MessageModel... message) {
            super.onProgressUpdate(message);
            logsAdapter.add(message[0]);
        }

        @Override
        protected void onPostExecute(Void s) {
            cursor.close();
            db.close();
            if (logsAdapter.size()==0){
                recyclerView.setVisibility(View.GONE);
                nothing_to_show.setVisibility(View.VISIBLE);
            }else {
                nothing_to_show.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            Log.d(TAG, "onPostExecute: " + logsAdapter.size());
            recyclerView.setAdapter(logsAdapter);
        }
    }
}
