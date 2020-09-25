package com.sms.receivingsms;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getExtras();


        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);


                    // Show Alert
//                    int duration = Toast.LENGTH_LONG;
//                    Toast toast = Toast.makeText(context,
//                            "receiveNum: "+ phoneNumber + ", message: " + message, duration);
//                    toast.show();
//                    sendSMS(senderNum, "send message again", context);
                    intent.putExtra("phone_number", senderNum);
                    intent.putExtra("msg_content", message);
                    ObservableObject.getInstance().updateValue(intent);

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }


    }



    public void sendSMS(String phoneNo, String msg, Context context) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(context, "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(context,ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
    public static void enable(Context context){
        ComponentName receiver = new ComponentName(context, SmsReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }
    public static void disable(Context context) {
        ComponentName receiver = new ComponentName(context, SmsReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    public static void resetDefault(Context context) {
        ComponentName component = new ComponentName(context, SmsReceiver.class);
        context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        context.getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
}
