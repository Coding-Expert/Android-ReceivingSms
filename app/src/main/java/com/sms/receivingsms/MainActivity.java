package com.sms.receivingsms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.Observable;
import java.util.Observer;
import com.jaredrummler.android.device.DeviceName;



public class MainActivity extends AppCompatActivity {

    private TextView msg_textview;
    SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private static final int SMS_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;



    private Button login_btn;
    private Button register_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        msg_textview = (TextView) findViewById(R.id.message_textview);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        login_btn = (Button)findViewById(R.id.login);
        register_btn = (Button)findViewById(R.id.register);
        login_btn.setOnClickListener(login_listener);
        register_btn.setOnClickListener(register_listener);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.SEND_SMS)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need SMS Permission");
                builder.setMessage("This app needs SMS permission to send Messages.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.SEND_SMS, false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need SMS Permission");
                builder.setMessage("This app needs SMS permission to send Messages.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(),
                                "Go to Permissions to Grant SMS permissions", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}
                        , SMS_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.SEND_SMS, true);
            editor.commit();

        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

                    }
                    else{
                        Toast.makeText(this, "No permission granted", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

            }

        }

    }

    private View.OnClickListener login_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
            finish();
        }
    };
    private View.OnClickListener register_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    };

}
