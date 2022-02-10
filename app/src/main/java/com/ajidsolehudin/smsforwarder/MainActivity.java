package com.ajidsolehudin.smsforwarder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnSend;
    private EditText etPhoneNumber, etMessage;
    private final static int REQUEST_CODE_PERMISSION_SEND_SMS = 123;

    private ListView lvSMS;
    private final static int REQUEST_CODE_PERMISSION_READ_SMS = 456;
    ArrayList<String> smsMsgList = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    public static MainActivity instance;

    public static MainActivity Instance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etMessage = findViewById(R.id.etMessage);
        btnSend =findViewById(R.id.btnSend);

        btnSend.setEnabled(false);

        //Setting List View For Messages
        lvSMS = findViewById(R.id.lv_sms);
        arrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, smsMsgList);
        lvSMS.setAdapter(arrayAdapter);

        if (checkPermission(Manifest.permission.READ_SMS)){
            refreshInbox();
        }else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    (Manifest.permission.READ_SMS)}, REQUEST_CODE_PERMISSION_READ_SMS);
        }

        if (checkPermission(Manifest.permission.SEND_SMS)) {
            btnSend.setEnabled(true);
        }else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {
                    (Manifest.permission.SEND_SMS)}, REQUEST_CODE_PERMISSION_SEND_SMS);
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = etMessage.getText().toString();
                String phoneNum = etPhoneNumber.getText().toString();

                SmsManager smsMan = SmsManager.getDefault();
                smsMan.sendTextMessage(phoneNum, null, msg, null, null);
                Toast.makeText(MainActivity.this,
                        "SMS send to " + phoneNum, Toast.LENGTH_LONG).show();
            }
        });
    }


    private boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
        @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_SEND_SMS:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    btnSend.setEnabled(true);
                }
                break;
        }
    }

    public void refreshInbox(){
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms?inbox"),
                null, null,null,null);

        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");

        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;

        arrayAdapter.clear();
        do {
            String str = "SMS From :" + smsInboxCursor.getString(indexAddress) + '\n';
            str += smsInboxCursor.getString(indexBody);
            arrayAdapter.add(str);
        }while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMsg){
        arrayAdapter.insert(smsMsg, 0);
        arrayAdapter.notifyDataSetChanged();
    }
}