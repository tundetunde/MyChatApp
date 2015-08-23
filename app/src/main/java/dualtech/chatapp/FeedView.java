package dualtech.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class FeedView extends Fragment implements View.OnClickListener {
    DbSqlite db;
    Button btn_share;
    EditText et_feed;
    String update;
    LinearLayout lt_feed;
    static SharedPreferences prefs;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.feed_list, container, false);
        db = new DbSqlite(getActivity());
        prefs = getActivity().getSharedPreferences(ApplicationInit.SHARED_PREF, Context.MODE_PRIVATE);
        lt_feed = (LinearLayout) v.findViewById(R.id.lt_feed);
        btn_share = (Button) v.findViewById(R.id.btnGo);
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

        et_feed = (EditText) v.findViewById(R.id.etUpdate);
        et_feed.addTextChangedListener(text_watch);
        refreshFeed();

        return v;
    }

    @Override
    public void onClick(View v) {
        update = String.valueOf(et_feed.getText());
        if(update != null){
            db.insertFeed(update);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ApplicationInit.PROPERTY_STATUS, update);
            editor.apply();
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

            TextView tv = new TextView(getActivity());
            tv.setText(tv_text);
            tv.setTextSize(12);
            lt_feed.addView(tv);//add TextView to layout
        }
    }

}
