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
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.google.android.gms.internal.zzhl.runOnUiThread;

public class FeedView extends ListFragment implements View.OnClickListener {

    private static final String TAG = "FEEDVIEW";
    static ArrayList<Feed> feed_query = new ArrayList<>();

    static FeedAdapter adapter;
    DbSqlite db;
    Button btn_share;
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
        db = new DbSqlite(getActivity());
        e = (ArrayList)db.getAllContacts();
        //[removes duplicate numbers]
        Set<String> s = new HashSet<>(db.getAllContacts());
        contacts = new ArrayList<>(s);
        contacts.remove(ApplicationInit.getMobile_number()); //removes device mobile number if exists
        //[End of duplicate removal]
        btn_share = (Button) v.findViewById(R.id.btnGo);
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

        //getActivity().registerReceiver(feedBroadcastReceiver, new IntentFilter("REFRESH_FEED"));

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mFeedReceiver, new IntentFilter("chicken"));
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
                    feed_query.add(0, new Feed(name,update, time, number));
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
        String jsonCartList = gson.toJson(x);
        return jsonCartList;
    }

    private int msgId() {
        int id = ApplicationInit.getMsgId();
        return id;
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
            feed_query.add(new Feed(getContactName(s.get(0).toString()), s.get(1).toString(), s.get(2).toString(), s.get(0).toString()));
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

        Feed(String u, String s, String t, String n){
            user = u;
            status = s;
            time = t;
            number = n;
        }
    }

    private class FeedAdapter extends ArrayAdapter<Feed> {
        RelativeLayout feed_bubble;
        private List<Feed> feed_list = new ArrayList<>();
        private Context context;
        final ContextWrapper cw;
        File directory;

        public FeedAdapter(Context context, int resource, ArrayList<Feed> arr) {
            super(context, resource, arr);
            this.context = context;
            feed_list = arr;
            cw = new ContextWrapper(context.getApplicationContext());
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        }

        @Override
        public Feed getItem(int position) {
            // TODO Auto-generated method stub
            return feed_list.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            FHolder holder;
            View cv = convertView;

            if (cv == null) {
                cv = LayoutInflater.from(context).inflate(R.layout.feed_box, parent, false);
                holder = new FHolder();
                holder.fh_msg = (TextView) cv.findViewById(R.id.fd_msg);
                holder.fh_time = (TextView) cv.findViewById(R.id.fd_time);
                holder.fh_user = (TextView) cv.findViewById(R.id.fd_user);
                holder.fh_displayPic = (ImageView) cv.findViewById(R.id.imageView);
                cv.setTag(holder);
            } else {
                holder = (FHolder) cv.getTag();
            }

            Feed p = getItem(position);
            String Message = p.status;
            String User = p.user;
            String Time = p.time;
            String number = p.number;

            holder.fh_msg.setText(Message.trim());
            holder.fh_user.setText(User);
            holder.fh_time.setText(Time);
            Drawable profilePic = null;
            profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + number + ".jpg");
            if(profilePic != null)
                holder.fh_displayPic.setImageDrawable(profilePic);

            feed_bubble = (RelativeLayout) cv.findViewById(R.id.fd_bubble);
            feed_bubble.setBackgroundResource(R.drawable.box);

            return cv;
        }

        /**
         * To cache views of item
         */
        private class FHolder {
            private TextView fh_user, fh_time, fh_msg;
            private ImageView fh_displayPic;

            FHolder() {
            }
        }
    }
}
