package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Jesz on 18-Aug-15.
 */

public class ProfilePage extends Activity implements View.OnClickListener{
    static SharedPreferences prefs;
    static TextView tv_user, tv_mobi, tv_status;
    final String TAG = "PROFILE";
    File f = new File(Environment.getExternalStorageDirectory() + "/MyChatApp/Profile Photo/");
    Button et_profile;
    ImageView dp;
    DbSqlite db;
    GoogleCloudMessaging gcm;

    public static void setName(String s){
        ApplicationInit.setUser(s);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationInit.PROPERTY_USER_NAME, s);
        editor.apply();

        tv_user.setText(ApplicationInit.getUser());
    }

    public static void storePhoto(String s){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ApplicationInit.PROPERTY_PHOTO, s);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);

        et_profile = (Button) findViewById(R.id.pro_edit);
        tv_mobi = (TextView) findViewById(R.id.tvNum);
        tv_user = (TextView) findViewById(R.id.tvName);
        tv_status = (TextView) findViewById(R.id.tvStatus);
        dp = (ImageView) findViewById(R.id.dpView);
        gcm = GoogleCloudMessaging.getInstance(this);
        db = new DbSqlite(this);

        tv_mobi.setText(ApplicationInit.getMobile_number());
        et_profile.setOnClickListener(this);
        tv_status.setText(prefs.getString(ApplicationInit.PROPERTY_STATUS, "Hello there!!!"));
        initialize();
    }

    public void initialize(){
        tv_user.setText(ApplicationInit.getUser());

        String path = prefs.getString(ApplicationInit.PROPERTY_PHOTO, null);
        if(path != null){
            Bitmap bmp = BitmapFactory.decodeFile(path);
            dp.setImageBitmap(bmp);
        }
    }

    @Override
    public void onClick(View v) {
        //Creating the instance of PopupMenu
        PopupMenu popup = new PopupMenu(this, et_profile);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.edit_profile, popup.getMenu());
        popup.setOnMenuItemClickListener(new MenuItemListener(this));
        popup.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK /*&& requestCode == SELECT_IMG*/){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();


            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File app_path = new File(directory, "profile_mine.jpg");

            System.out.println("PATH APP: " + app_path.getAbsolutePath());
            try {
                copyImage(new File(picturePath), app_path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeFile(app_path.getAbsolutePath());
            storePhoto(app_path.getAbsolutePath());
            dp.setImageBitmap(bmp);
            uploadProfile(bmp);
        }
    }

    //Upload to server
    public void uploadProfile(final Bitmap bitmap){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bArray = bos.toByteArray();

                try {
                    String id = String.valueOf(msgId());
                    Bundle data = new Bundle();
                    Gson gson = new Gson();
                    String jsonPhoneList = gson.toJson(db.getAllContacts());
                    String byteString = Base64.encodeToString(bArray, Base64.DEFAULT);
                    data.putString("Type", "ProfilePhoto");
                    data.putString("ContactList", jsonPhoneList);
                    data.putString("UserOwner", ApplicationInit.getMobile_number());
                    //data.putString("ProfilePic", byteString);
                    gcm.send(ApplicationInit.getProjectNO() + "@gcm.googleapis.com", id, data);
                    msg = "Sent profile picture";
                } catch (IOException ex) {
                    msg = "Profile picture could not be sent";
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getBaseContext(), "UPLOADED PHOTO...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "PROFILE PICTURE SENT");
            }
        }.execute(null, null, null);
    }

    private int msgId() {
        int id = prefs.getInt(ApplicationInit.KEY_MSG_ID, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }

    public void copyImage(File s, File d) throws IOException {
        FileChannel src = new FileInputStream(s).getChannel();
        FileChannel dest = new FileOutputStream(d).getChannel();
        if(dest != null && src != null){
            dest.transferFrom(src, 0, src.size());
            dest.close();
            src.close();
        }
    }

}