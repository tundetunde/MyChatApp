package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Jesz on 18-Aug-15.
 */

public class ProfilePage extends Activity implements View.OnClickListener{
    final String TAG = "PROFILE";
    File f = new File(Environment.getExternalStorageDirectory() + "/MyChatApp/Profile Photo/");
    Button et_profile;
    ImageView dp;
    static SharedPreferences prefs;
    static TextView tv_user, tv_mobi, tv_status;

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

            if(!f.exists()){
                boolean b = f.mkdirs();
            }
            File file = new File(f.getAbsolutePath(), "/display_photo.jpg");

            try {
                file.createNewFile();
                copyImage(new File(picturePath), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            storePhoto(file.getAbsolutePath());
            dp.setImageBitmap(bmp);
        }
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