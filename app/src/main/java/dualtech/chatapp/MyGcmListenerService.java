package dualtech.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    Bitmap bitmap;

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }
    // [END receive_message]

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
        String user = data.getString("GCM_FROM");

        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Type: " + type);
        Log.d(TAG, "Contact: " + contact);
        Log.d(TAG, "Time: " + time);

        DbManager db = new DbManager(this);
        Gson gson;
        switch (type){
            case "msg":
                db.insertMessage(message, sender, 0);
                sendDeliveredReceipt(sender, data.getString("GCM_msgId"));
                break;
            case "Feed":
                String text = data.getString("msg");
                Log.d("CHECK FEED", user);
                db.insertFeed(user, text, time);
                Intent fin = new Intent("FEED");
                this.sendBroadcast(fin);
                break;
            case "Contact":
                String listString = data.getString("Contacts");
                ArrayList list;
                gson = new Gson();
                TypeToken<List<String>> token = new TypeToken<List<String>>() {};
                list = gson.fromJson(listString, token.getType());
                Log.d("Returned List: ", list.toString());
                db.insertContacts(list);
                Intent cn = new Intent("CONTACT");
                this.sendBroadcast(cn);
                break;
            case "Photo":
                //String image = data.getString("msg");
                String number = data.getString("GCM_FROM");
                /*byte[] byt = image.getBytes();
                saveToInternalStorage(user, byt);*/
                downloadImg(number);
                String d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                db.insertFeed(number, "display image", d, 1);
                Intent fini = new Intent("FEED");
                this.sendBroadcast(fini);
                break;
            case "ContactsPhoto":
                String photolist = data.getString("ListPhoto");
                ArrayList photo;
                gson = new Gson();
                TypeToken<List<Map<String, byte[]>>> photo_token = new TypeToken<List<Map<String, byte[]>>>() {};
                photo = gson.fromJson(photolist, photo_token.getType());
                System.out.println("Returned Photo List");
                for(Object m : photo){
                    HashMap<String, byte[]> map = (HashMap<String, byte[]>) m;
                    String key = map.keySet().toArray()[0].toString();
                    byte[] value = map.get(key);
                    saveToInternalStorage(key, value);
                }
                break;
            case "Deactivate":
                String confirm = data.getString("Confirm");
                if(confirm.equals("y")){
                    db.deactivateDatabase();
                    Intent i = new Intent().setClass(getApplicationContext(), MobileReg.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                break;
            case "Typing":
                String typing = data.getString("isUserTyping");
                Intent intent = new Intent("chicken");
                intent.putExtra("typing", typing);
                this.sendBroadcast(intent);
                break;
            case "Receipt":
                String receiptNumber = data.getString("receiptNo");
                String id = data.getString("msgId");
                db.updateMsgStatus(Integer.valueOf(receiptNumber), Integer.valueOf(id));
                updateMyActivity(this, message, sender, "");
                break;
            case "GroupMessage":
                String group = data.getString("GroupName");
                String groupId1 = data.getString("GCM_msgId");
                db.insertGroupMessage(message, sender, 0, groupId1);
                //sendDeliveredReceipt(sender, data.getString("GCM_msgId"));
                break;
            case "NewGroup":
                String groupName = data.getString("groupName");
                String groupList = data.getString("groupList");
                String groupId = data.getString("groupId");
                String creator = data.getString("creator");
                gson = new Gson();
                db.insertChatList(groupName, 1);
                TypeToken<List<String>> token1 = new TypeToken<List<String>>() {};
                ArrayList<String> list1 = gson.fromJson(groupList, token1.getType());
                db.insertGroupContacts(groupName, list1, groupId);
                db.insertChatList(groupId, 1);
                db.insertGroupMessage("You have added to this group by " + creator, "", 0, groupId);
                break;
            case "DeleteContactFromGroup":
                String theContact = data.getString("GCM_contactId");
                String groupID = data.getString("GCM_groupId");
                db.deleteUserFromGroup(theContact, groupID);
                db.insertGroupMessage("This contact has left the group: " + theContact, "", 0, groupID);
                break;
        }

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        if(type.equals("msg")){
            if(ChatView.active)
                updateMyActivity(this, message, sender, "");
            else
                sendNotification(message, sender, "");
        }else if(type.equals("GroupMessage")){
            String group = data.getString("GroupName");
            if(ChatView.active)
                updateMyActivity(this, message, sender, group);
            else
                sendNotification(message, sender, group);
        }
    }



    private void downloadImg(final String user){
        final ContextWrapper cw = new ContextWrapper(getApplicationContext());
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String myImagePath = ApplicationInit.PROFILE_PIC_ADDRESS + user + ".jpg";
                File mypath = null;
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream)new URL(myImagePath).getContent());
                    File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                    // Create imageDir
                    mypath = new File(directory, "profile_" + user + ".jpg");
                } catch (MalformedURLException e) {
                    Log.d("Error", "downloading Image");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.d("Error", "Saving Image 1");
                    e.printStackTrace();
                }
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                } catch (Exception e) {
                    Log.d("Error", "Saving Image");
                    e.printStackTrace();
                }
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d("Stored img", user);
            }
        }.execute();
    }

    private void saveToInternalStorage(String user, byte[] imgByte){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile_" + user + ".jpg");

        FileOutputStream fos;
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private int msgId() {
        return  ApplicationInit.getMsgId();
    }

    private void sendDeliveredReceipt(final String number, final String mid){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(MyGcmListenerService.this);
                String msg;
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "Receipt");
                    data.putString("msgId", mid);
                    data.putString("GCM_number", number);
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent delivery message";
                } catch (IOException ex) {
                    msg = "Delivery Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {}
        }.execute();
    }

    private void sendStatus(final String number){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(MyGcmListenerService.this);
                String msg;
                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    data.putString("Type", "DeviceGetsStatus");
                    data.putString("number", number);
                    data.putString("numberFrom", ApplicationInit.getMobile_number());
                    data.putString("status", getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE).getString(ApplicationInit.PROPERTY_STATUS, null));
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent delivery message";
                } catch (IOException ex) {
                    msg = "Delivery Message could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {}
        }.execute();
    }

    void updateMyActivity(Context context, String message, String contactID, String groupName) {
        Intent intent = new Intent("chicken");
        //put whatever data you want to send, if any
        String display = getContactName(contactID);
        if(groupName.isEmpty())
            display = groupName;
        intent.putExtra("message", message);
        intent.putExtra("display", display);
        intent.putExtra("contact", contactID);
        //send broadcast
        context.sendBroadcast(intent);
        cancelNotification(getBaseContext(), 0);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message, String contactID, String groupName) {
        Intent intent = new Intent(this, ChatView.class);
        String display = getContactName(contactID);
        intent.putExtra("display", display);
        intent.putExtra("contact", contactID);
        intent.putExtra("type", "0");

        if(!groupName.isEmpty()){
            intent.putExtra("display", groupName);
            intent.putExtra("type", "1");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder;
        if(!groupName.equals("")){
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(groupName)
                    .setContentText(message + " - " + groupName)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[] { 1000, 1000});

        }else{
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(display)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setVibrate(new long[] { 1000, 1000});
        }
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ApplicationInit.NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
    }
}
