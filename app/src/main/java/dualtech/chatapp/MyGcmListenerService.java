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
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                String image = data.getString("msg");
                String number = data.getString("GCM_FROM");
                /*byte[] byt = image.getBytes();
                saveToInternalStorage(user, byt);*/
                downloadImg(number);
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
                updateMyActivity(this, message, sender);
                break;

        }

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        if(type.equals("msg")){
            if(ChatView.active)
                updateMyActivity(this, message, sender);
            else
                sendNotification(message, sender);
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

    void updateMyActivity(Context context, String message, String contactID) {
        Intent intent = new Intent("chicken");
        //put whatever data you want to send, if any
        String display = getContactName(contactID);
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

        notificationManager.notify(ApplicationInit.NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
    }
}
