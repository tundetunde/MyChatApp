package dualtech.chatapp;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ChatView extends Activity implements MessagesFragment.OnFragmentInteractionListener{
    private EditText msgEdit;
    private Button sendBtn;
    private String profileId, profileName, profileNumber;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbox);

        profileId = getIntent().getStringExtra("TUNDE");
        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (Button) findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(msgEdit.getText().toString());
                msgEdit.setText(null);
            }
        });

        Cursor c = getContentResolver().query(Uri.withAppendedPath(DBProvider.CONTENT_URI_PROFILE, profileId), null, null, null, null);
        if (c.moveToFirst()) {
            profileName = c.getString(c.getColumnIndex(DBProvider.COL_NAME));
            profileNumber = c.getString(c.getColumnIndex(DBProvider.COL_PHONENUMBER));
            //actionBar.setTitle(profileName);
        }


        //actionBar.setSubtitle("connecting ...");
        //TextView mb = (TextView) findViewById(R.id.mid);
        //TextView rb = (TextView) findViewById(R.id.rid);

        //mb.setText(mb.getText() + ApplicationInit.getMobile_number());
        //rb.setText(rb.getText() + ApplicationInit.getREGISTRATION_KEY());
    }
    @Override
    protected void onPause() {
        //reset new messages count
        ContentValues values = new ContentValues(1);
        values.put(DBProvider.COL_COUNT, 0);
        getContentResolver().update(Uri.withAppendedPath(DBProvider.CONTENT_URI_PROFILE, profileId), values, null, null);
        super.onPause();
    }

    private void send(final String txt) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                /*try {
                    //ServerUtilities.send(txt, profileEmail);

                    ContentValues values = new ContentValues(2);
                    values.put(DBProvider.COL_MSG, txt);
                    values.put(DBProvider.COL_TO, profileEmail);
                    getContentResolver().insert(DBProvider.CONTENT_URI_MESSAGES, values);

                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }*/
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
    }



    @Override
    public String getProfileEmail() {
        return null;
    }
}
