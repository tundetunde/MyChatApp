package dualtech.chatapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatList extends ListFragment implements View.OnClickListener{
    DbSqlite db;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_list, container, false);

        db = new DbSqlite(getActivity());
        ArrayList<String> chatList = (ArrayList)db.getChatList();
        ArrayList<Contact> chatName = new ArrayList<>();
        for (String s : chatList){chatName.add(new Contact(getContactName(s), s));}
        ArrayAdapter<Contact> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, chatName);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.action_refresh).setVisible(false).setEnabled(false);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), ChatView.class);
        String s = l.getItemAtPosition(position).toString();
        Contact c = (Contact) l.getItemAtPosition(position);
        intent.putExtra("display", s);
        intent.putExtra("contact", c.number);
        startActivity(intent);
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

}