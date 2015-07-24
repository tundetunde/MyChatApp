package dualtech.chatapp;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class ContactView extends ListActivity {
    private static final String TAG = "CONTACTVIEW";
    ProgressBar loader;
    List<Map<String, String>> cc;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list);
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
    public Comparator<Map<String, String>> mapComparator = new Comparator<Map<String, String>>() {
        public int compare(Map<String, String> m1, Map<String, String> m2) {
            return m1.get("name").compareTo(m2.get("name"));
        }
    };


    public void list_adapter(){
        Collections.sort(cc, mapComparator);
        SimpleAdapter adapter = new SimpleAdapter(this, cc, android.R.layout.simple_list_item_2,
                new String[] {"name", "phone"}, new int[] {android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);
    }
}
