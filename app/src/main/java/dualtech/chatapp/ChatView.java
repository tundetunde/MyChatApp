package dualtech.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class ChatView extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list);
        TextView mb = (TextView) findViewById(R.id.mid);
        TextView rb = (TextView) findViewById(R.id.rid);

        mb.setText(mb.getText() + ApplicationInit.getMobile_number());
        rb.setText(rb.getText() + ApplicationInit.getREGISTRATION_KEY());
    }
}
