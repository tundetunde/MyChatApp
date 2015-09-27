package dualtech.chatapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Created by Jesz on 09-Sep-15.
*/
public class InviteContact extends AppCompatActivity{
        private static final String TAG = "INVITE_CONTACT";
        ProgressBar loader;
        List<Map<String, String>> cc;
        DbSqlite db;
        ListView lvPhoneContacts;
        SharedPreferences prefs;
        SimpleAdapter adapter;
        Toolbar toolbar;

    public static void setDynamicHeight(ListView list){
        SimpleAdapter adapter = (SimpleAdapter) list.getAdapter();
        if(adapter == null){
            return;
        }
        int height = 0;

        for(int i = 0; i < adapter.getCount(); i++){
            View listItem = adapter.getView(i,null, list);
            listItem.measure(0,0);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams param = list.getLayoutParams();
        param.height = height + (list.getDividerHeight() * (adapter.getCount()-1));
        list.setLayoutParams(param);
        list.requestLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_contact);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.invite_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("CONTACTS");

        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        cc = new ArrayList<>();
        db = new DbSqlite(this);
        read_contact();
        initialize();
    }

    private void initialize(){
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        loader = (ProgressBar) findViewById(R.id.contact_load);
        loader.setVisibility(View.VISIBLE);

        lvPhoneContacts = (ListView)findViewById(R.id.lvPhoneContacts);
        adapter = new SimpleAdapter(this, cc, android.R.layout.simple_list_item_2,
                new String[] { "name", "phone" },
                new int[] { android.R.id.text1, android.R.id.text2 });
        lvPhoneContacts.setAdapter(adapter);

        lvPhoneContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,String> map = (HashMap<String, String>) parent.getItemAtPosition(position);
                sendInvitation(map.get("name"), map.get("phone"));
            }
        });
        setDynamicHeight(lvPhoneContacts);
    }

    public void read_contact() {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    List c = db.getAllContacts();
                    ContentResolver CR = getContentResolver();
                    Cursor contact_details = CR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", null,
                            "UPPER (" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

                    if (contact_details.moveToFirst()) {
                        do {
                            String contactName = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            String phoneNumber;

                            if (Integer.parseInt(contact_details.getString(contact_details.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                phoneNumber = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replaceAll("\\s","");
                                Map<String, String> mapList= new HashMap<>(2);
                                if(!c.contains(phoneNumber)) {
                                    mapList.put("name", contactName);
                                    mapList.put("phone", phoneNumber);
                                    cc.add(mapList);
                                }
                            }
                        } while (contact_details.moveToNext());
                    }
                    contact_details.close();
                    return "DONE";
                }

                @Override
                protected void onPostExecute(String msg) {
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Done Phone contact");
                    loader.setVisibility(View.GONE);
                }
            }.execute();
        }

    public void sendInvitation(final String s, final String n){

        final TextView txt = new TextView(this);
        final String url = "www.google.com";

        txt.setText("Hey,\n" +
                "Join me on this app and chat with me and many other friends" +
                "\n" +
                "PS. STILL ON TESTING PHASE" +
                "\n");

        new AlertDialog.Builder(this)
                .setTitle("Invite " + s)
                .setMessage("Data charges may apply!!!")
                .setView(txt)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String message = txt.getText() + "\n" + url;
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(n, null, message, null, null);
                        Toast.makeText(getBaseContext(), "Invitation Sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                item.setOnMenuItemClickListener(new MenuItemListener(this));
        }
        return super.onOptionsItemSelected(item);
    }
}