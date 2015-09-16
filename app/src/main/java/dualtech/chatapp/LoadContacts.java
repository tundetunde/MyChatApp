package dualtech.chatapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Created by tunde_000 on 31/08/2015.
 */
public class LoadContacts extends Activity{
    private static final String TAG = "LOADCONTACTS";
    static ArrayList<String> numbers;
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_contact);
        spinner=(ProgressBar)findViewById(R.id.pb1);
        spinner.setVisibility(View.VISIBLE);
        prefs = this.getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        read_contact();
        numbers = new ArrayList<>();
        gcm = GoogleCloudMessaging.getInstance(this);
    }

    private ArrayList<String> generateList(){
        ArrayList<String> chicken = new ArrayList<String>();
        for(int i = 0; i < 499; i++)
            chicken.add(i, "12345678900");
        chicken.add("00000000000");
        return chicken;
    }

    private void getContactsVolley(){
        StringRequest postRequest = new StringRequest(Request.Method.POST, ApplicationInit.SERVER_ADDRESS,
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
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // the POST parameters:
                Gson gson = new Gson();
                String jsonPhoneList = "";
                ArrayList<String> hs = new ArrayList<String>();
                //TestList: ArrayList<String> h1 = generateList();
                hs.addAll(numbers);
                numbers.clear();
                numbers.addAll(hs);
                jsonPhoneList = gson.toJson(hs);
                params.put("Contacts", "c");
                params.put("List", jsonPhoneList);
                params.put("Phone", ApplicationInit.getMobile_number());

                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    private void getContactsFromServer(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                ArrayList<String> hs = new ArrayList<String>();
                hs.addAll(numbers);
                numbers.clear();
                numbers.addAll(hs);
                try {
                    String id = String.valueOf(ApplicationInit.getMsgId());
                    Bundle data = new Bundle();
                    Gson gson = new Gson();
                    int num = numbers.size();
                    int si = hs.size();
                    String jsonPhoneList = "";
                    jsonPhoneList = gson.toJson(hs);
                    byte[] bytes = jsonPhoneList.getBytes();
                    Log.d("json list", jsonPhoneList);
                    Log.d("json size", Integer.toString(si));
                    Log.d("byte size", Integer.toString(bytes.length));
                    data.putString("Type", "Contacts");
                    data.putString("List", jsonPhoneList);
                    data.putString("Phone", prefs.getString(ApplicationInit.PROPERTY_REG_ID,null));
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent Contact";
                } catch (IOException ex) {
                    msg = "Contact could not be sent";
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(LoadContacts.this, msg, Toast.LENGTH_LONG).show();
                Intent openMain = new Intent("dualtech.chatapp.MAINACTIVITY");
                openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openMain);
            }
        }.execute(null, null, null);
    }

    public void read_contact() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d(TAG, "doInBack");
                ContentResolver CR = LoadContacts.this.getContentResolver();
                Cursor contact_details = CR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", null,
                        "UPPER (" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

                if (contact_details.moveToFirst()) {
                    do {
                        String contactName = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String phoneNumber;

                        if (Integer.parseInt(contact_details.getString(contact_details.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            //Get all associated numbers
                            phoneNumber = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            numbers.add(phoneNumber.replaceAll("\\s", ""));
                        }
                    } while (contact_details.moveToNext());
                }
                contact_details.close();
                Log.d(TAG, "Done");
                return "DONE";
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, "Done list");
                //getContactsFromServer();
                getContactsVolley();
                Intent openMain = new Intent("dualtech.chatapp.MAINACTIVITY");
                openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openMain);
            }
        }.execute();
    }
}
