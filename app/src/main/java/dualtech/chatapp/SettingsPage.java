package dualtech.chatapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Created by Jesz on 20-Aug-15.
 */

public class SettingsPage extends Activity {
    ListView menu;
    String[] menuArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        menu = (ListView)findViewById(R.id.lvSettings);
        menuArray = new String[]{"Chat Background"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, menuArray);
        menu.setAdapter(adapter);
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                switch (position){
                    case 0:
                        Intent i = new Intent("dualtech.chatapp.CHATBACKGROUND");
                        startActivity(i);
                        break;
                }
            }
        });
    }


}
