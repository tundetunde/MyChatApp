package dualtech.chatapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import java.io.IOException;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class MobileReg extends Activity implements View.OnClickListener {

    EditText mobileNum;
    Button reg_next;
    static String regId;
    static String phnNo;
    static SharedPreferences prefs;
    private static final String TAG = "Note"/*MainActivity*/;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        setContentView(R.layout.registration);

        mobileNum = (EditText) findViewById(R.id.phone_num);
        mobileNum.setText(VerifyNumber.getNO());
        reg_next = (Button) findViewById(R.id.bt_reg);
        reg_next.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_reg:
                phnNo = String.valueOf(mobileNum.getText());
                ApplicationInit.setMobile_number(phnNo);
                Intent i = new Intent("dualtech.chatapp.BROADCASTACTIVITY");
                startActivity(i);
        }
        }

    public static void storePref(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationInit.PROPERTY_REG_ID, regId);
        editor.putString(ApplicationInit.PROPERTY_MOB_ID, phnNo);
        editor.apply();
    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }
}