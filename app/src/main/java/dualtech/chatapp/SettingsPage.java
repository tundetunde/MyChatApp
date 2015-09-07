package dualtech.chatapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;

/**
 * Created by Jesz on 20-Aug-15.
 */

public class SettingsPage extends Activity {
    ListView menu;
    String[] menuArray;
    Intent i;
    DbSqlite db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        db = new DbSqlite(this);
        menu = (ListView)findViewById(R.id.lvSettings);
        menuArray = new String[]{"About Us","Chat Background", "Deactivate Account", "Help", "Clear Chats"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, menuArray);
        menu.setAdapter(adapter);
        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                switch (position){
                    case 0:
                        i = new Intent("dualtech.chatapp.ABOUTUS");
                        startActivity(i);
                        break;
                    case 1:
                        i = new Intent("dualtech.chatapp.CHATBACKGROUND");
                        startActivity(i);
                        break;
                    case 2:
                        i = new Intent("dualtech.chatapp.DEACTIVATE");
                        startActivity(i);
                        break;
                    case 4:
                        displayDialog();
                        break;
                }
            }
        });
    }

    public void displayDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        deleteChat();
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
                Intent i = new Intent(SettingsPage.this, MainActivity.class);
                startActivity(i);
            }
        }.execute(null, null, null);
    }



}
