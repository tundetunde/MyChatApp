package dualtech.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by tunde_000 on 24/10/2015.
 */
public class CreateGroup extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    EditText groupName;
    ImageView ivProfilePic;
    Button btnContact, btnCreateGroup;
    ArrayList<String> groupContacts, contactNumbers;
    String groupNameString;
    ListView lvGroupContacts;
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
        Bundle bundle = getIntent().getExtras();
        groupNameString = "";
        if(bundle != null){
            groupContacts = bundle.getStringArrayList("contactList");
            contactNumbers = bundle.getStringArrayList("contactNumbers");
            groupNameString = bundle.getString("name");
        }
        db = new DbManager(this);
        initialize();
    }

    private void initialize(){
        groupName = (EditText) findViewById(R.id.etGroupName);
        groupName.setText(groupNameString);
        ivProfilePic = (ImageView) findViewById(R.id.ivGroupProfilePic);
        btnContact = (Button) findViewById(R.id.btnAddContacts);
        btnContact.setOnClickListener(this);
        btnCreateGroup = (Button)findViewById(R.id.btnCreateTheGroup);
        btnCreateGroup.setOnClickListener(this);
        lvGroupContacts = (ListView) findViewById(R.id.lvContactGroupPpl);
        if(groupContacts != null){
            final ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1, groupContacts);
            lvGroupContacts.setAdapter(adapter);
        }
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
                    db.insertGroupContacts(group, contactNumbers);
                    db.insertChatList(group, 1);

                    Intent i = new Intent(CreateGroup.this, MainActivity.class);
                    i.putStringArrayListExtra("group", groupContacts);
                    i.putStringArrayListExtra("number", contactNumbers);
                    i.putExtra("display", group);
                    i.putExtra("name", groupNameString);
                    startActivity(i);
                }else {
                    Toast.makeText(getApplicationContext(), "Please enter a Group Name", Toast.LENGTH_SHORT).show();
                }
            break;
            case R.id.btnAddContacts:
                Intent i = new Intent("dualtech.chatapp.GROUPADDCONTACTS");
                i.putExtra("name", String.valueOf(groupName.getText()));
                startActivity(i);
            break;
        }
    }
}
