package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
* Created by Jesz on 18-Jul-15.
*/
public class SplashScreen extends Activity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make sure the app is registered with GCM and with the server

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(1500);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally {
                    Intent i = new Intent().setClass(getApplicationContext(), LoadContacts.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(i);
                    Intent openMain = new Intent().setClass(getApplicationContext(), MainActivity.class);
                    openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                // If there is no registration ID, the app isn't registered.
                    if (ApplicationInit.getREGISTRATION_KEY()== null){
                        openMain = new Intent().setClass(getApplicationContext(), VerifyNumber.class);
                    }
                    openMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(openMain);
                }
            }
        };
        timer.start();
    }
}
