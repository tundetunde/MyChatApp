package dualtech.chatapp;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class ContactView extends ListActivity {
    private static final String TAG = "CONTACTVIEW";
    List<Map<String, String>> cc;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textview = new TextView(this);
        textview.setText("This is Contact tab");
        setContentView(R.layout.contact_list);
        cc = new ArrayList<>();
        read_contact();
        list_adapter();
    }

    public void read_contact() {
        Map<String, String> data = new HashMap<>(2);

        Cursor contact_details = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        contact_details.moveToFirst();
        Log.d(TAG, "Contact number: " + contact_details.getCount());

        if (contact_details.getCount() > 0){
            while (contact_details.moveToNext()) {
                String contactName = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber;
                data.put("name", contactName);

                //Get all associated numbers
                Cursor contact_number = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contactName }, null);
                contact_number.moveToFirst();

                while(contact_number.moveToNext()){
                    phoneNumber = contact_details.getString(contact_details.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    data.put("phone", phoneNumber);
                    cc.add(data);
                }
                    contact_number.close();
            }
        }
        contact_details.close();

    }

    public void list_adapter(){
        SimpleAdapter adapter = new SimpleAdapter(this, cc, android.R.layout.simple_list_item_2,
                new String[] {"name", "phone"}, new int[] {android.R.id.text1, android.R.id.text2});
        setListAdapter(adapter);
    }
}
