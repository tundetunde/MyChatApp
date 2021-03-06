package dualtech.chatapp;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private static final String PROJECT_NO = "25515784135";
    Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
    LocalBroadcastManager broadcastManager;
    SharedPreferences sharedPreferences;
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        String token;

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(this);
                token = instanceID.getToken(PROJECT_NO, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);

//                ApplicationInit.setREGISTRATION_KEY(token);// Save the regid for global use - no need to register again.

                // Subscribe to topic channels
                subscribeTopics(token);
                //API_KEY = token;
                // [END register_for_gcm]

                //Send to server and wait for response
                // Notify UI that registration has completed, so the progress indicator can be hidden.
                sendRegistrationToServer(token);
                // [ END Response received]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            broadcastManager.sendBroadcast(registrationComplete);
        }
    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token) {
        // Add custom implementation, as needed.
        StringRequest postRequest = new StringRequest(Request.Method.POST, ApplicationInit.SERVER_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                        sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                        broadcastManager.sendBroadcast(registrationComplete);
                        Toast.makeText(getApplicationContext(), "Server has received the RegID", Toast.LENGTH_SHORT).show();
                        ApplicationInit.setREGISTRATION_KEY(token);// Save the regid for global use - no need to register again.

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(TAG, error.toString());
                broadcastManager.sendBroadcast(registrationComplete);
                Toast.makeText(getApplicationContext(), "Server failed to receive the RegID", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                params.put("RegNo", token);
                params.put("MobileNo", ApplicationInit.getMobile_number());
                params.put("Register", "yes");
                return params;
            }
        };
        /*postRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));*/
        Volley.newRequestQueue(this).add(postRequest);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

}
