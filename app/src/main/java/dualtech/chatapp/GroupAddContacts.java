package dualtech.chatapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tunde_000 on 24/10/2015.
 */
public class GroupAddContacts extends AppCompatActivity {
    Toolbar toolbar;
    ArrayList<Contact> app_contact;
    ArrayList<String> groupContacts, contactNumbers;
    Map<String,ArrayList<Contact>> exCollection;
    ExpandableListView exListView;
    List<String> exGroup = Arrays.asList("Friends");
    GroupAddContacts.ExContactListAdapter exAdapter;
    DbManager db;
    Button button;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_addcontacts);
        toolbar = (Toolbar) findViewById(R.id.create_group_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Contacts To Group");
        exCollection = new HashMap<>();
        app_contact = new ArrayList<>();
        groupContacts = new ArrayList<>();
        contactNumbers = new ArrayList<>();
        db = new DbManager(this);
        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("name");
        initialize();
    }

    private void getChildData(){
        ArrayList<String> appContacts  = (ArrayList<String>) db.getAllContacts();

        for(String s: appContacts){app_contact.add(new Contact(getContactName(s), s));
        }

        System.out.println("APP_CONTACT: " + app_contact);
    }

    private void initialize(){
        exListView = (ExpandableListView) findViewById(R.id.elvGroupContacts);

        exListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                exListView.expandGroup(groupPosition);
                return false;
            }
        });

        getChildData();
        exCollection.put(exGroup.get(0), app_contact);

        exAdapter = new GroupAddContacts.ExContactListAdapter(exGroup, exCollection);
        exAdapter.setInflater((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE), this);
        exListView.setAdapter(exAdapter);
        button = (Button)findViewById(R.id.buttonAddContactGroup);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent("dualtech.chatapp.CREATEGROUP");
                for(Contact x : app_contact){
                    if(x.isChecked()){
                        Log.d("groupContact", x.name);
                        groupContacts.add(x.name);
                        contactNumbers.add(x.number);
                    }
                    //groupContacts.add(x.name);
                }
                    //Log.d("groupContact", x);
                i.putStringArrayListExtra("contactList", groupContacts);
                i.putStringArrayListExtra("contactNumbers", contactNumbers);
                i.putExtra("name", name);
                startActivity(i);
            }

        });
    }

    private String getContactName(String num){
        String name = num;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        Cursor cursor = this.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return name;
    }

    public class ExContactListAdapter extends BaseExpandableListAdapter {
        ContextWrapper cw;
        Context baseContext;
        LayoutInflater inflater;
        List<String> parentItem;
        ArrayList<Contact> rowItem;
        Map<String, ArrayList<Contact>> childItem;
        File directory;

        public ExContactListAdapter(List<String> l, Map<String, ArrayList<Contact>> col){
            parentItem = l;
            childItem = col;
            cw = null;
        }

        public void setInflater(LayoutInflater in, Context c){
            cw = new ContextWrapper(c.getApplicationContext());
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            inflater = in;
            baseContext = c;
        }

        @Override
        public int getGroupCount() {
            return parentItem.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childItem.get(parentItem.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = inflater.inflate(R.layout.group_row, null);
                ((CheckedTextView)convertView).setText(parentItem.get(groupPosition));
                ((CheckedTextView)convertView).setChecked(isExpanded);
            }
            ExpandableListView lv = (ExpandableListView) parent;

            lv.expandGroup(groupPosition);

            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            rowItem = childItem.get(parentItem.get(groupPosition));
            if(convertView == null){
                convertView = inflater.inflate(R.layout.invite_group_childrow, null);
            }
            TextView childText = (TextView) convertView.findViewById(R.id.childTV);
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkboxGroup);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.childIMG);
            childText.setText(rowItem.get(childPosition).toString());

            Contact c1 = rowItem.get(childPosition);
            Drawable profilePic;
            profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + c1.number + ".jpg");
            if(profilePic != null)
                imgView.setImageDrawable(profilePic);
            else
                imgView.setImageResource(R.drawable.default_pic);

            /*convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (groupPosition == 0) {
                        Intent newActivity = new Intent(v.getContext(), ChatView.class);
                    Contact c = rowItem.get(childPosition);
                    newActivity.putExtra("display", c.name);
                    newActivity.putExtra("contact", c.number);
                    startActivity(newActivity);
                }
                }
            });*/
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact c = rowItem.get(childPosition);
                    for (Contact x : app_contact) {
                        if (x.name == c.name) {
                            x.toggleChecked();
                        }
                    }
                    /*if (checkBox.isSelected()) {
                        groupContacts.add(c.name);
                    } else {
                        if (groupContacts.isEmpty() == false) {
                            groupContacts.remove(c.name);
                            *//*for (String x : groupContacts) {
                                groupContacts.remove(c.name);
                            }*//*
                        }

                    }*/
                }
            });

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }
}
