package dualtech.chatapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

public class ContactView extends Fragment implements View.OnClickListener{
    private static final String TAG = "CONTACTVIEW";
    static ArrayList<String> appContacts;
    static ArrayList<Contact> app_contact;
    static ContactViewAdapter adapter;
    ProgressBar loader;
    List<String> cc, numbers;
    GoogleCloudMessaging gcm;
    DbSqlite db;
    ListView lvAppContacts, lvPhoneContacts;
    SharedPreferences prefs;
    ArrayAdapter<String> adapter2;

    public static void updateList(final ArrayList<Contact> list){
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        app_contact = list;
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list, container, false);
        prefs = v.getContext().getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        gcm = GoogleCloudMessaging.getInstance(getActivity());
        cc = new ArrayList<>();
        numbers = new ArrayList<>();
        db = new DbSqlite(getActivity());
        read_contact();
        initialize(v);
        setHasOptionsMenu(true);
        return v;
    }

    private void initialize(final View v){
        loader = (ProgressBar) v.findViewById(R.id.contact_load);
        loader.setVisibility(View.VISIBLE);
        lvAppContacts = (ListView) v.findViewById(R.id.lvAppContacts);
        appContacts = (ArrayList<String>) db.getAllContacts();
        app_contact = new ArrayList<>();
        adapter = new ContactViewAdapter(v.getContext(), android.R.layout.simple_list_item_1, app_contact);
        lvAppContacts.setAdapter(adapter);

        lvAppContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent newActivity = new Intent(v.getContext(), ChatView.class);
                String s = arg0.getItemAtPosition(position).toString();
                Contact c = (Contact) arg0.getItemAtPosition(position);
                newActivity.putExtra("display", s);
                newActivity.putExtra("contact", c.number);
                startActivity(newActivity);
            }
        });
        lvPhoneContacts = (ListView)v.findViewById(R.id.lvPhoneContacts);
        adapter2 = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, cc);
        lvPhoneContacts.setAdapter(adapter2);

        //ListUtils.setDynamicHeight(lvAppContacts);
        //ListUtils.setDynamicHeight(lvPhoneContacts);
    }

    public void read_contact() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d(TAG, "doInBack");
                ContentResolver CR = getActivity().getContentResolver();
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

                            cc.add(contactName + "   " + phoneNumber);
                            numbers.add(phoneNumber);
                        }
                    } while (contact_details.moveToNext());
                }
                contact_details.close();
                Log.d(TAG, "Done");
                return "DONE";
            }

            @Override
            protected void onPostExecute(String msg) {
                adapter2.notifyDataSetChanged();
                Log.d(TAG, "Done list");
                sendContact();
                loader.setVisibility(View.GONE);
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
                for (String s : appContacts){app_contact.add(new Contact(getContactName(s), s));}
                adapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }

    public String getContactName(String num){
        String name = num;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return name;
    }

    @Override
    public void onClick(View v) {

    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.action_add).setVisible(false).setEnabled(false);
    }

    public static class ListUtils{
        public static void setDynamicHeight(ListView list){
            ListAdapter la = list.getAdapter();
            if(la == null){
                return;
            }
            int height = 0;
            int desiredHeight = MeasureSpec.makeMeasureSpec(list.getWidth(), MeasureSpec.UNSPECIFIED);

            for(int i = 0; i < la.getCount(); i++){
                View listItem = la.getView(i,null, list);
                listItem.measure(desiredHeight, MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams param = list.getLayoutParams();
            param.height = height + (list.getDividerHeight() * (la.getCount()-1));
            list.setLayoutParams(param);
            list.requestLayout();
        }
    }

    private class ContactViewAdapter extends ArrayAdapter<Contact> {
        private List<Contact> lt_contact = new ArrayList<>();

        public ContactViewAdapter(Context context, int resource, ArrayList<Contact> arr) {
            super(context, resource, arr);
            lt_contact = arr;
        }

        @Override
        public void add(Contact s){
            lt_contact.add(s);
            notifyDataSetChanged();
        }

        @Override
        public Contact getItem(int position) {
            return lt_contact.get(position);
        }

    }
}
