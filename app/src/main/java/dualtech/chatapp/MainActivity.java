package dualtech.chatapp;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends TabActivity {

    public static TextView tvSignedIn;
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            tvSignedIn = (TextView) findViewById(R.id.tvSignedIn1);
            sendRegistrationToServer(ApplicationInit.SERVER_ADDRESS);
            Resources res = getResources();
            TabHost tabHost = getTabHost();
// Chat tab
            Intent ChatIntent = new Intent().setClass(this, ChatView.class);
            TabHost.TabSpec chatTab = tabHost
                    .newTabSpec("Chat")
                    .setIndicator("CHAT")
                            //.setIndicator("", res.getDrawable(R.drawable.icon_android_config))
                    .setContent(ChatIntent);
// Feed tab
            Intent FeedIntent = new Intent().setClass(this,FeedView.class);
            TabHost.TabSpec feedTab = tabHost
                    .newTabSpec("Feed")
                    .setIndicator("FEED")
                            //.setIndicator("", res.getDrawable(R.drawable.icon_apple_config))
                    .setContent(FeedIntent);
// Contact tab
            Intent contactIntent = new Intent().setClass(this,ContactView.class);
            TabHost.TabSpec contactTab = tabHost
                    .newTabSpec("Contacts")
                    .setIndicator("CONTACTS")
                            //.setIndicator("", res.getDrawable(R.drawable.icon_windows_config))
                    .setContent(contactIntent);

            tabHost.addTab(chatTab);
            tabHost.addTab(feedTab);
            tabHost.addTab(contactTab);

//set Chat tab as default (zero based)
            tabHost.setCurrentTab(0);

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

}
