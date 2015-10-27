package dualtech.chatapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by Jesz on 29-Aug-15.
 */

public class Contact{
    String number , name;
    Context context;
    private boolean checked = false ;

    Contact(String n, String num){
        number = num;
        name = n;
    }

    Contact(Context c, String num){
        number = num;
        name = getContactName(num);
        context = c;
    }

    @Override
    public String toString(){
        return name;
    }


    public String getContactName(String num){
        String name = num;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return name;
    }

    public boolean isChecked(){
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        checked = !checked ;
    }
}
