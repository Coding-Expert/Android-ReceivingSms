package com.sms.receivingsms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jaredrummler.android.device.DeviceName;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;

public class ReceivingSmsActivity extends AppCompatActivity implements
        Observer {

    private static final String DEVICE_DEFAULT_SMS_PACKAGE_KEY = "com.example.smsexample.deviceDefaultSmsPackage";
    private static final String INVALID_PACKAGE = "invalid_package";

    private TelephonyManager tMgr;
    private String device_phone_number = "";
    private String device_imei = "";
    private String device_model = "";
    private String device_name = "";

    private static final int READ_SMS_REQUEST_CODE = 102;
    private static final int READ_PHONE_NUMBERS_REQUEST_CODE = 103;
    private static final int READ_PHONE_STATE_REQUEST_CODE = 104;
    private static final int SMS_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;

    private TextView status_textView;
    private boolean online_flag =false;
    private String msg_content ="";
    private String date = "";

    private Handler mHandler;
    public int mInterval = 10000;
    SharedPreferences permissionStatus;
    private boolean sentToSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiving_sms);

        status_textView = (TextView) findViewById(R.id.status_textview);
        mHandler = new Handler();
        ObservableObject.getInstance().addObserver(this);
        setUpViews();
        saveDeviceDefaultSmsPackage();
        device_phone_number = getIntent().getStringExtra("phone_number");
        startCheckOnline();

    }
    Runnable checkOnline_runnable = new Runnable(){
      @Override
      public void run(){
          try{
              checkOnlineDevice();
          }
          finally {
              mHandler.postDelayed(checkOnline_runnable, mInterval);
          }
      }
    };
    public void startCheckOnline(){
        checkOnline_runnable.run();
//        mHandler.postDelayed(checkOnline_runnable, mInterval);
    }

    @Override
    public void update(Observable observable, Object arg) {
        Intent intent = (Intent) arg;
        String phoneNo = intent.getStringExtra("phone_number");
        msg_content = intent.getStringExtra("msg_content");
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        int currentYear = cal.get(Calendar.YEAR);
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int currentMonth = cal.get(Calendar.MONTH);
        String time = String.valueOf(cal.get(Calendar.HOUR)) + ":" + String.valueOf(cal.get(Calendar.MINUTE)) + ":" + String.valueOf(cal.get(Calendar.SECOND));
        date = String.valueOf(currentYear) + "-" + String.valueOf(currentMonth) + "-" + String.valueOf(currentDay) + " " + time;

        if(online_flag){
            ContentValues values = new ContentValues();         // save incoming sms to inbox of phone
            values.put("address", phoneNo );
            values.put("body", msg_content);
            getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
            sendMessageToServer();
        }
        else{
            Toast.makeText(getApplicationContext(), "Server is offline", Toast.LENGTH_SHORT).show();
        }

    }

    private void setUpViews() {
//        findViewById(R.id.set_as_default).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setDeviceDefaultSmsPackage(getPackageName(), 1);
//            }
//        });

        findViewById(R.id.restore_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDeviceDefaultSmsPackage(getPreviousSmsDefaultPackage(), 2);
            }
        });
    }
    private void saveDeviceDefaultSmsPackage() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (hasNoPreviousSmsDefaultPackage(preferences)) {
            String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
            preferences.edit().putString(DEVICE_DEFAULT_SMS_PACKAGE_KEY, defaultSmsPackage).apply();
        }
    }
    private void setDeviceDefaultSmsPackage(String packageName, int status) {

        if (Telephony.Sms.getDefaultSmsPackage(getApplicationContext()) != null && Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
            RoleManager roleManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                roleManager = getApplicationContext().getSystemService(RoleManager.class);

                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                        Log.d("role", "role");

                        openSMSappChooser(this);
                    }
                }
            } else {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        packageName);
                startActivityForResult(intent, status);
            }
        }

    }
    public void openSMSappChooser(Context context) {
//        PackageManager packageManager = context.getPackageManager();
//        ComponentName componentName = new ComponentName(context, ReceivingSmsActivity.class);
//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_APP_MESSAGING);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(selector, 2);

//        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
    }

    private String getPreviousSmsDefaultPackage() {
        return getPreferences(MODE_PRIVATE).getString(DEVICE_DEFAULT_SMS_PACKAGE_KEY, INVALID_PACKAGE);
    }
    private boolean hasNoPreviousSmsDefaultPackage(SharedPreferences preferences) {
        return !preferences.contains(DEVICE_DEFAULT_SMS_PACKAGE_KEY);
    }

    public void checkOnlineDevice(){
        RequestParams rp = new RequestParams();
        rp.add("deviceid", device_imei);
        rp.add("phoneno", device_phone_number);
        rp.add("signaltype", "checkSignal");
        rp.add("deviceip", Utils.getIPAddress(true));
        String url = "/checkonline.php";
        HttpUtils.post(url, rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    String status = response.getString("status");
                    if(status.equals("true")){
                        online_flag = true;
                        status_textView.setText("ONLINE");
                        status_textView.setTextColor(Color.GREEN);
                    }
                    else{
                        online_flag = false;
                        status_textView.setText("OFFLINE");
                        status_textView.setTextColor(Color.RED);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                online_flag = false;
                status_textView.setText("OFFLINE");
                status_textView.setTextColor(Color.RED);
            }
        });
    }

    public void sendMessageToServer(){
        RequestParams rp = new RequestParams();
        rp.add("deviceid", device_imei);
        rp.add("phoneno", device_phone_number);
        rp.add("signaltype", "smscontent");
        rp.add("msgcontent", msg_content);
        rp.add("datetimereceived", date);
        rp.add("deviceip", Utils.getIPAddress(true));
        String url = "/sendcontent.php";
        Toast.makeText(getApplicationContext(), msg_content, msg_content.length()).show();
        HttpUtils.post(url, rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    String status = response.getString("status");
                    if(status.equals("true")){
                        Toast.makeText(getApplicationContext(), "Message sending Success!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Message Sending failed", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                Toast.makeText(getApplicationContext(), "Message Sending failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2){
            finish();
        }
    }
    public void settingSystemSmsApp(){

        if (Telephony.Sms.getDefaultSmsPackage(getApplicationContext()) != null && Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
            RoleManager roleManager = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                roleManager = getApplicationContext().getSystemService(RoleManager.class);
                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                        Log.d("role", "role");
                    }

                }
            }
            else {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        getApplicationContext().getPackageName());
                startActivityForResult(intent, 0);
            }
        }
        else{
            showConfirmCloseApp();
        }
    }

    private void showConfirmCloseApp() {

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Do you want to close App")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SmsReceiver.disable(getApplicationContext());
//                        finish();
                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                })
                .show();
    }
    @Override
    public void onResume() {
        super.onResume();
        settingSmsApp();
    }

    public void settingSmsApp(){
        if (Telephony.Sms.getDefaultSmsPackage(getApplicationContext()) != null && !Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getApplicationContext().getPackageName())) {
            RoleManager roleManager = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                roleManager = getApplicationContext().getSystemService(RoleManager.class);
                if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                        Log.d("role", "role");
                    } else {
                        SmsReceiver.enable(this);
                        Intent roleRequestIntent = roleManager.createRequestRoleIntent(
                                RoleManager.ROLE_SMS);
                        startActivityForResult(roleRequestIntent, 0);
                    }
                }
            }
            else {
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        getApplicationContext().getPackageName());
                startActivityForResult(intent, 0);
            }
        }
    }

}
