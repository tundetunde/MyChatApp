package dualtech.chatapp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatView extends AppCompatActivity implements View.OnClickListener {
    static ListView lv;
    static LinearLayout BGlv;
    static Bitmap bmp;
    static public boolean active;

    DbSqlite db;
    Toolbar toolbar;
    Button send;
    EditText editText;
    TextWatcher text_watch;
    String et_msg, ch_contact, ch_display, ch_sender;
    ArrayList chatList;
    ArrayAdapter<ChatDbProvider> adapter;
    SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbox);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        ch_contact = bundle.getString("contact");
        ch_display = bundle.getString("display");
        ch_sender = ApplicationInit.getMobile_number();
        db = new DbSqlite(this);
        initialize();
        loadChat();
    }

    private void initialize() {
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(ch_display);
        //getSupportActionBar().setIcon(R.drawable.ppg);
        lv = (ListView) findViewById(R.id.lvChatHistory);
        lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        send = (Button) findViewById(R.id.send_btn);
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
                if (s.toString().trim().isEmpty()) {
                    send.setVisibility(View.GONE);
                } else {
                    send.setVisibility(View.VISIBLE);
                }
            }
        };
        editText = (EditText) findViewById(R.id.msg_edit);
        editText.addTextChangedListener(text_watch);
        BGlv = (LinearLayout) findViewById(R.id.Lin);
        changeBackground();
    }

    public void refreshChat(){
        chatList.clear();
        chatList = (ArrayList)db.getChatList();
        adapter.notifyDataSetChanged();
    }

    public void changeBackground(){
        if (ApplicationInit.getChatBgURL() != null){
            Bitmap bm = BitmapFactory.decodeFile(ApplicationInit.getChatBgURL());
            BitmapDrawable bmDraw = new BitmapDrawable(this.getResources(), bm);
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                BGlv.setBackgroundDrawable(bmDraw);
            }else{
                BGlv.setBackground(bmDraw);
            }
        }
    }

    private void loadChat() {
        chatList = (ArrayList) db.getChatHistory(ch_contact);
        adapter = new ChatViewAdapter(this, R.layout.message, chatList);
        lv.setAdapter(adapter);

        if(ch_contact.equals(ApplicationInit.getMobile_number())){
            editText.setClickable(false);
            editText.setFocusable(false);
            String msg = "You cannot send messages to your self";
            String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            chatList.add(new ChatDbProvider(msg, 2, d));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(mMessageReceiver, new IntentFilter("chicken"));
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mMessageReceiver);
        active = false;
    }

    private void sendMsg(final String txt, final String dt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ChatView.this);
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "msg");
                    data.putString("GCM_msg", txt);
                    data.putString("GCM_time", dt);
                    data.putString("GCM_contactId", ch_contact);
                    data.putString("GCM_sender", ch_sender);
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            loadChat();

            //do other stuff here
        }
    };

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_view_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_ch_info:
                Intent i = new Intent().setClass(this, ContactProfile.class);
                i.putExtra("number", ch_contact);
                i.putExtra("name", ch_display);
                startActivity(i);
                return true;
            case R.id.action_ch_delete:
                new AlertDialog.Builder(this)
                        .setTitle("Delete history")
                        .setMessage("Do you want to permanently delete this history?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                db.deleteChatHistory(ch_contact);
                                onBackPressed();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();

                return true;
            default:
                item.setOnMenuItemClickListener(new MenuItemListener(this));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent openMain = new Intent("dualtech.chatapp.MAINACTIVITY");
        openMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(openMain);
        finish();
    }

    @Override
    public void onClick(View v) {
        et_msg = String.valueOf(editText.getText());
        if (et_msg != null) {
            String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            chatList.add(new ChatDbProvider(et_msg, 1, d));
            sendMsg(et_msg, d);
            db = new DbSqlite(this);
            db.insertMessage(et_msg, ch_contact, 1);
            editText.setText("");
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Adapter inner class
     */
    private class ChatViewAdapter extends ArrayAdapter<ChatDbProvider> {
        LinearLayout msg_bubble;
        private List<ChatDbProvider> chat_list = new ArrayList<>();
        private Context context;

        public ChatViewAdapter(Context context, int resource, ArrayList<ChatDbProvider> arr) {
            super(context, resource, arr);
            this.context = context;
            chat_list = arr;
        }

        @Override
        public ChatDbProvider getItem(int position) {
            // TODO Auto-generated method stub
            return chat_list.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            BHolder holder;
            View cv = convertView;

            if (cv == null) {
                cv = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
                holder = new BHolder();
                holder.vh_msg = (TextView) cv.findViewById(R.id.msg);
                holder.vh_time = (TextView) cv.findViewById(R.id.msg_time);
                cv.setTag(holder);
            } else {
                holder = (BHolder) cv.getTag();
            }

            ChatDbProvider p = getItem(position);
            String Message = p.msg;
            int sender = p.s_id;
            String Date = p.date;

            holder.vh_msg.setText(Message.trim());
            holder.vh_time.setText(Date);

            msg_bubble = (LinearLayout) cv.findViewById(R.id.ct_bubble);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) msg_bubble.getLayoutParams();

            if (sender == 1) {
                msg_bubble.setBackgroundResource(R.drawable.outgoing);
                params.gravity = Gravity.END;
            } else {
                msg_bubble.setBackgroundResource(R.drawable.incoming);
                params.gravity = Gravity.START;
            }

            return cv;
        }

        /**
         * To cache views of item
         */
        private class BHolder {
            private TextView vh_msg;
            private TextView vh_time;

            BHolder() {
            }
        }
    }
}
