package dualtech.chatapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactView extends Fragment implements View.OnClickListener{
    private static final String TAG = "CONTACTVIEW";
    ProgressBar loader;
    List<String> cc;
    GoogleCloudMessaging gcm;
    String answer;
    DbSqlite db;
    List<String> appContacts;
    ListView lvAppContacts, lvPhoneContacts;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list, container, false);
        gcm = GoogleCloudMessaging.getInstance(getActivity());
        cc = new ArrayList<>();
        db = new DbSqlite(getActivity());
        read_contact();
        appContacts = db.getAllContacts();
        initialize(v);
        return v;
    }

    private void initialize(final View v){
        loader = (ProgressBar) v.findViewById(R.id.contact_load);
        loader.setVisibility(View.VISIBLE);
        lvAppContacts = (ListView) v.findViewById(R.id.lvAppContacts);
        ArrayAdapter adapter = new ArrayAdapter(v.getContext(), android.R.layout.simple_list_item_1, appContacts);
        lvAppContacts.setAdapter(adapter);

        lvAppContacts.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                Intent newActivity = new Intent(v.getContext(), ChatView.class);
                String s = arg0.getItemAtPosition(position).toString();
                newActivity.putExtra("contact", s);
                startActivity(newActivity);
            }
        });
        lvPhoneContacts = (ListView)v.findViewById(R.id.lvPhoneContacts);
        ArrayAdapter adapter2 = new ArrayAdapter(v.getContext(), android.R.layout.simple_list_item_1, cc);
        lvPhoneContacts.setAdapter(adapter2);
    }

    public void read_contact() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d("CONTACTVIEW", "doInBack");
                HashMap<String, String> data /*= new HashMap<>(2)*/;
                ContentResolver CR = getActivity().getContentResolver();
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
                        cc.add(contactName + "   " + phoneNumber);
                    }
                }
                contact_details.close();
                Log.d("CONTACTVIEW", "Done");
                return "DONE";
            }


            @Override
            protected void onPostExecute(String msg) {
                Log.d("CONTACTVIEW", "Done list");
                loader.setVisibility(View.GONE);
                //list_adapter();
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
        }) {
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
        Volley.newRequestQueue(getActivity()).add(postRequest);
    }

    public Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("name").compareTo(m2.get("name"));
        }
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.lvAppContacts:
                break;
        }
    }
}
