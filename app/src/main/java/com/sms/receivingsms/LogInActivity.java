package com.sms.receivingsms;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.jaredrummler.android.device.DeviceName;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LogInActivity extends AppCompatActivity {

    private Button login_button;
    private TextView phone_text;

    private TelephonyManager tMgr;
    private String device_phone_number = "";
    private String device_imei = "";
    private String device_model = "";
    private String device_name = "";

    private static final int READ_SMS_REQUEST_CODE = 102;
    private static final int READ_PHONE_NUMBERS_REQUEST_CODE = 103;
    private static final int READ_PHONE_STATE_REQUEST_CODE = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        login_button = (Button)findViewById(R.id.login_button);
        login_button.setOnClickListener(login_listener);
        phone_text = (TextView)findViewById(R.id.login_phone_textview);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    device_phone_number = tMgr.getLine1Number();
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        device_imei = Settings.Secure.getString(
                                getApplicationContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                    } else {
                        final TelephonyManager mTelephony = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        if (mTelephony.getDeviceId() != null) {
                            device_imei = mTelephony.getDeviceId();
                        } else {
                            device_imei = Settings.Secure.getString(
                                    getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID);
                        }
                    }
                    phone_text.setText(device_phone_number);
                    settingSmsApp();
                    FirebaseCrashlytics.getInstance().log("Higgs-Boson detected! Bailing out");
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_CODE);
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_NUMBERS}, READ_PHONE_NUMBERS_REQUEST_CODE);
            }

        } else {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, READ_SMS_REQUEST_CODE);
        }

    }

    private View.OnClickListener login_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            loginToServer();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 100:{
                requestPermissions(new String[]{Manifest.permission.READ_SMS}, READ_SMS_REQUEST_CODE);
            }
            case 102:{
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_NUMBERS}, READ_PHONE_NUMBERS_REQUEST_CODE);
            }
            case 103:{
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_CODE);
            }
            case 104:{
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    device_phone_number = tMgr.getLine1Number();
//                    device_model = Build.MODEL;
//                    device_name = DeviceName.getDeviceName();
//                    device_imei = tMgr.getSimSerialNumber();
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        device_imei = Settings.Secure.getString(
                                getApplicationContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                    } else {
                        final TelephonyManager mTelephony = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                        if (mTelephony.getDeviceId() != null) {
                            device_imei = mTelephony.getDeviceId();
                        } else {
                            device_imei = Settings.Secure.getString(
                                    getApplicationContext().getContentResolver(),
                                    Settings.Secure.ANDROID_ID);
                        }
                    }
//                    Toast.makeText(getApplicationContext(), device_imei, device_imei.length()).show();
                    phone_text.setText(device_phone_number);
                    settingSmsApp();
                    FirebaseCrashlytics.getInstance().log("Higgs-Boson detected! Bailing out");
                    return;
                }

            }
        }
    }

    public void loginToServer() {
        RequestParams rp = new RequestParams();
        rp.add("deviceid", device_imei);
        rp.add("phoneno", device_phone_number);
        rp.add("signaltype", "login");
        rp.add("deviceip", Utils.getIPAddress(true));
        String url = "/logindevice.php";
        HttpUtils.post(url, rp, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                try {
                    String status = response.getString("status");
                    if(status.equals("true")){
                        Toast.makeText(getApplicationContext(), "LogIn Success!", Toast.LENGTH_SHORT).show();
                        moveToReceivingScreen();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "LogIn failed!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse){
                Toast.makeText(getApplicationContext(), "LogIn failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void moveToReceivingScreen(){
        Intent intent = new Intent(LogInActivity.this, ReceivingSmsActivity.class);
        intent.putExtra("phone_number", device_phone_number);
        startActivity(intent);
        finish();
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
