package dualtech.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jesz on 18-Aug-15.
 */

public class ProfilePage extends AppCompatActivity implements View.OnClickListener{
    static SharedPreferences prefs;
    static TextView tv_user, tv_mobi, tv_status;
    final String TAG = "PROFILE";
    ImageView dp;
    DbSqlite db;
    GoogleCloudMessaging gcm;
    Toolbar toolbar;

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

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("MY PROFILE");

        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        tv_mobi = (TextView) findViewById(R.id.tvNum);
        tv_user = (TextView) findViewById(R.id.tvName);
        tv_status = (TextView) findViewById(R.id.tvStatus);
        dp = (ImageView) findViewById(R.id.dpView);
        gcm = GoogleCloudMessaging.getInstance(this.getApplicationContext());
        db = new DbSqlite(this);

        tv_mobi.setText(ApplicationInit.getMobile_number());
        tv_status.setText(prefs.getString(ApplicationInit.PROPERTY_STATUS, "Hello there!!!"));
        initialize();
    }

    public void initialize(){
        tv_user.setText(ApplicationInit.getUser());

        String path = prefs.getString(ApplicationInit.PROPERTY_PHOTO, null);
        if(path != null){
            Bitmap bmp = getOriginalImg(path);
            dp.setImageBitmap(bmp);
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_pr_edit:
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_pr_edit));
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.edit_profile, popup.getMenu());
                popup.setOnMenuItemClickListener(new MenuItemListener(this));
                popup.show();
                return true;
            default:
                item.setOnMenuItemClickListener(new MenuItemListener(this));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
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
            Bitmap bmp = getThumbnail(app_path.getAbsoluteFile());
            storePhoto(app_path.getAbsolutePath());
            uploadProfile(bmp);
        }
    }

    //Upload to server
    public void uploadProfile(final Bitmap selected){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                String id = String.valueOf(msgId());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                selected.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bArray = bos.toByteArray();
                Gson gson = new Gson();
                final String jsonPhoneList = gson.toJson(db.getAllContacts());
                final String byteString = Base64.encodeToString(bArray, Base64.DEFAULT);
                Log.d(TAG, "BYTE ARR: " + bArray.length);
                Log.d(TAG, "BYTE STR: " + byteString.length());
                Log.d(TAG, "BYTE ARR: " + (double)bArray.length/(1024*1024) + "MB");
                Log.d(TAG, "BYTE STR: " + (double) byteString.getBytes().length / (1024 * 1024) + "MB");

                StringRequest postRequest = new StringRequest(Request.Method.POST, ApplicationInit.SERVER_ADDRESS,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response: " + response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, error.toString());
                        Toast.makeText(getApplicationContext(), "Server failed to receive the RegID", Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        // the POST parameters:
                        params.put("Photo", "y");
                        params.put("ContactList", jsonPhoneList);
                        params.put("UserOwner", ApplicationInit.getMobile_number());
                        params.put("ProfilePic", byteString);
                        return params;
                    }
                };
                Volley.newRequestQueue(ProfilePage.this).add(postRequest);
                msg = "Profile picture could not be sent";
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getBaseContext(), "UPLOADED PHOTO...", Toast.LENGTH_LONG).show();
                Log.d(TAG, "PROFILE PICTURE SENT");
            }
        }.execute(null, null, null);

    }

    public Bitmap getThumbnail(File file){
        try{
            Bitmap SelectedImg = BitmapFactory.decodeFile(file.getAbsolutePath());
            int h = SelectedImg.getHeight()/5;
            int w = SelectedImg.getWidth()/5;
            Bitmap thumbnail = Bitmap.createScaledBitmap(SelectedImg, w, h, false);

            //overwrite file
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);

            dp.setImageBitmap(SelectedImg);//set the profile image view

            return thumbnail;
        }catch(Exception e){
            return null;
        }
    }

    public Bitmap getOriginalImg(String path){
        Bitmap bm;// = BitmapFactory.decodeFile(path);
        BitmapFactory.Options op = new BitmapFactory.Options();
        op.inScaled = false;
        bm = BitmapFactory.decodeFile(path, op);
        int h = bm.getHeight()*5;
        int w = bm.getWidth()*5;
        Bitmap b = Bitmap.createScaledBitmap(bm, w, h, false);
        return b;
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