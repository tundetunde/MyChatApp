package dualtech.chatapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ChatList extends ListFragment implements View.OnClickListener{
    DbSqlite db;
    ArrayList<String> chatList;
    ArrayList<Contact> chatName;
    ArrayAdapter<Contact> adapter;
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_list, container, false);

        prefs = getActivity().getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        gcm = GoogleCloudMessaging.getInstance(getActivity().getApplicationContext());
        db = new DbSqlite(getActivity());
        chatList = (ArrayList)db.getChatList();
        chatName = new ArrayList<>();
        for (String s : chatList){chatName.add(new Contact(getContactName(s), s));}
        adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, chatName);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResume() {
        super.onResume();
        chatList.clear();
        chatList = (ArrayList)db.getChatList();
        adapter.notifyDataSetChanged();
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

    public void getImages(){new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
            String msg;
            try {
                String id = String.valueOf(msgId());
                Bundle data = new Bundle();
                Gson gson = new Gson();
                String jsonPhoneList = gson.toJson(PhotoRequestList());
                data.putString("Type", "GetPhoto");
                data.putString("List", jsonPhoneList);
                data.putString("Phone", prefs.getString(ApplicationInit.PROPERTY_REG_ID,null));
                gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                msg = "Photo requested";
            } catch (IOException ex) {
                msg = "Photo could not be sent";
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {}
    }.execute(null, null, null);
    }

    public ArrayList<String> PhotoRequestList(){
        ArrayList<String> returnedList = new ArrayList<>();
        ArrayList<String> chatlist = (ArrayList<String>) db.getChatList();

        for(String contact : chatlist){
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File mypath = new File(directory, "profile_" + contact + ".jpg");
            if(!mypath.exists()){
                returnedList.add(contact);
            }
        }
        return returnedList;
    }
}