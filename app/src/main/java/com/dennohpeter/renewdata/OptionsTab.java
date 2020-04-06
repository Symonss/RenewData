package com.dennohpeter.renewdata;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class OptionsTab extends androidx.fragment.app.Fragment {
    private static final String TAG = "OptionsTab";
    private DatabaseHelper databaseHelper;
    private ProgressDialog progressDialog;
    private SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.options_tab, container, false);
        Button sync_sms = root.findViewById(R.id.sync_sms);
        databaseHelper = new DatabaseHelper(getContext());
        sync_sms.setOnClickListener(v -> new SyncMessages().execute());
        Spinner pick_network = root.findViewById(R.id.pick_network);
        List<String> networks = new ArrayList<>();
        networks.add(getString(R.string.telkom));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, networks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pick_network.setAdapter(adapter);
        pick_network.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                // TODO configure selected network
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button feedback = root.findViewById(R.id.feedback);
        feedback.setOnClickListener(v -> {
            String subject = "Feedback For " + getString(R.string.app_name) + " v" + appVersion(getContext());
            //Add extras and launch intent to send email
            Intent feedbackEmailIntent = new Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", getString(R.string.email), null))
                    .putExtra(Intent.EXTRA_SUBJECT, subject);
            startActivity(Intent.createChooser(feedbackEmailIntent, subject));
        });
        Button about_app = root.findViewById(R.id.about_renewdata);
        about_app.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AboutActivity.class);
            startActivity(intent);
        });
        return root;
    }

    /*
     * Takes Activity Context and returns a String of the App Version e.g 1.0
     */
    private static String appVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;

    }
    class SyncMessages extends AsyncTask<Void, Integer, String> {
        Cursor cursor;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getContext().getString(R.string.sync_sms));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            cursor = getContext().getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            if (cursor !=null){
                 db = databaseHelper.getWritableDatabase();
                Log.d(TAG, "fetch_messages: " + cursor.getCount());
                int count =0;
                while (cursor.moveToNext()){
                    String msg_from = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String msg_body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    long msg_date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    // filtering telkom messages
                    if (msg_from.toLowerCase().contains(getContext().getString(R.string.telkom).toLowerCase())) {
                        // check if message body contains the following keywords
                        // have recharged, have exhausted,have subscribed, balance, awarded, received
                        if (msg_body.contains("recharged") | msg_body.contains("exhausted") |  msg_body.contains("subscribed")| msg_body.contains("balance") | msg_body.contains("awarded") | msg_body.contains("received")){
                            // updating or creating the logs
                            databaseHelper.create_or_update_logs(db, msg_from, msg_body, msg_date);
                        }
                    }
                    // TODO add more sms filters e.g SAF, AIRTEL.

                    publishProgress(count * 100 / cursor.getCount());
                    count ++;
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
            if (result.equals("success")){
                db.close();
                progressDialog.dismiss();
            } else {
                Toast.makeText(getContext(), "No Messages", Toast.LENGTH_LONG).show();
            }
            cursor.close();
        }
    }
}
