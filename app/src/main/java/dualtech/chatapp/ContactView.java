package dualtech.chatapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactView extends Fragment implements View.OnClickListener{
    private static final String TAG = "CONTACTVIEW";
    ProgressBar loader;
    List<String> cc;
    List<String> numbers;
    GoogleCloudMessaging gcm;
    String answer;
    DbSqlite db;
    static ArrayList<String> appContacts;
    ListView lvAppContacts, lvPhoneContacts;
    SharedPreferences prefs;
    static ArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list, container, false);
        prefs = v.getContext().getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        gcm = GoogleCloudMessaging.getInstance(getActivity());
        cc = new ArrayList<>();
        numbers = new ArrayList<>();
        db = new DbSqlite(getActivity());
        read_contact();
        appContacts = new ArrayList<String>();
        sendContact();
        initialize(v);
        return v;
    }

    public void writeContactsToDatabase(){

    }

    private void initialize(final View v){
        loader = (ProgressBar) v.findViewById(R.id.contact_load);
        loader.setVisibility(View.VISIBLE);
        lvAppContacts = (ListView) v.findViewById(R.id.lvAppContacts);
        adapter = new ArrayAdapter(v.getContext(), android.R.layout.simple_list_item_1, appContacts);
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
                        numbers.add(phoneNumber);
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

    private void sendContact() {
        // Add custom implementation, as needed.
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;

                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    Gson gson = new Gson();
                    String jsonPhoneList = gson.toJson(numbers);
                    data.putString("Type", "Contacts");
                    data.putString("List", jsonPhoneList);
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
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

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }
}
