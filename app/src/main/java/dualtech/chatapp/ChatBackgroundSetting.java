package dualtech.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by tunde_000 on 31/08/2015.
 */
public class ChatBackgroundSetting extends AppCompatActivity{
    static Bitmap backgroundPic;
    private static int RESULT_LOAD_IMAGE = 1;
    Button btChoosePhoto;
    ImageView img;
    Switch aSwitch;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_background_settings);

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.bg_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("CHAT BACKGROUND");

        initialize();
    }

    private void initialize(){
        btChoosePhoto = (Button)findViewById(R.id.btChoosePhoto);
        img = (ImageView)findViewById(R.id.ivBackgroundChat);
        aSwitch = (Switch)findViewById(R.id.switchChatBackground);

        if(ApplicationInit.getChatBg()){
            aSwitch.setChecked(ApplicationInit.getChatBg());
            img.setVisibility(View.VISIBLE);
            btChoosePhoto.setVisibility(View.VISIBLE);

            if(ApplicationInit.getChatBgURL() != null){
                Bitmap bmp = BitmapFactory.decodeFile(ApplicationInit.getChatBgURL());
                img.setImageBitmap(bmp);
            }
        }
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    img.setVisibility(View.VISIBLE);
                    btChoosePhoto.setVisibility(View.VISIBLE);
                    ApplicationInit.setChatBg(true);
                }else{
                    img.setVisibility(View.INVISIBLE);
                    btChoosePhoto.setVisibility(View.INVISIBLE);
                    img.setImageDrawable(null);
                    ApplicationInit.setChatBg(false);
                    ApplicationInit.setChatBgURL(null);
                }
            }
        });
        btChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });

    }

    public void selectPicture(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Code to use selected image
        if(resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            try{
                final File directory = new ContextWrapper(getApplicationContext()).getDir("BG_Dir", Context.MODE_PRIVATE);
                File app_path = new File(directory, "Background.jpg");
                FileOutputStream out = new FileOutputStream(app_path);
                backgroundPic = BitmapFactory.decodeFile(picturePath);
                backgroundPic.compress(Bitmap.CompressFormat.JPEG, 85, out);
                img.setImageBitmap(backgroundPic);
                ApplicationInit.setChatBgURL(app_path.getAbsolutePath());
                Drawable drawable = new BitmapDrawable(getResources(), backgroundPic);
                out.flush();
                out.close();
                Toast.makeText(getBaseContext(),"Chat Background changed", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                item.setOnMenuItemClickListener(new MenuItemListener(this));
        }
        return super.onOptionsItemSelected(item);
    }

}