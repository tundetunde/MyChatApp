package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatView extends Activity implements View.OnClickListener{
    DbSqlite db;
    ListView lv;
    Button send;
    EditText editText;
    TextView tv_header;
    TextWatcher text_watch;
    String et_msg;
    String ch_contact;
    ArrayList chatList;
    ArrayAdapter<chatDbProvider> adapter;
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbox);
        Bundle bundle = getIntent().getExtras();
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        ch_contact = bundle.getString("contact");
        db = new DbSqlite(this);
        gcm = GoogleCloudMessaging.getInstance(ChatView.this);
        initialize();
        loadChat();
    }

    private void initialize(){
        lv = (ListView)findViewById(R.id.lvChatHistory);

        /*lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lv.setSelection(adapter.getCount() - 1);
            }
        });*/

        tv_header = (TextView) findViewById(R.id.tv_header);
        tv_header.setText(ch_contact);
        send = (Button)findViewById(R.id.send_btn);
        send.setOnClickListener(this);
        text_watch = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){send.setVisibility(View.GONE);}
                else{send.setVisibility(View.VISIBLE);}
            }
        };
        editText = (EditText)findViewById(R.id.msg_edit);
        editText.addTextChangedListener(text_watch);
    }

    private void loadChat(){
        chatList = (ArrayList)db.getChatHistory(ch_contact);
        adapter = new ChatViewAdapter(this, R.layout.message,chatList);
        lv.setAdapter(adapter);
    }

    private void sendMsg(final String txt, final String dt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("GCM_msg", txt);
                    data.putString("GCM_time", dt);
                    data.putString("GCM_contactId", ch_contact);
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }

    public void receivedMsg(Bundle data) {
        int sender  = 0;
        String message = data.getString("GCM_msg");
        String contact = data.getString("GCM_contactId");
        String time = data.getString("GCM_time");

        adapter.add(new chatDbProvider(message, sender, time));
        db.insertMessage(message, contact, sender);
    }

    public void reload(){
        loadChat();
    }

    @Override
    public void onClick(View v) {
        et_msg = String.valueOf(editText.getText());
        if(et_msg != null){
            String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            chatList.add(new chatDbProvider(et_msg, 1, d));
            sendMsg(et_msg, d);
            db.insertMessage(et_msg, ch_contact, 1);
            editText.setText("");
            adapter.notifyDataSetChanged();
        }
    }


    /** Adapter inner class
     * */
    private class ChatViewAdapter extends ArrayAdapter<chatDbProvider> {
        private List<chatDbProvider> chat_list = new ArrayList<> ();
        private Context context;
        LinearLayout msg_bubble;

        public ChatViewAdapter(Context context, int resource, ArrayList<chatDbProvider> arr) {
            super(context, resource, arr);
            this.context = context;
            chat_list = arr;
        }

        /** To cache views of item */
        private class BHolder {
            private TextView vh_msg;
            private TextView vh_time;

            BHolder() {}
        }

        @Override
        public chatDbProvider getItem(int position) {
            // TODO Auto-generated method stub
            return chat_list.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            BHolder holder;
            View cv = convertView;

            if(cv == null)
            {
                cv = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
                holder = new BHolder();
                holder.vh_msg = (TextView)cv.findViewById(R.id.msg);
                holder.vh_time = (TextView)cv.findViewById(R.id.msg_time);
                cv.setTag(holder);
            }
            else{
                holder = (BHolder)cv.getTag();
            }

            chatDbProvider p = getItem(position);
            String Message = p.msg;
            int sender  = p.s_id;
            String Date = p.date;

            holder.vh_msg.setText(Message.trim());
            holder.vh_time.setText(Date);

            msg_bubble = (LinearLayout) cv.findViewById(R.id.ct_bubble);
            LinearLayout.LayoutParams params  = (LinearLayout.LayoutParams) msg_bubble.getLayoutParams();

            if(sender == 1)
            {
                msg_bubble.setBackgroundResource(R.drawable.outgoing);
                params.gravity = Gravity.END;
            }
            else
            {
                msg_bubble.setBackgroundResource(R.drawable.incoming);
                params.gravity = Gravity.START;
            }

            return cv;
        }
    }


}
