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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
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
    String TAG = "MOBILE";
    TextView info;

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
        info = (TextView) findViewById(R.id.tvInfo);
        info.setText("Enter as you would locally. For Example: enter 07944447710 not +447944447710");
        next = (Button) findViewById(R.id.bt_verify);
        next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        P_NO = String.valueOf(mobile_Num.getText());
        spinner.setVisibility(View.VISIBLE);
        String countryCode = country_list.getSelectedItem().toString();
        countryCode = countryCode.substring(0, countryCode.length() - 3);
        //sendSMSMessage();
        String number = countryCode + P_NO.substring(1);
        VerifyNumber(number);
        if(sent){
            ApplicationInit.setMobile_number(P_NO);
            sent = false;
            Intent openMain = new Intent("dualtech.chatapp.REG").setClass(getApplicationContext(),MobileReg.class).putExtra("verificationCode", V_Code);
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

    private void VerifyNumber(String number) {
        Random rand = new Random();
        V_Code = String.valueOf(rand.nextInt(9999) + 1000);
        String message = "Your Verification Code is " + V_Code;
        //message = URLEncoder.encode(message);
        String senderID = "123456";
        String url = "https://control.msg91.com/api/sendhttp.php?authkey=98940ACbGtaEc565a1f69&mobiles=" + number + "&message=" + message + "&sender=" + senderID + "&route=4&country=44";
        //String url = "http://www.smsglobal.com/http-api.php?action=sendsms&user=wr4q7hzh&password=ou6U865z&&from=447944447710&to=" + number + "&text=" + message;
        // Add custom implementation, as needed.
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(TAG, error.toString());
                Toast.makeText(getApplicationContext(), "SMS Failed", Toast.LENGTH_SHORT).show();
            }
        });
        /*postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        Volley.newRequestQueue(this).add(postRequest);
        sent = true;
    }
}
