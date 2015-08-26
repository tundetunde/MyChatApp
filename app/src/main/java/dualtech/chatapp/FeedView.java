package dualtech.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class FeedView extends ListFragment implements View.OnClickListener {
    DbSqlite db;
    Button btn_share;
    EditText et_feed;
    String update;
    static SharedPreferences prefs;
    GoogleCloudMessaging gcm;
    ArrayList e;
    ArrayList<Feed> feed_query = new ArrayList<>();
    FeedAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.feed_list, container, false);
        db = new DbSqlite(getActivity());
        e = (ArrayList)db.getAllContacts();
        prefs = getActivity().getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        btn_share = (Button) v.findViewById(R.id.btnGo);
        btn_share.setOnClickListener(this);
        gcm = GoogleCloudMessaging.getInstance(getActivity().getApplicationContext());
        //gcm = GoogleCloudMessaging.getInstance(context);
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

        return v;
    }

    @Override
    public void onClick(View v) {
        update = String.valueOf(et_feed.getText());
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        if(update!=null) {
            storeUpdate(update);
            String name = getContactName(prefs.getString(ApplicationInit.PROPERTY_MOB_ID, null));
            db.insertFeed(name, update, time);
            feed_query.add(0, new Feed(name,update, time));
        }
        et_feed.setText("");
        adapter.notifyDataSetChanged();
    }

    public void storeUpdate(String  s){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationInit.PROPERTY_STATUS, s);
        editor.apply();
    }

    private String listToJSON(ArrayList x){
        Gson gson = new Gson();
        String jsonCartList = gson.toJson(x);
        return jsonCartList;
    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }

    private void send(final String text){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "Feed");
                    data.putString("GCM_Feed", listToJSON(e));
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

        for (List s : query){feed_query.add(new Feed(getContactName(s.get(0).toString()), s.get(1).toString(), s.get(2).toString()));}
        adapter = new FeedAdapter(getActivity(), R.layout.feed_box, feed_query);
        setListAdapter(adapter);
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
        String status, user, time;

        Feed(String u, String s, String t){
            user = u;
            status = s;
            time = t;
        }
    }

    private class FeedAdapter extends ArrayAdapter<Feed> {
        private List<Feed> feed_list = new ArrayList<>();
        private Context context;
        LinearLayout feed_bubble;

        public FeedAdapter(Context context, int resource, ArrayList<Feed> arr) {
            super(context, resource, arr);
            this.context = context;
            feed_list = arr;
        }

        /**
         * To cache views of item
         */
        private class FHolder {
            private TextView fh_user, fh_time, fh_msg;

            FHolder() {
            }
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
                cv.setTag(holder);
            } else {
                holder = (FHolder) cv.getTag();
            }

            Feed p = getItem(position);
            String Message = p.status;
            String User = p.user;
            String Time = p.time;

            holder.fh_msg.setText(Message.trim());
            holder.fh_user.setText(User);
            holder.fh_time.setText(Time);

            feed_bubble = (LinearLayout) cv.findViewById(R.id.fd_bubble);
            feed_bubble.setBackgroundResource(R.drawable.box);

            return cv;
        }
    }
}
