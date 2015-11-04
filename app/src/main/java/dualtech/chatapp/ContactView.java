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
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContactView extends ListFragment implements View.OnClickListener{
    private static final String TAG = "CONTACT_VIEW";
    static ArrayList<Contact> app_contact;
    static ContactAdapter adapter;
    ProgressBar loader;
    DbManager db;
    ImageButton invite;
    GoogleCloudMessaging gcm;
    private BroadcastReceiver mContactReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshContact();
            Log.d(TAG, "CONTACTS Refreshed");
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list, container, false);
        gcm = GoogleCloudMessaging.getInstance(getActivity());
        db = new DbManager(getActivity());
        app_contact = new ArrayList<>();
        initialize(v);
        setHasOptionsMenu(true);
        return v;
    }

    public void refreshContact(){
        ArrayList<String> appContacts  = (ArrayList<String>) db.getAllContacts();
        app_contact.clear();
        for(String s: appContacts){app_contact.add(new Contact(getContactName(s), s));}

        adapter = new ContactAdapter(getActivity(), R.layout.contactbox, app_contact);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void initialize(final View v){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        invite = (ImageButton) v.findViewById(R.id.btnInvite);
        invite.setOnClickListener(this);
        loader = (ProgressBar) v.findViewById(R.id.contact_load);
        refreshContact();
        loader.setVisibility(View.GONE);
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

    private class ContactAdapter extends ArrayAdapter<Contact>{
        final ContextWrapper cw;
        File directory;
        private Context context;
        private List<Contact> contact_list = new ArrayList<>();

        public ContactAdapter(Context context, int resource, ArrayList<Contact> arr) {
            super(context, resource, arr);
            contact_list = arr;
            this.context = context;
            cw = new ContextWrapper(context.getApplicationContext());
            directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        }

        @Override
        public Contact getItem(int position) {
            return contact_list.get(position);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            FHolder holder;
            View cv = convertView;

            if (cv == null) {
                cv = LayoutInflater.from(context).inflate(R.layout.contactbox, parent, false);
                holder = new FHolder();
                holder.fh_user = (TextView) cv.findViewById(R.id.childTV);
                holder.fh_displayPic = (ImageView) cv.findViewById(R.id.childIMG);
                holder.fh_info = (ImageButton) cv.findViewById(R.id.chdInfo);
                cv.setTag(holder);
            } else {
                holder = (FHolder) cv.getTag();
            }
            Contact p = getItem(position);
            holder.fh_user.setText(p.name);
            Drawable profilePic = Drawable.createFromPath(directory.toString() + "/profile_" + p.number + ".jpg");
            if(profilePic != null)
                holder.fh_displayPic.setImageDrawable(profilePic);
            else
                holder.fh_displayPic.setImageResource(R.drawable.default_pic);
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newActivity = new Intent(v.getContext(), ChatView.class);
                    Contact c = getItem(position);
                    newActivity.putExtra("display", c.name);
                    newActivity.putExtra("contact", c.number);
                    startActivity(newActivity);
                }
            });
            holder.fh_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Contact c = getItem(position);
                    Intent i = new Intent().setClass(getActivity(), ContactProfile.class);
                    i.putExtra("number", c.number);
                    i.putExtra("name", c.name);
                    startActivity(i);
                }
            });
            return cv;
        }

        /**
         * To cache views of item
         */
        private class FHolder {
            private TextView fh_user;
            private ImageView fh_displayPic;
            private ImageButton fh_info;
        }
    }
}
