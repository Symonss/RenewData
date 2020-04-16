package com.dennohpeter.renewdata;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StkPush {
    private static final String TAG = "StkPush";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String consumer_key = "om4hPSFFPzlAABEpUiKoT1514gIVwKIC";
    private String consumer_secret = "osdMIvIcsSbtWv4T";
    private String key_secret = consumer_key + ":" + consumer_secret;
    private OkHttpClient client;
    private String paybill_no = "174379";
    private String timestamp;
    private Context context;
    private String to_number = "254715605476";
    private Activity activity;

    StkPush(Context context) {
        client = new OkHttpClient();
        timestamp = new DateUtil().timestamp();
        this.context = context;
        this.activity = (Activity) context;
    }

    private String base64EncodedKey() {
        byte[] bytes = key_secret.getBytes(StandardCharsets.ISO_8859_1);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public void initiateRenewProcess() {
        String generate_endpoint = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";
        Request request = new Request.Builder()
                .url(generate_endpoint)
                .get()
                .addHeader("authorization", "Basic " + base64EncodedKey())
                .addHeader("cache-control", "no-cache")
                .build();
        Log.d(TAG, "getting access token...");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Something went wrong
                Toast.makeText(context, "An Error Occurred. Try again.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());

                        String access_token = jsonObject.getString("access_token");
                        Log.d(TAG, "Access Token Received.");
                        Log.d(TAG, "Initiating payment...");
                        // make pay method to run on main thread
                        pay("1", to_number, access_token);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Request was not successful. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pay(String amount, String to_number, String accessToken) {
        String stk_push_endpoint = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
        String callback_endpoint = "https://dd84d25f.ngrok.io/api/renew_data_callback/";

        JSONObject json = new JSONObject();
        try {
            json.put("BusinessShortCode", paybill_no);
            json.put("Password", password());
            json.put("Timestamp", timestamp);
            json.put("TransactionType", "CustomerPayBillOnline");
            json.put("Amount", amount);
            json.put("PartyA", to_number);
            json.put("PartyB", paybill_no);
            json.put("PhoneNumber", to_number);
            json.put("CallBackURL", callback_endpoint);
            json.put("AccountReference", to_number);
            json.put("TransactionDesc", "Data Renewal");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(stk_push_endpoint)
                .post(body)
                .addHeader("authorization", "Bearer " + accessToken)
                .addHeader("content-type", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // an error occurred while doing payment
                activity.runOnUiThread(() -> Toast.makeText(context, "An Error Occurred While Doing Payment. Please Try Again.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    try {
                        Log.d(TAG, "\nPayment request Response " + response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.d(TAG, "onResponse: " + response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    activity.runOnUiThread(() -> Toast.makeText(context, "Payment request not successful", Toast.LENGTH_LONG).show());
                }
            }
        });

    }

    private String password() {
        String LNP_passKey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
        String pass = paybill_no + LNP_passKey + timestamp;
        byte[] bytes = pass.getBytes(StandardCharsets.ISO_8859_1);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
