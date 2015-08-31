package dualtech.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("GCM_msg");
        String type = data.getString("Type");
        String contact = data.getString("GCM_contactId");
        String sender = data.getString("GCM_sender");
        String time = data.getString("GCM_time");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Contact: " + contact);
        Log.d(TAG, "Time: " + time);

        switch (type){
            case "msg":
                DbSqlite db = new DbSqlite(this);
                db.insertMessage(message, sender, 0);
                break;
            case "Feed":
                String text = data.getString("msg");
                String user = data.getString("GCM_FROM");
                Log.d("CHECK FEED", user);
                DbSqlite db1 = new DbSqlite(this);
                db1.insertFeed(user, text, time);
                break;
            case "Contact":
                String listString = data.getString("Contacts");
                ArrayList list;
                Gson gson = new Gson();
                TypeToken<List<String>> token = new TypeToken<List<String>>() {};
                list = gson.fromJson(listString, token.getType());
                System.out.println("Returned List: " + list);
                DbSqlite db2 = new DbSqlite(this);
                db2.insertContacts(list);
                ArrayList<Contact> lista = new ArrayList<>();
                for (Object s : list){lista.add(new Contact(getContactName(s.toString()), s.toString()));}
                System.out.println("LIST RETURNED");
                ContactView.updateList(lista);
                break;
            case "MainContact":
                String theList = data.getString("MainContact");
                ArrayList listOfContacts;
                Gson gsons = new Gson();
                TypeToken<List<String>> token1 = new TypeToken<List<String>>() {};
                listOfContacts = gsons.fromJson(theList, token1.getType());
                System.out.println("Returned List: " + listOfContacts);
                DbSqlite db3 = new DbSqlite(this);
                db3.insertContacts(listOfContacts);
                break;
        }
        if(type.equals("msg")){
            //insert into db

        }else if(type.equals("Feed")){


            //Add the string to the feed HERE!!!!!!!!!!!!!!!!!!!!!!!
        }else if(type.equals("Contacts")){

        }

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        if(type.equals("msg")){
            sendNotification(message, sender);
        }

    }
    // [END receive_message]

    public String getContactName(String num){
        String name = num;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(num));
        Cursor cursor = getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if(cursor != null){
            if(cursor.moveToFirst()){
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }
        return name;
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String contactID) {
        Intent intent = new Intent(this, ChatView.class);
        String display = getContactName(contactID);
        intent.putExtra("display", display);
        intent.putExtra("contact", contactID);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(display)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
