package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Random;


/**
* Created by Jesz on 19-Jul-15.
*/
public class VerifyNumber extends Activity implements View.OnClickListener {

    static  String P_NO;
    EditText mobile_Num;
    Button next;
    ProgressBar spinner;
    Spinner country_list;
    SharedPreferences prefs;
    String V_Code;
    Boolean sent;

    public static String getNO() {
        return P_NO;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        setContentView(R.layout.phone_entry);

        sent = false;
        spinner=(ProgressBar)findViewById(R.id.loadBar);
        spinner.setVisibility(View.GONE);

        country_list = (Spinner) findViewById(R.id.dial_code);
        mobile_Num = (EditText) findViewById(R.id.phone_number);
        next = (Button) findViewById(R.id.bt_verify);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        P_NO = String.valueOf(mobile_Num.getText());
        spinner.setVisibility(View.VISIBLE);
        sendSMSMessage();
        if(sent){
            ApplicationInit.setMobile_number(P_NO);
            sent = false;
            Intent openMain = new Intent("dualtech.chatapp.REG").setClass(getApplicationContext(),MobileReg.class);
            startActivity(openMain);
        }

    }

    protected void sendSMSMessage() {
        Log.i("Send SMS", "");

        Random rand = new Random();
        V_Code = String.valueOf(rand.nextInt(9999) + 1000);//Verification code

        String message = "D-Chat VERIFICATION CODE : "+ V_Code;
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", P_NO);
        smsIntent.putExtra("sms_body"  , message);

        /*try {
            startActivity(smsIntent);
            finish();
            Log.i("Finished sending SMS...", "");
            sent = true
        }
        catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
        }*/
        sent = true;
        spinner.setVisibility(View.GONE);
    }
}
