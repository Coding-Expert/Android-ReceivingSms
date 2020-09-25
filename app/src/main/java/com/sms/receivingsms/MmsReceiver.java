package com.sms.receivingsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class MmsReceiver extends BroadcastReceiver {

    private final String DEBUG_TAG = getClass().getSimpleName().toString();
    private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_DELIVER";
    private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

    public MmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        android.util.Log.d("SmsKitKat", "MmsReceiver onReceive – " + intent.toString());
        String action = intent.getAction();
        String type = intent.getType();
        String msg = "incoming mms";



        if (action.equals(ACTION_MMS_RECEIVED) && type.equals(MMS_DATA_TYPE)) {


            Bundle bundle = intent.getExtras();

            Log.d(DEBUG_TAG, "bundle " + bundle);
            SmsMessage[] msgs = null;
            String str = "";
            int contactId = -1;
            String address;

            if (bundle != null) {

                byte[] buffer = bundle.getByteArray("data");
                Log.d(DEBUG_TAG, "buffer " + buffer);
                String incomingNumber = new String(buffer);
                String sender_address = getPhoneNumber(incomingNumber, context);
                intent.putExtra("phone_number", sender_address);
                ObservableObject.getInstance().updateValue(intent);
//                Toast.makeText(context, incomingNumber, incomingNumber.length()).show();
//                Log.d(DEBUG_TAG, "Mobile Number: " + incomingNumber);
//                int indx = incomingNumber.indexOf("/TYPE");
//                if (indx > 0 && (indx - 15) > 0) {
//                    int newIndx = indx - 15;
//                    incomingNumber = incomingNumber.substring(newIndx, indx);
//                    indx = incomingNumber.indexOf("+");
//                    if (indx > 0) {
//                        incomingNumber = incomingNumber.substring(indx);
//                        Log.d(DEBUG_TAG, "Mobile Number: " + incomingNumber);
////                        Toast.makeText(context, incomingNumber, incomingNumber.length()).show();
//                    }
//                }

//                int transactionId = bundle.getInt("transactionId");
//                Log.d(DEBUG_TAG, "transactionId " + transactionId);
//
//                int pduType = bundle.getInt("pduType");
//                Log.d(DEBUG_TAG, "pduType " + pduType);
//
//                byte[] buffer2 = bundle.getByteArray("header");
//                String header = new String(buffer2);
//                Log.d(DEBUG_TAG, "header " + header);
//
//                if (contactId != -1) {
//                    showNotification(contactId, str);
//                }

                // ---send a broadcast intent to update the MMS received in the
                // activity---
//                Intent broadcastIntent = new Intent();
//                broadcastIntent.setAction("MMS_RECEIVED_ACTION");
//                broadcastIntent.putExtra("mms", str);
//                context.sendBroadcast(broadcastIntent);


            }
        }


    }

    protected void showNotification( int contactId, String message){
        //Display notification...
    }

    public String getPhoneNumber(String data, Context context) {
        String number = "";
        String regexStr = "^[0-9]*$";
        int first_index = data.indexOf("/TYPE");

        int str_index = 0;
        for(int i = first_index; i > 0; i--) {
            if(data.substring(i-1, i).trim().matches(regexStr) || data.substring(i-1, i).equals("+")){
                str_index = i;
            }
            else{
                break;
            }
        }
        String str = data.substring(str_index - 1, first_index);
        number = str;
//        Toast.makeText(context, str, str.length()).show();
//        Log.d(DEBUG_TAG, "Mobile Number: " + str);
        return number;
    }
}
