package dualtech.chatapp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Jesz on 09-Sep-15.
 */
public class InviteContact extends AppCompatActivity {
        private static final String TAG = "INVITECONTACT";
        ProgressBar loader;
        List<String> cc;
        DbSqlite db;
        ListView lvPhoneContacts;
        SharedPreferences prefs;
        ArrayAdapter<String> adapter2;
        Toolbar toolbar;

    public static void setDynamicHeight(ListView list){
            ArrayAdapter adapter = (ArrayAdapter) list.getAdapter();
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
        read_contact();
        initialize();
    }

    private void initialize(){
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            loader = (ProgressBar) findViewById(R.id.contact_load);
            loader.setVisibility(View.VISIBLE);

            lvPhoneContacts = (ListView)findViewById(R.id.lvPhoneContacts);
            adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cc);
            lvPhoneContacts.setAdapter(adapter2);

            setDynamicHeight(lvPhoneContacts);
        }

    public void read_contact() {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    Log.d(TAG, "doInBack");
                    ContentResolver CR = getContentResolver();
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
                    setDynamicHeight(lvPhoneContacts);
                    Log.d(TAG, "Done Phone contact");
                    loader.setVisibility(View.GONE);
                }
            }.execute();
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
