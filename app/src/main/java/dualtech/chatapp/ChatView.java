package dualtech.chatapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbox);
        Bundle bundle = getIntent().getExtras();
        ch_contact = bundle.getString("contact");
        db = new DbSqlite(this);
        loadChat();

        //actionBar.setSubtitle("connecting ...");
        //TextView mb = (TextView) findViewById(R.id.mid);
        //TextView rb = (TextView) findViewById(R.id.rid);

        //mb.setText(mb.getText() + ApplicationInit.getMobile_number());
        //rb.setText(rb.getText() + ApplicationInit.getREGISTRATION_KEY());
    }

    private void initalize(){
        lv = (ListView)findViewById(R.id.lvChatHistory);
        lv.setAdapter(adapter);

        lv.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                lv.setSelection(adapter.getCount() - 1);
            }
        });

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
        adapter = new ChatViewAdapter(this, R.layout.message);
        for(Object c : chatList){
            adapter.add((chatDbProvider) c);
        }
        initalize();
    }

/**    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    //ServerUtilities.send(txt, profileEmail);

                    ContentValues values = new ContentValues(2);
                    values.put(DBProvider.COL_MSG, txt);
                    values.put(DBProvider.COL_TO, profileEmail);
                    getContentResolver().insert(DBProvider.CONTENT_URI_MESSAGES, values);

                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                ContentValues values = new ContentValues(2);
                values.put(DBProvider.COL_MSG, txt);
                values.put(DBProvider.COL_TO, profileName);
                getContentResolver().insert(DBProvider.CONTENT_URI_MESSAGES, values);
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
    }*/

    @Override
    public void onClick(View v) {
        et_msg = String.valueOf(editText.getText());
        if(et_msg != null){
            String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            adapter.add(new chatDbProvider(et_msg, 1, d));
            db.insertMessage(et_msg, "Me", ch_contact, ch_contact, 1);
            editText.setText("");
        }
    }


    /** Adapter inner class
     * */
    private class ChatViewAdapter extends ArrayAdapter<chatDbProvider> {
        private List<chatDbProvider> chat_list = new ArrayList<> ();
        private Context context;
        LinearLayout v;

        public ChatViewAdapter(Context context, int resource) {
            super(context, resource);
            this.context = context;
        }

        @Override
        public void add(chatDbProvider s) {
            // TODO Auto-generated method stub
            chat_list.add(s);
            super.add(s);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return chat_list.size();
        }

        /** To cache views of item */
        private class ViewHolder {
            private TextView vh_msg;
            private TextView vh_time;

            ViewHolder() {}
        }

        @Override
        public chatDbProvider getItem(int position) {
            // TODO Auto-generated method stub
            return chat_list.get(position);
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null)
            {
                convertView = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
                holder = new ViewHolder();
                v = (LinearLayout) convertView.findViewById(R.id.view);
                holder.vh_msg = (TextView)convertView.findViewById(R.id.msg);
                holder.vh_time = (TextView)convertView.findViewById(R.id.msg_time);
                convertView.setTag(holder);
            }
            else{
                holder = (ViewHolder)convertView.getTag();
            }

            chatDbProvider p = getItem(position);
            String Message = p.msg;
            int from_me  = p.s_id;
            String Date = p.date;

            holder.vh_msg.setText(Message.trim());
            holder.vh_msg.setTextSize(15);
            holder.vh_time.setText(Date);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            if(from_me == 1)
            {
                v.setBackgroundResource(R.drawable.outgoing);
                params.gravity = Gravity.END;

            }
            else
            {
                v.setBackgroundResource(R.drawable.incoming);
                params.gravity = Gravity.START;
            }

            v.setLayoutParams(params);

            return convertView;
        }
    }


}
