package dualtech.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class FeedView extends Activity implements View.OnClickListener {
    DbSqlite db = new DbSqlite(this);
    Button btn_share;
    EditText et_feed;
    String update;
    LinearLayout lt_feed;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_list);

        lt_feed = (LinearLayout) findViewById(R.id.lt_feed);
        btn_share = (Button) findViewById(R.id.btnGo);
        btn_share.setOnClickListener(this);

        TextWatcher text_watch = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){btn_share.setVisibility(View.GONE);}
                else{btn_share.setVisibility(View.VISIBLE);}
            }
        };

        et_feed = (EditText) findViewById(R.id.etUpdate);
        et_feed.addTextChangedListener(text_watch);
        refreshFeed();

    }


    @Override
    public void onClick(View v) {
        update = String.valueOf(et_feed.getText());
        if(update != null){
            db.insertFeed(update);
            refreshFeed();
            et_feed.setText("");
        }
    }

    public void refreshFeed(){
        if(lt_feed.getChildCount() > 0)
            lt_feed.removeAllViews();

        List<Collection> query = db.getAllFeed();
        Collections.reverse(query);//reverse the result

        for(Collection c : query){
            List list = new ArrayList(c);
            String tv_text = (String) list.get(1);

            TextView tv = new TextView(this);
            tv.setText(tv_text);
            tv.setTextSize(12);
            lt_feed.addView(tv);//add TextView to layout
        }
    }
}
