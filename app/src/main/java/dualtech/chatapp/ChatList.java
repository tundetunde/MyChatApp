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

public class ChatList extends ListActivity implements View.OnClickListener{
    DbSqlite db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DbSqlite(this);
        ArrayList<String> chatList = (ArrayList)db.getChatList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, chatList);
        setListAdapter(adapter);
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, ChatView.class);
        String s = l.getItemAtPosition(position).toString();
        intent.putExtra("contact", s);
        startActivity(intent);
    }
}
