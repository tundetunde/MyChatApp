package dualtech.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class FeedView extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textview = new TextView(this);
        textview.setText("This is Feed tab");
        setContentView(R.layout.feed_list);
    }
}
