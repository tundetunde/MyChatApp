package dualtech.chatapp;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tunde_000 on 31/08/2015.
 **/
public class LoadContacts extends IntentService{
    public static final String TAG = "LOAD_CONTACT";
    static Set<String> numSet;
    static ArrayList<String> numbers;
    GoogleCloudMessaging gcm;

    public LoadContacts() {
        super("Load Contact");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        numSet = new HashSet<>();
        numbers = new ArrayList<>();
        gcm = GoogleCloudMessaging.getInstance(this);
        read_contact();
        Log.d(TAG, "Handle Contact");
    }

    private void getContactsFromServer(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                int subSize = 100;
                List<List<String>> subLists = new ArrayList<>();
                for (int i = 0; i < numbers.size(); i += subSize) {
                    subLists.add(numbers.subList(i, Math.min(i + subSize, numbers.size())));
                }
                try {
                    for(List<String> lst : subLists){
                        String id = String.valueOf(ApplicationInit.getMsgId());
                        Bundle data = new Bundle();
                        Gson gson = new Gson();
                        String jsonPhoneList  = gson.toJson(lst);
                        data.putString("Type", "Contacts");
                        data.putString("List", jsonPhoneList);
                        data.putString("Phone", ApplicationInit.getREGISTRATION_KEY());
                        gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    }
                    msg = "Sent Contact";
                } catch (IOException ex) {
                    msg = "Contact could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            }
        }.execute(null, null, null);
    }

    public void read_contact() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                ContentResolver CR = LoadContacts.this.getContentResolver();
                Cursor contact_details = CR.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1", null,
                        "UPPER (" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + ") ASC");

                if (contact_details.moveToFirst()) {
                    do {
                        String phoneNumber;

                        if (Integer.parseInt(contact_details.getString(contact_details.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                            //Get all associated numbers
                            phoneNumber = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            numSet.add(phoneNumber.replaceAll("\\s", ""));
                        }
                    } while (contact_details.moveToNext());
                }
                contact_details.close();
                return "DONE";
            }

            @Override
            protected void onPostExecute(String msg) {
                numbers.clear();
                numbers.addAll(numSet);
                Log.d(TAG, "Read Contacts");
                getContactsFromServer();
            }
        }.execute();
    }

}