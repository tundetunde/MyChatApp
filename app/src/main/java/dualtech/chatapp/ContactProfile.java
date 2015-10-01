package dualtech.chatapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
* Created by Jesz on 18-Aug-15.
*/
public class ContactProfile extends AppCompatActivity {
    private static final String TAG = "ContactProfile";
    SharedPreferences prefs;
    ImageView ch_display;
    TextView ch_num, ch_status, ch_name;
    String contact, display_name;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_profile);
        Bundle bundle = getIntent().getExtras();
        prefs = getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        contact = bundle.getString("number");
        display_name = bundle.getString("name");
        ch_name = (TextView) findViewById(R.id.ch_header);
        ch_display = (ImageView) findViewById(R.id.ch_dpView);
        ch_num = (TextView) findViewById(R.id.ch_tvNum);
        ch_status = (TextView) findViewById(R.id.ch_tvStatus);
        initialize();
    }

    public void initialize(){
        ch_num.setText(contact);
        ch_name.setText(display_name);
        setImage();
    }

    public void setImage(){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "profile_" + contact + ".jpg");
        if(mypath.exists()){
            Bitmap bmp = BitmapFactory.decodeFile(mypath.getAbsolutePath());
            ch_display.setImageBitmap(bmp);
        }
    }
}
