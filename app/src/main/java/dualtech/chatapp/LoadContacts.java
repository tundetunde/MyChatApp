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

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

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

    private void getContactsFromServer(){
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
                Toast.makeText(LoadContacts.this, msg, Toast.LENGTH_LONG).show();
                Intent openMain = new Intent("dualtech.chatapp.MAINACTIVITY");
                openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openMain);
            }
        }.execute(null, null, null);
    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
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
                Log.d(TAG, "Done list");
                getContactsFromServer();
            }
        }.execute();
    }
}
