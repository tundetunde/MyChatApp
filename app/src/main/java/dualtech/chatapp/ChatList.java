package dualtech.chatapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

public class ChatList extends Activity implements View.OnClickListener{
    DbSqlite db;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_list);
        db = new DbSqlite(this);
        ArrayList<Collection> chatList = (ArrayList)db.getChatList();
        ArrayAdapter<Collection> adapter = new ArrayAdapter<Collection>(this,
                android.R.layout.simple_list_item_1, chatList);
        initialize(adapter);
    }

    private void initialize(ArrayAdapter<Collection> adapter){
        lv = (ListView)findViewById(R.id.lvChat);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                //AddContactDialog dialog = new AddContactDialog();
                //dialog.show(getFragmentManager(), "AddContactDialog");
                return true;

            case R.id.action_settings:
                //Intent intent = new Intent(this, SettingsActivity.class);
                //startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}
