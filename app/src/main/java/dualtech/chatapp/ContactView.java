package dualtech.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactView extends Fragment implements View.OnClickListener{
    private static final String TAG = "CONTACTVIEW";
    static ArrayList<Contact> app_contact;
    Map<String,ArrayList<Contact>> exCollection;
    ExpandableListView exListView;
    ProgressBar loader;
    DbSqlite db;
    Button invite;
    SharedPreferences prefs;
    GoogleCloudMessaging gcm;
    ExContactListAdapter exAdapter;
    List<String> exGroup = Arrays.asList("Friends");


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list, container, false);
        gcm = GoogleCloudMessaging.getInstance(getActivity());
        db = new DbSqlite(getActivity());
        exCollection = new HashMap<>();
        app_contact = new ArrayList<>();
        initialize(v);
        setHasOptionsMenu(true);
        return v;
    }

    private void initialize(final View v){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        invite = (Button) v.findViewById(R.id.btnInvite);
        invite.setOnClickListener(this);
        loader = (ProgressBar) v.findViewById(R.id.contact_load);
        exListView = (ExpandableListView) v.findViewById(R.id.expandLV);

        exListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                exListView.expandGroup(groupPosition);
                return false;
            }
        });

        getChildData();
        exCollection.put(exGroup.get(0), app_contact);

        exAdapter = new ExContactListAdapter(exGroup, exCollection);
        exAdapter.setInflater((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE), getActivity());
        exListView.setAdapter(exAdapter);

        loader.setVisibility(View.GONE);
    }

    public void getChildData(){
        ArrayList<String> appContacts  = (ArrayList<String>) db.getAllContacts();

        for(String s: appContacts){app_contact.add(new Contact(getContactName(s), s));}

        System.out.println("APP_CONTACT: " + app_contact);
    }

    public String getContactName(String num){
        String name = num;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return name;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        menu.findItem(R.id.action_add).setVisible(false).setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnInvite:
                Intent i = new Intent().setClass(getActivity(), InviteContact.class);
                startActivity(i);
        }
    }

    public class ExContactListAdapter extends BaseExpandableListAdapter{
        Context baseContext;
        LayoutInflater inflater;
        List<String> parentItem;
        ArrayList<Contact> rowItem;
        Map<String, ArrayList<Contact>> childItem;

        public ExContactListAdapter(List<String> l, Map<String, ArrayList<Contact>> col){
            parentItem = l;
            childItem = col;
        }

        public void setInflater(LayoutInflater in, Context c){
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
            if ( getChildrenCount( groupPosition ) == 0 && groupPosition!=0) {
                CheckedTextView ct = (CheckedTextView) convertView.findViewById(R.id.checkTV);
                //
                // ct.setVisibility(View.GONE);
            }else {
                lv.expandGroup(groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            rowItem = childItem.get(parentItem.get(groupPosition));
            if(convertView == null){
                convertView = inflater.inflate(R.layout.child_row, null);
            }
            TextView childText = (TextView) convertView.findViewById(R.id.childTV);
            CheckBox childChkBox = (CheckBox) convertView.findViewById(R.id.chkRequest);
            childText.setText(rowItem.get(childPosition).toString());

            if(groupPosition == 0){
                childChkBox.setVisibility(View.GONE);
            }else if(groupPosition == 2){
                childChkBox.setVisibility(View.GONE);
            }else if(groupPosition == 1 && (db.checkRequest(rowItem.get(childPosition).number) || db.checkContList(rowItem.get(childPosition).number))){
                childChkBox.setVisibility(View.GONE);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
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
            });
            childChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isChecked() && groupPosition == 1) {
                        try {
                            String num = rowItem.get(childPosition).number;
                            db.insertRequest(num, 0);
                            String id = String.valueOf(ApplicationInit.getMsgId());
                            Bundle data = new Bundle();
                            data.putString("Type", "Request");
                            data.putString("Phone", num);
                            data.putString("AddPhone", ApplicationInit.getMobile_number());
                            gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                            Toast.makeText(getActivity().getApplicationContext(), "Checked!!!", Toast.LENGTH_SHORT).show();
                            //childChkBox.setVisibility(View.GONE);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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
