package dualtech.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

/**
 * Created by tunde_000 on 31/08/2015.
 */
public class ChatBackgroundSetting extends Activity{
    Button btChoosePhoto;
    ImageView img;
    ImageView imgChatView;
    private static int RESULT_LOAD_IMAGE = 1;
    static Bitmap backgroundPic;
    LinearLayout l1;
    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_background_settings);
        initialize();
    }

    private void initialize(){
        //l1 = (LinearLayout) findViewById(R.id.linearlayout1);
        btChoosePhoto = (Button)findViewById(R.id.btChoosePhoto);
        img = (ImageView)findViewById(R.id.ivBackgroundChat);
        aSwitch = (Switch)findViewById(R.id.switchChatBackground);
        aSwitch.setChecked(false);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    img.setVisibility(View.VISIBLE);
                    btChoosePhoto.setVisibility(View.VISIBLE);
                }else{
                    img.setVisibility(View.INVISIBLE);
                    btChoosePhoto.setVisibility(View.INVISIBLE);
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
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            backgroundPic = BitmapFactory.decodeFile(picturePath);
            img.setImageBitmap(backgroundPic);
            Drawable drawable = new BitmapDrawable(getResources(), backgroundPic);
            //ChatView.changeBackground(drawable);
            //ChatView.lin.setBackgroundDrawable(this, drawable);

            //l1.setBackgroundResource(this, backgroundPic);
        }
    }
}
