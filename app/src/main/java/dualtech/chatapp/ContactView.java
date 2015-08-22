package dualtech.chatapp;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ContactView extends ListActivity {
    private static final String TAG = "CONTACTVIEW";
    ProgressBar loader;
    List<Map<String, String>> cc;
    GoogleCloudMessaging gcm;
    String answer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
        gcm = GoogleCloudMessaging.getInstance(ContactView.this);
        cc = new ArrayList<>();
        loader = (ProgressBar) findViewById(R.id.contact_load);
        loader.setVisibility(View.VISIBLE);
        read_contact();
    }

    public void read_contact (){

        new AsyncTask<Void, Void, String> (){
            @Override
            protected String doInBackground(Void... params) {
                Log.d("CONTACTVIEW", "doInBack");
                HashMap<String, String> data /*= new HashMap<>(2)*/;
                ContentResolver CR = getContentResolver();
                Cursor contact_details = CR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                contact_details.moveToFirst();


                while (contact_details.moveToNext()) {
                    String contactId = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String contactName = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber;

                    if (Integer.parseInt(contact_details.getString(contact_details.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        //Get all associated numbers
/*                        Cursor contact_number = CR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contactId}, null);
                        contact_number.moveToFirst();*/

                        phoneNumber = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        data = new HashMap<>(2);
                        data.put("name", contactName);
                        data.put("phone", phoneNumber);
                        cc.add(data);
                    }
                }
                contact_details.close();
                Log.d("CONTACTVIEW" ,"Done");
                return "DONE";
        }



        @Override
        protected void onPostExecute (String msg) {
            Log.d("CONTACTVIEW", "Done list");
            loader.setVisibility(View.GONE);
            list_adapter();
        }
    }.execute();

}

    private void sendContact(final String reg) {
        // Add custom implementation, as needed.
        StringRequest postRequest = new StringRequest(Request.Method.POST, ApplicationInit.SERVER_ADDRESS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonResponse;
                        try {
                            jsonResponse = new JSONObject(response);
                            answer = jsonResponse.getString("Contacts");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
                params.put("RegNo", ApplicationInit.getREGISTRATION_KEY());
                params.put("", ApplicationInit.getMobile_number());
                params.put("Contacts", "yes");
                //params.put("token", token);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    public Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("name").compareTo(m2.get("name"));
        }
    };


    public void list_adapter(){
        Collections.sort(cc, mapComparator);
        ArrayList<String> listPhoneNumber = new ArrayList<String>();
        for(Map<String, String> s: cc){
            Iterator<Map.Entry<String, String>> iterator = s.entrySet().iterator() ;
            while(iterator.hasNext()){
                Map.Entry<String, String> x = iterator.next();
                listPhoneNumber.add(x.getValue());

            }
        }
        ArrayList<String> a = new ArrayList<String>();
        for(String x: listPhoneNumber){
            sendContact(x);
            a.add(answer);
        }
        SpecialAdapter adapter =  new SpecialAdapter(this, cc, android.R.layout.simple_list_item_2,
                new String[] {"name", "phone"}, new int[] {android.R.id.text1, android.R.id.text2,}, false, a);
        setListAdapter(adapter);
    }

    public class SpecialAdapter extends SimpleAdapter {
        private int[] colors = new int[] { 0x30FF0000, 0x300000FF };
        boolean inApp;
        ArrayList<String> x;

        public SpecialAdapter(Context context, List<Map<String, String>> items, int resource, String[] from, int[] to, boolean inApp, ArrayList<String> a) {
            super(context, items, resource, from, to);
            this.inApp = inApp;
            x = a;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if(x.get(position).equals("y"))
                view.setBackgroundColor(colors[0]);
            else
                view.setBackgroundColor(colors[1]);
            /*if(inApp)
                view.setBackgroundColor(colors[0]);
            else
                view.setBackgroundColor(colors[1]);*/
            return view;
        }
    }
}
