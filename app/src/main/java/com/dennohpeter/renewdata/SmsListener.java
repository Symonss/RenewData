package com.dennohpeter.renewdata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            // Retrieves a map of extended data from the intent.
            Bundle bundle  = intent.getExtras();
            // get the SMS message passed in
            SmsMessage[] msgs = null;
            String msg_from;
            String msg_body;
            if (bundle != null){
                // retrieve the SMS message received
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        msg_body = msgs[i].getMessageBody();
                    }
                } catch (Exception e){
                    Log.d("Exception caught", e.getMessage());

                }
            }
        }
    }
}
