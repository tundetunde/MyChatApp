package dualtech.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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
        int sender = 0;
        String message = data.getString("GCM_msg");
        String type = data.getString("Type");
        String contact = data.getString("GCM_contactId");
        String time = data.getString("GCM_time");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Contact: " + contact);
        Log.d(TAG, "Time: " + time);

        Log.d(TAG, message);
        if(type.equals("msg")){
            //insert into db
            DbSqlite db = new DbSqlite(this);
            db.insertMessage(message, contact, sender);
        }else if(type.equals("Feed")){
            String text = data.getString("msg");

            //Add the string to the feed HERE!!!!!!!!!!!!!!!!!!!!!!!
        }else if(type.equals("Contacts")){
            String listString = data.getString("Contacts");
            ArrayList list = new ArrayList();
            Gson gson = new Gson();
            TypeToken<List<String>> token = new TypeToken<List<String>>() {};
            list = gson.fromJson(listString, token.getType());
            ContactView.appContacts = list;
            ContactView.adapter.notifyDataSetChanged();
        }
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */

        sendNotification(message, contact);

    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String contactID) {
        Intent intent = new Intent(this, ChatView.class);
        intent.putExtra("contact", contactID);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(contactID)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
