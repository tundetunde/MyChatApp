package dualtech.chatapp;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class ChatView extends Activity implements View.OnClickListener{
    DbSqlite db;
    ListView lv;
    Button send;
    EditText editText;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbox);
        db = new DbSqlite(this);
        ArrayList<String> chatList = (ArrayList)db.getChatHistory("Tunde", "Jesz");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, chatList);
        initalize(adapter);
        //actionBar.setSubtitle("connecting ...");
        //TextView mb = (TextView) findViewById(R.id.mid);
        //TextView rb = (TextView) findViewById(R.id.rid);

        //mb.setText(mb.getText() + ApplicationInit.getMobile_number());
        //rb.setText(rb.getText() + ApplicationInit.getREGISTRATION_KEY());
    }

    private void initalize(ArrayAdapter<String> adapter){
        lv = (ListView)findViewById(R.id.lvChatHistory);
        lv.setAdapter(adapter);
        send = (Button)findViewById(R.id.send_btn);
        send.setOnClickListener(this);
        editText = (EditText)findViewById(R.id.msg_edit);
    }

    /*private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                *//*try {
                    //ServerUtilities.send(txt, profileEmail);

                    ContentValues values = new ContentValues(2);
                    values.put(DBProvider.COL_MSG, txt);
                    values.put(DBProvider.COL_TO, profileEmail);
                    getContentResolver().insert(DBProvider.CONTENT_URI_MESSAGES, values);

                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }*//*
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

    }
}
