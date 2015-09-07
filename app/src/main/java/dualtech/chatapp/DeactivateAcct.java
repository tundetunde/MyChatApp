package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by tunde_000 on 03/09/2015.
 */
public class DeactivateAcct extends Activity implements View.OnClickListener{
    DbSqlite db;
    EditText editText;
    TextView tvInfo;
    Button deactivateButton;
    GoogleCloudMessaging gcm;
    static SharedPreferences prefs;
    String text = "Please beaware that deactivating your account will result in the deletion of all chats\n and will need " +
            "to register again in order to use this app";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deactivate);
        db = new DbSqlite(this);
        gcm = GoogleCloudMessaging.getInstance(this);
        prefs = this.getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        initialize();
    }

    private void initialize(){
        editText = (EditText)findViewById(R.id.etNumber);
        tvInfo = (TextView)findViewById(R.id.tvInfoDeactivate);
        tvInfo.setText(text);
        deactivateButton = (Button)findViewById(R.id.btnDeactivate);
        deactivateButton.setOnClickListener(this);
    }

    private void deleteRequest(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "Deactivate");
                    data.putString("number", ApplicationInit.getMobile_number());
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("DEACTIVATE", "NUMBER DEACTIVATED");
            }
        }.execute(null, null, null);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnDeactivate:
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT);
                if(ApplicationInit.getMobile_number().equals(editText.getText().toString()))
                    deleteRequest();
                else
                    Toast.makeText(this, "Mobile number is incorrect", Toast.LENGTH_SHORT);
                break;

        }
    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }
}
