package dualtech.chatapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatList extends ListFragment implements View.OnClickListener{
    static ArrayList<ChatItem> chat_query = new ArrayList<>();
    DbManager db;
    ArrayList<Contact> chatName;
    ChatListAdapter adapter;
    GoogleCloudMessaging gcm;
    ImageButton btnCreateGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_list, container, false);

        gcm = GoogleCloudMessaging.getInstance(getActivity().getApplicationContext());
        db = new DbManager(getActivity());
        chatName = new ArrayList<>();
        setHasOptionsMenu(true);
        refreshChatList();
        btnCreateGroup = (ImageButton) v.findViewById(R.id.btnCreateGroup);
        btnCreateGroup.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreateGroup:
                Intent i = new Intent().setClass(getActivity(), CreateGroup.class);
                startActivity(i);
        }
    }

    public void refreshChatList(){
        List<List<String>> query = db.getChatList();
        chat_query.clear();
        for (List<String> s : query) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            try {
                String c = s.get(0);
                if (s.get(1).equals("0")) {
                    chat_query.add(new ChatItem(getContactName(c), c, dateFormat.parse(db.getLastMessageTime(c)), "0"));
                }else{
                    chat_query.add(new ChatItem(db.getGroupName(c), c, dateFormat.parse(db.getGrpMessageTime(c)), "1"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(chat_query, Collections.reverseOrder(new Comparator<ChatItem>() {
            @Override
            public int compare(ChatItem p1, ChatItem p2) {
                return p1.date.compareTo(p2.date);
            }
        }));
        adapter = new ChatListAdapter(getActivity(), R.layout.chat_list_box, chat_query);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshChatList();
    }

    private int msgId() {
        return ApplicationInit.getMsgId();
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
        intent.putExtra("type", c.type);
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
                data.putString("Phone", ApplicationInit.getREGISTRATION_KEY());
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
        List<List<String>> chatlist = db.getChatList();

        for(List<String> contact : chatlist){
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File mypath = new File(directory, "profile_" + contact + ".jpg");
            if(!mypath.exists()){
                returnedList.add(contact.get(0));
            }
        }
        return returnedList;
    }

    public class ChatItem{
        String user, number, type;
        Date date;

        ChatItem(String u, String n, Date d, String t){
            user = u;
            number = n;
            date = d;
            type = t;
        }
    }

    private class ChatListAdapter extends ArrayAdapter<ChatItem> {
        final ContextWrapper cw;
        RelativeLayout chat_list_bubble;
        File directory;
        private List<ChatItem> chat_list = new ArrayList<>();
        private Context context;

        public ChatListAdapter(Context context, int resource, ArrayList<ChatItem> arr) {
            super(context, resource, arr);
            this.context = context;
            chat_list = arr;
            cw = new ContextWrapper(context.getApplicationContext());
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        }

        @Override
        public ChatItem getItem(int position) {
            return chat_list.get(position);
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
            if(p.type.equals("1")){
                time = db.getGrpMessageTime(number);
            }
            holder.fh_user.setText(User);
            holder.fh_msg.setText(msg);
            holder.fh_time.setText(time);
            Drawable profilePic;
            profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + number + ".jpg");
            if(profilePic != null)
                holder.fh_displayPic.setImageDrawable(profilePic);
            else
                holder.fh_displayPic.setImageResource(R.drawable.default_pic);

            chat_list_bubble = (RelativeLayout) cv.findViewById(R.id.chatList_bubble);
            chat_list_bubble.setBackgroundResource(R.drawable.box);

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