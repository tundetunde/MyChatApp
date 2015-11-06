package dualtech.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tunde_000 on 24/10/2015.
 */
public class CreateGroup extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    EditText groupName;
    ImageView ivProfilePic;
    ImageButton btnCreateGroup;
    ArrayList<String> contactNumbers;
    String groupNameString;
    DbManager db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group);
        toolbar = (Toolbar) findViewById(R.id.create_group_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");
        groupNameString = "";
        db = new DbManager(this);
        initialize();
    }

    private void initialize(){
        groupName = (EditText) findViewById(R.id.etGroupName);
        groupName.setText(groupNameString);
        ivProfilePic = (ImageView) findViewById(R.id.ivGroupProfilePic);
        btnCreateGroup = (ImageButton) findViewById(R.id.btnCreateTheGroup);
        btnCreateGroup.setOnClickListener(this);
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
            default:
                item.setOnMenuItemClickListener(new MenuItemListener(this));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCreateTheGroup:
                String group = String.valueOf(groupName.getText());
                if(!group.equals("")){
                    String groupId = ApplicationInit.generateGroupId();
                    db.insertGroupContacts(group, contactNumbers, groupId);
                    //db.insertChatList(group, 1);
                    db.insertGroupMessage("You have created this group", "", 0, group);
                    //sendGroupContacts(group, contactNumbers, rand);
                    Intent i = new Intent(CreateGroup.this, MainActivity.class);
                    startActivity(i);
                }else {
                    Toast.makeText(getApplicationContext(), "Please enter a Group Name", Toast.LENGTH_SHORT).show();
                }
            break;
        }
    }

    private int msgId() {
        return  ApplicationInit.getMsgId();
    }

    public void sendGroupContacts(final String groupName,final ArrayList<String> groupContacts, final int groupId){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(CreateGroup.this);
                try {
                    String json = new Gson().toJson(groupContacts);
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "NewGroup");
                    data.putString("groupName", groupName);
                    data.putString("groupList", json);
                    data.putString("groupId", String.valueOf(groupId));
                    data.putString("creator", String.valueOf(ApplicationInit.getMobile_number()));
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Message could not be sent";
                }

                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {}
        }.execute(null, null, null);
    }
}
