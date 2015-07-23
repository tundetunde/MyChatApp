package dualtech.chatapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by tunde_000 on 23/07/2015.
 */
public class BroadcastActivity extends Activity {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final String TAG = "Note";
    ProgressBar spinner;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static String regId;
    static String phnNo;
    static SharedPreferences prefs;
    boolean isRegister;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRegister = false;
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        setContentView(R.layout.registration2);
        spinner=(ProgressBar)findViewById(R.id.progressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //setContentView(R.layout.registration);
                Log.d(TAG, "Entered onReceive");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                Log.d(TAG, String.valueOf(sentToken));
                if (sentToken) {
                    Toast.makeText(BroadcastActivity.this, "Activated", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BroadcastActivity.this,"Cannot activation -- Try again",Toast.LENGTH_LONG).show();
                }
                MobileReg.storePref();
                /*Intent openMain = new Intent(context, MainActivity.class);
                openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                openMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
                context.startActivity(openMain);*/

                spinner.setVisibility(View.GONE);

            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        Intent i = new Intent("dualtech.chatapp.MAINACTIVITY");
        startActivity(i);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public static void storePref(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationInit.PROPERTY_REG_ID, regId);
        editor.putString(ApplicationInit.PROPERTY_MOB_ID, phnNo);
        editor.apply();

        ApplicationInit.setMobile_number(phnNo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
