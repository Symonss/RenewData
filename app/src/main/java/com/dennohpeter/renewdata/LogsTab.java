package com.dennohpeter.renewdata;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

public class LogsTab extends androidx.fragment.app.Fragment {
    private static final String TAG = "LogsTab";
    private static final int SMS_PERMISSION_CODE = 12;
    private RecyclerView recyclerView;
    private TextView nothing_to_show;
    private DatabaseHelper databaseHelper;
    private LogsAdapter logsAdapter;
    private String format_style;
    private boolean in24hrsFormat;
    private SwipeRefreshLayout pullToRefresh;
    private ProgressDialog progressDialog;
    private SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.logs_tab, container, false);
        // Bind views
        recyclerView = root.findViewById(R.id.logs_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        nothing_to_show = root.findViewById(R.id.nothing_to_show);
        pullToRefresh = root.findViewById(R.id.logsContainer);
        // get preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        in24hrsFormat = preferences.getBoolean("twenty4_hour_clock", false);
        format_style = preferences.getString("date_format", getString(R.string.default_date_format));

        logsAdapter = new LogsAdapter();
        recyclerView.setAdapter(logsAdapter);
        databaseHelper = new DatabaseHelper(getContext());
        new populateRecyclerView().execute();
        setSwipeRefreshView();
        return root;
    }

    private void setSwipeRefreshView() {
        // the refreshing colors
        pullToRefresh.setColorSchemeColors(getResources().
                        getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light)
                , getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

        pullToRefresh.setOnRefreshListener(this::refreshMessages);
    }

    private void refreshMessages() {
        if (isSmsPermissionGranted(getActivity())) {
            new SyncMessages().execute();
        } else {
            // request permission
            requestReadSmsPermission(getActivity());
        }
    }

    /*
     * Check if we have sms permission
     */
    private boolean isSmsPermissionGranted(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /*
     * Request runtime SMS Permission
     */
    private void requestReadSmsPermission(Activity activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_SMS)) {
            // None blocking explanation
            showRequestPermissionsInfoAlertDialog(getContext(), activity);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
            // disable loading
            pullToRefresh.setRefreshing(false);
        }
    }

    private void showRequestPermissionsInfoAlertDialog(Context context, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("To sync with messages, allow " + context.getString(R.string.app_name) + " to access sms.");

        builder.setPositiveButton(R.string.action_ok, (dialog, which) -> {
            //  request runtime permission
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
            // disable loading
            pullToRefresh.setRefreshing(false);
        }).setNegativeButton("Not Now", (dialog, which) -> {
            dialog.dismiss();
            pullToRefresh.setRefreshing(false);
            Toast.makeText(context, "Syncing cancelled..", Toast.LENGTH_SHORT).show();
        });
        builder.setCancelable(false);
        builder.show();


    }

    /*
     * Displays an Alert Dialog explaining to the user what SMS permission for.
     *
     * @param makeSystemRequest if set to true the system permission will be granted when it's dismissed
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new SyncMessages().execute();

            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Can't sync messages. Read SMS permission is required");
                builder.setCancelable(true);
                builder.show();
            }
        }
    }

    class populateRecyclerView extends AsyncTask<Void, MessageModel, Void> {
        Cursor cursor;
        SQLiteDatabase db;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            db = databaseHelper.getReadableDatabase();
            cursor = databaseHelper.getLogMessages(db);
            while (cursor.moveToNext()) {
                String msg_from = cursor.getString(cursor.getColumnIndex("msg_from"));
                String msg_body = cursor.getString(cursor.getColumnIndex("msg_body"));
                long dateInMilliseconds = cursor.getLong(cursor.getColumnIndex("received_date"));
                String formatted_date = new Utils().formatDate(dateInMilliseconds, format_style, in24hrsFormat, "\n");
                publishProgress(new MessageModel(formatted_date, msg_body, msg_from));
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
            if (logsAdapter.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                nothing_to_show.setVisibility(View.VISIBLE);
            } else {
                nothing_to_show.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Done updating logs.", Toast.LENGTH_SHORT).show();
                }
            }
            recyclerView.setAdapter(logsAdapter);
        }
    }

    class SyncMessages extends AsyncTask<Void, Integer, String> {
        Cursor cursor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.syncing_with_messages));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            cursor = getContext().getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                db = databaseHelper.getWritableDatabase();
                int count = 0;
                while (cursor.moveToNext()) {
                    String msg_from = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String msg_body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    long msg_date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    // filtering telkom messages
                    if (msg_from.toLowerCase().contains(getContext().getString(R.string.telkom).toLowerCase())) {
                        // check if message body contains the following keywords
                        // have recharged, have exhausted,have subscribed, balance, awarded, received
                        if (msg_body.contains("recharged") | msg_body.contains("exhausted") | msg_body.contains("subscribed") | msg_body.contains("balance") | msg_body.contains("awarded") | msg_body.contains("received")) {
                            // updating or creating the logs
                            databaseHelper.create_or_update_logs(msg_from, msg_body, msg_date);
                        }
                    }
                    // TODO add more sms filters e.g SAF, AIRTEL.

                    publishProgress(count * 100 / cursor.getCount());
                    count++;
                }
                return "success";
            }
            return "failed";

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {
                db.close();
                progressDialog.dismiss();
                // after successful fetch populate recycler viewer
                new populateRecyclerView().execute();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "No Messages", Toast.LENGTH_LONG).show();
                }
            }
            cursor.close();
            // Stop refreshing
            pullToRefresh.setRefreshing(false);
        }
    }

}
