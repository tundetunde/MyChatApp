package dualtech.chatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FeedView extends ListFragment implements View.OnClickListener {

    private static final String TAG = "FEEDVIEW";
    static ArrayList<Feed> feed_query = new ArrayList<>();
    static FeedAdapter adapter;
    DbManager db;
    ImageButton btn_share;
    EditText et_feed;
    String update;
    GoogleCloudMessaging gcm;
    ArrayList e;
    ArrayList<String> contacts;
    private BroadcastReceiver mFeedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshFeed();
            Log.d(TAG, "FEED Refreshed");
            //do other stuff here
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.feed_list, container, false);
        db = new DbManager(getActivity());
        e = (ArrayList)db.getAllContacts();
        //[removes duplicate numbers]
        Set<String> s = new HashSet<>(db.getAllContacts());
        contacts = new ArrayList<>(s);
        contacts.remove(ApplicationInit.getMobile_number()); //removes device mobile number if exists
        //[End of duplicate removal]
        btn_share = (ImageButton) v.findViewById(R.id.btnGo);
        btn_share.setOnClickListener(this);
        gcm = GoogleCloudMessaging.getInstance(getActivity().getApplicationContext());
        TextWatcher text_watch = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){btn_share.setVisibility(View.GONE);}
                else{btn_share.setVisibility(View.VISIBLE);}
            }
        };

        et_feed = (EditText) v.findViewById(R.id.etUpdate);
        et_feed.addTextChangedListener(text_watch);
        refreshFeed();

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mFeedReceiver, new IntentFilter("FEED"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mFeedReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnGo:
                update = String.valueOf(et_feed.getText());
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                if(update!=null) {
                    ApplicationInit.storeUpdate(update);
                    String name = getContactName(ApplicationInit.getMobile_number());
                    String number = ApplicationInit.getMobile_number();
                    send(update, time);
                    db.insertFeed(name, update, time);
                    feed_query.add(0, new Feed(name,update, time, number, 0));
                }
                et_feed.setText("");
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.action_add).setVisible(false).setEnabled(false);
        menu.findItem(R.id.action_refresh).setVisible(false).setEnabled(false);
    }

    private String listToJSON(ArrayList x){
        Gson gson = new Gson();
        return gson.toJson(x);
    }

    private int msgId() {
        return ApplicationInit.getMsgId();
    }

    private void send(final String text, final String time){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "Feed");
                    data.putString("GCM_FROM", ApplicationInit.getMobile_number());
                    data.putString("GCM_time", time);
                    data.putString("Contacts", listToJSON(contacts));
                    data.putString("msg", text);
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("FEED", "MESSAGE SENT");
            }
        }.execute(null, null, null);
    }

    public void refreshFeed(){
        List<List> query = db.getAllFeed();
        Collections.reverse(query);//reverse the result
        feed_query.clear();

        for (List s : query) {
            feed_query.add(new Feed(getContactName(s.get(0).toString()), s.get(1).toString(), s.get(2).toString(), s.get(0).toString(), Integer.parseInt(s.get(3).toString())));
        }
        adapter = new FeedAdapter(getActivity(), R.layout.feed_box, feed_query);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //On list click
    }

    public class Feed{
        String status, user, time, number;
        int picture;

        Feed(String u, String s, String t, String n, int p){
            user = u;
            status = s;
            time = t;
            number = n;
            picture = p;
        }
    }

    private class FeedAdapter extends ArrayAdapter<Feed> {
        final ContextWrapper cw;
        RelativeLayout feed_bubble;
        File directory;
        private List<Feed> feed_list = new ArrayList<>();
        private Context context;

        public FeedAdapter(Context context, int resource, ArrayList<Feed> arr) {
            super(context, resource, arr);
            this.context = context;
            feed_list = arr;
            cw = new ContextWrapper(context.getApplicationContext());
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        }

        @Override
        public Feed getItem(int position) {
            return feed_list.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            FHolder holder;
            View cv;
            Feed p = getItem(position);
            String Message = p.status;
            String User = p.user;
            String Time = p.time;
            String number = p.number;
            int picture = p.picture;
            String t = "Status has changed to: '" + Message.trim() + "'";
            String d = "Display Picture Changed";

            if(picture == 0){
                    cv = LayoutInflater.from(context).inflate(R.layout.feed_box, parent, false);
                    holder = new FHolder();
                    holder.fh_msg = (TextView) cv.findViewById(R.id.fd_msg);
                    holder.fh_time = (TextView) cv.findViewById(R.id.fd_time);
                    holder.fh_user = (TextView) cv.findViewById(R.id.fd_user);
                    holder.fh_smallDisplayPic = (ImageView) cv.findViewById(R.id.imageView);
                    cv.setTag(holder);

                holder.fh_msg.setText(t);
                holder.fh_user.setText(User);
                holder.fh_time.setText(Time);
                Drawable profilePic;
                profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + number + ".jpg");
                if(profilePic != null)
                    holder.fh_smallDisplayPic.setImageDrawable(profilePic);
                else
                    holder.fh_smallDisplayPic.setImageResource(R.drawable.default_pic);
                feed_bubble = (RelativeLayout) cv.findViewById(R.id.fd_bubble);
                feed_bubble.setBackgroundResource(R.drawable.box);
            }else{
                    cv = LayoutInflater.from(context).inflate(R.layout.feed_box_display_pic, parent, false);
                    holder = new FHolder();
                    holder.fh_msg = (TextView) cv.findViewById(R.id.tvMessageStatus);
                    holder.fh_time = (TextView) cv.findViewById(R.id.tvTime);
                    holder.fh_user = (TextView) cv.findViewById(R.id.tvContactName);
                    holder.fh_smallDisplayPic = (ImageView) cv.findViewById(R.id.ivSmallDisplayPic);
                    holder.fh_LargeDisplayPic = (ImageView) cv.findViewById(R.id.ivBigImage);
                    cv.setTag(holder);

                holder.fh_msg.setText(d);
                holder.fh_user.setText(User);
                holder.fh_time.setText(Time);
                Drawable profilePic;
                profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + number + ".jpg");
                if(profilePic != null){
                    holder.fh_smallDisplayPic.setImageDrawable(profilePic);
                    holder.fh_LargeDisplayPic.setImageDrawable(profilePic);
                }else {
                    holder.fh_smallDisplayPic.setImageResource(R.drawable.default_pic);
                    holder.fh_LargeDisplayPic.setImageResource(R.drawable.default_pic);
                }

                feed_bubble = (RelativeLayout) cv.findViewById(R.id.rlBubbble);
                feed_bubble.setBackgroundResource(R.drawable.box);
            }
            return cv;
        }

        /**
         * To cache views of item
         */
        private class FHolder {
            private TextView fh_user, fh_time, fh_msg;
            private ImageView fh_smallDisplayPic, fh_LargeDisplayPic;
        }
    }
}
