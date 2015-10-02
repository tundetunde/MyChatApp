package dualtech.chatapp;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
public class DeactivateAcct extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "DEACTIVATE";
    Toolbar toolbar;
    DbManager db;
    EditText editText;
    TextView tvInfo;
    Button deactivateButton;
    GoogleCloudMessaging gcm;
    String text = "Please beware that deactivating your account will result in the deletion of all chats\n and will need " +
            "to register again in order to use this app\n" +
            "THIS CANNOT BE UNDONE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deactivate);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.de_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("DEACTIVATE ACCOUNT");

        db = new DbManager(this);
        gcm = GoogleCloudMessaging.getInstance(this);
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
                Log.d(TAG, "NUMBER DEACTIVATED");
            }
        }.execute(null, null, null);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnDeactivate:
                Toast.makeText(this, "Please Wait", Toast.LENGTH_SHORT).show();
                if(ApplicationInit.getMobile_number().equals(editText.getText().toString()))
                    deactivateCompleteDialog();
                else
                    Toast.makeText(this, "Mobile number is incorrect", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private void deactivateCompleteDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Deactivation")
                .setMessage("After deactivation, you will be sent to the Registration Page")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteRequest();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private int msgId() {
        return ApplicationInit.getMsgId();
    }
}
