package com.dennohpeter.renewdata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            // retrieve the SMS message received and pass it to the
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                String msg_from = smsMessage.getOriginatingAddress();
                String msg_body = smsMessage.getMessageBody();
                long timestampMillis = smsMessage.getTimestampMillis();
                Intent i = new Intent("android.intent.action.SmsReceiver").putExtra("msg_from", msg_from);
                i.putExtra("msg_body", msg_body);
                i.putExtra("timestampMillis", timestampMillis);
                context.sendBroadcast(i);
            }
        }
    }
}
