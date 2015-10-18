package dualtech.chatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
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
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.File;
import java.io.FileNotFoundException;
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
    DbManager db;
    Button invite;
    GoogleCloudMessaging gcm;
    ExContactListAdapter exAdapter;
    List<String> exGroup = Arrays.asList("Friends");
    private BroadcastReceiver mContactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initialize(getView());
            Log.d(TAG, "CONTACTS Refreshed");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list, container, false);
        gcm = GoogleCloudMessaging.getInstance(getActivity());
        db = new DbManager(getActivity());
        exCollection = new HashMap<>();
        app_contact = new ArrayList<>();
        initialize(v);
        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mContactReceiver, new IntentFilter("CONTACT"));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mContactReceiver);
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
                convertView = inflater.inflate(R.layout.child_row, null);
            }
            TextView childText = (TextView) convertView.findViewById(R.id.childTV);
            ImageButton imgBt = (ImageButton) convertView.findViewById(R.id.chdInfo);
            ImageView imgView = (ImageView) convertView.findViewById(R.id.childIMG);
            childText.setText(rowItem.get(childPosition).toString());

            Contact c1 = rowItem.get(childPosition);
            Drawable profilePic;
            profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + c1.number + ".jpg");
            imgView.setImageDrawable(profilePic);

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
            imgBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact c = rowItem.get(childPosition);
                    Intent i = new Intent().setClass(getActivity(), ContactProfile.class);
                    i.putExtra("number", c.number);
                    i.putExtra("name", c.name);
                    startActivity(i);
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
