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

public class BroadcastActivity extends Activity {
    private static final String TAG = "BROADCAST";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static String regId;
    static String phnNo;
    ProgressBar spinner;
    boolean isRegister;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isRegister = false;
        setContentView(R.layout.registration2);
        spinner=(ProgressBar)findViewById(R.id.progressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Entered onReceive");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                Log.d(TAG, String.valueOf(sentToken));
                if (sentToken) {
                    Toast.makeText(BroadcastActivity.this, "Activated", Toast.LENGTH_LONG).show();

                    phnNo = ApplicationInit.getMobile_number();
                    regId = ApplicationInit.getREGISTRATION_KEY();

                    spinner.setVisibility(View.GONE);

                    Intent i = new Intent().setClass(getApplicationContext(), LoadContacts.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(i);
                    Intent openMain = new Intent().setClass(getApplicationContext(), MainActivity.class);
                    openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(openMain);
                    finish();
                } else {
                    Toast.makeText(BroadcastActivity.this,"Cannot activation -- Try again",Toast.LENGTH_LONG).show();
                    //onBackPressed();
                    finish();
                }

            }
        };

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
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

    @Override
    public void onBackPressed(){

    }
}
