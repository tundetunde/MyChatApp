package dualtech.chatapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatList extends ListFragment implements View.OnClickListener{
    DbSqlite db;
    ArrayList<String> chatList;
    ArrayList<Contact> chatName;
    ChatListAdapter adapter;
    static ArrayList<ChatItem> chat_query = new ArrayList<>();
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
        //for (String s : chatList){chatName.add(new Contact(getContactName(s), s));}
        //adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, chatName);
        //setListAdapter(adapter);
        setHasOptionsMenu(true);
        refreshChatList();
        return v;
    }

    @Override
    public void onClick(View v) {

    }

    public void refreshChatList(){
        List<String> query = db.getChatList();
        chatList.clear();
        chat_query.clear();
        for (String s : query) {
            chat_query.add(new ChatItem(getContactName(s), s));
        }
        //chatList = (ArrayList)db.getChatList();adapter = new FeedAdapter(getActivity(), R.layout.feed_box, feed_query);
        adapter = new ChatListAdapter(getActivity(), R.layout.feed_box, chat_query);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        List<String> query = db.getChatList();
        chatList.clear();
        chat_query.clear();
        for (String s : query) {
            chat_query.add(new ChatItem(getContactName(s), s));
        }
        //chatList = (ArrayList)db.getChatList();adapter = new FeedAdapter(getActivity(), R.layout.feed_box, feed_query);
        adapter = new ChatListAdapter(getActivity(), R.layout.feed_box, chat_query);
        setListAdapter(adapter);
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
        ChatItem c = (ChatItem) l.getItemAtPosition(position);
        intent.putExtra("display", c.user);
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

    public class ChatItem{
        String user, number;

        ChatItem(String u, String n){
            user = u;
            number = n;
        }
    }

    private class ChatListAdapter extends ArrayAdapter<ChatItem> {
        RelativeLayout feed_bubble;
        private List<ChatItem> feed_list = new ArrayList<>();
        private Context context;
        final ContextWrapper cw;
        File directory;

        public ChatListAdapter(Context context, int resource, ArrayList<ChatItem> arr) {
            super(context, resource, arr);
            this.context = context;
            feed_list = arr;
            cw = new ContextWrapper(context.getApplicationContext());
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        }

        @Override
        public ChatItem getItem(int position) {
            // TODO Auto-generated method stub
            return feed_list.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            FHolder holder;
            View cv = convertView;

            if (cv == null) {
                cv = LayoutInflater.from(context).inflate(R.layout.chat_list_box, parent, false);
                holder = new FHolder();
                holder.fh_user = (TextView) cv.findViewById(R.id.chat_user);
                holder.fh_msg = (TextView) cv.findViewById(R.id.tvMsg);
                holder.fh_time = (TextView) cv.findViewById(R.id.lastMsgTime);
                holder.fh_displayPic = (ImageView) cv.findViewById(R.id.ivProfile1);
                cv.setTag(holder);
            } else {
                holder = (FHolder) cv.getTag();
            }

            ChatItem p = getItem(position);
            String User = p.user;
            String number = p.number;
            String msg = db.getLastMessage(number);
            String time = db.getLastMessageTime(number);
            holder.fh_user.setText(User);
            holder.fh_msg.setText(msg);
            holder.fh_time.setText(time);
            Drawable profilePic;
            profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + number + ".jpg");
            if(profilePic != null)
                holder.fh_displayPic.setImageDrawable(profilePic);

            feed_bubble = (RelativeLayout) cv.findViewById(R.id.chatList_bubble);
            feed_bubble.setBackgroundResource(R.drawable.box);

            return cv;
        }

        /**
         * To cache views of item
         */
        private class FHolder {
            private TextView fh_user, fh_msg, fh_time;
            private ImageView fh_displayPic;

            FHolder() {
            }
        }
    }
}