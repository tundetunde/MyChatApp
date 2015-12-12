package dualtech.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

/**
 * Created by tunde_000 on 03/09/2015.
 */
public class AboutUs extends AppCompatActivity {
    Toolbar toolbar;
    TextView tv;
    String text1 = "PhoMix Filter is a photo editing app enabling you to design and edit your photos with complete control. " +
            "You may create photo grids combining multiple pictures into one or you may use the built in editor which boasts " +
            "many effects and filters to apply to photos. " +
            "PhoMix Filter also features the ability to share your photos on many different social networks.";

    String text2 = "Chat app is created by two software developers, Oyetunde Awotunde and Jessica Adachi. " +
            "Both of us are in university currently studying Computer Science. This app was made as a project between " +
            "the both of us as a hobby. " +
            "We strive to produce the best photo editing app on the app store and will produce strong updates in order to achieve this.";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("ABOUT US");

        tv = (TextView) findViewById(R.id.textDescription);
        tv.setText(text1);
        tv.append("\n\n" + text2);
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
