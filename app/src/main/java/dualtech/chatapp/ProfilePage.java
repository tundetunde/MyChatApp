package dualtech.chatapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

/**
 * Created by Jesz on 18-Aug-15.
 */

public class ProfilePage extends Activity implements View.OnClickListener{
    Button et_profile;
    ImageView dp;
    static SharedPreferences prefs;
    static TextView tv_user, tv_mobi;
    private static int SELECT_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        et_profile = (Button) findViewById(R.id.pro_edit);
        tv_mobi = (TextView) findViewById(R.id.tvNum);
        tv_user = (TextView) findViewById(R.id.tvName);
        dp = (ImageView) findViewById(R.id.dpView);
        tv_mobi.setText(ApplicationInit.getMobile_number());
        apply();
    }

    public static void apply(){
        tv_user.setText(ApplicationInit.getUser());
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

        apply();
    }

    public void selectPicture(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == SELECT_IMG){
            Bundle extras = data.getExtras();
            Bitmap bmp = (Bitmap) extras.get("data");
            dp.setImageBitmap(bmp);
        }
    }
}
