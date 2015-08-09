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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BroadcastActivity extends Activity {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final String TAG = "BROADCAST";
    static final String server_address = "http://localhost:8080/ChatServerDual/GCMServer";
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
                Log.d(TAG, "Entered onReceive");
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                Log.d(TAG, String.valueOf(sentToken));
                if (sentToken) {
                    Toast.makeText(BroadcastActivity.this, "Activated", Toast.LENGTH_LONG).show();

                    phnNo = ApplicationInit.getMobile_number();
                    regId = ApplicationInit.getREGISTRATION_KEY();

                    //SEND REGID TO SERVER VIA HTTP
                    sendToServer(regId);

                    storePref();
                    spinner.setVisibility(View.GONE);

                    Intent i = new Intent("dualtech.chatapp.MAINACTIVITY");
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(BroadcastActivity.this,"Cannot activation -- Try again",Toast.LENGTH_LONG).show();
                    onBackPressed();
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

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        // Request a string response
        StringRequest postRequest = new StringRequest(Request.Method.POST, token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Result handling
                        /*try {
                            JSONObject jsonResponse = new JSONObject(response).getJSONObject("form");
                            String site = jsonResponse.getString("site"),
                                    network = jsonResponse.getString("network");
                            System.out.println("Site: "+site+"\nNetwork: "+network);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                        //tvSignedIn.append("\nServer has received the RegID");
                        Toast.makeText(getApplicationContext(), "Server has received the RegID", Toast.LENGTH_SHORT).show();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("RegNo", ApplicationInit.PROPERTY_REG_ID);
                params.put("MobileNo", ApplicationInit.getMobile_number());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    public void sendToServer(final String s){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d("C2DM", "Sending registration ID to my application server");
                try {
                    String r = "registering";
                    String regValue = "RegNo=" + s + "&Register" + r;
                    StringBuilder sb = new StringBuilder();
                    URL url = new URL(server_address);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");
                    con.setConnectTimeout(10000);
                    con.setDoInput(true);
                    con.setDoOutput(true);


                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
                    writer.write(regValue);
                    writer.flush();
                    writer.close();

                    int responseCode = con.getResponseCode();
                    Log.d(TAG, "RESPONSE CODE: " + String.valueOf(responseCode));

                    if (responseCode == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String line;

                        while ((line = reader.readLine()) != null)
                            sb.append(line);
                    }

                    Log.d(TAG, sb.toString());
                    return sb.toString();
                }
                catch (IOException io){
                    System.out.println(io.toString());
                    return "ASYNCTASK.........";
                }
            }
            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute();
    }

    public static void storePref(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationInit.PROPERTY_REG_ID, regId);
        editor.putString(ApplicationInit.PROPERTY_MOB_ID, phnNo);
        editor.apply();

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
