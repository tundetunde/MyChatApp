package dualtech.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
* Created by Jesz on 20-Aug-15.
*/

public class SettingsPage extends AppCompatActivity {
    ListView menu;
    String[] menuArray;
    Intent i;
    DbManager db;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("SETTINGS");

        db = new DbManager(this);
        menu = (ListView)findViewById(R.id.lvSettings);
        menuArray = new String[]{"About Us","Chat Background", "Deactivate Account", "Help", "Clear Chats"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, menuArray);
        menu.setAdapter(adapter);
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                switch (position) {
                    case 0:
                        i = new Intent().setClass(getApplicationContext(), AboutUs.class);
                        startActivity(i);
                        break;
                    case 1:
                        i = new Intent().setClass(getApplicationContext(), ChatBackgroundSetting.class);
                        startActivity(i);
                        break;
                    case 2:
                        i = new Intent().setClass(getApplicationContext(), DeactivateAcct.class);
                        startActivity(i);
                        break;
                    case 4:
                        displayDialog();
                        break;
                }
            }
        });
    }

    private void deleteChat(){
        new AsyncTask<Void, Void, String>() {
            String total;
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                db.deleteAllChatHistory();
                total = db.countChat();
                msg = "Delete Complete";
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                Log.d("DELETE CHAT", "ALL CHATS DELETED");
                Log.d("DELETE CHAT", total);
            }
        }.execute(null, null, null);
    }

    public void displayDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete all chat history?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteChat();
                        Toast.makeText(getBaseContext(), "All chat history deleted", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
