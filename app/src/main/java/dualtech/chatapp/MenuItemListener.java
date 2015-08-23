package dualtech.chatapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.PopupMenu;

/**
 * Created by Jesz on 19-Aug-15.
 */
public class MenuItemListener implements MenuItem.OnMenuItemClickListener, PopupMenu.OnMenuItemClickListener {

    Intent i;
    Context context;

    MenuItemListener(Context c){
        context = c;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_add:
                i = new Intent().setClass(context, ProfilePage.class);
                context.startActivity(i);
                return true;
            case R.id.action_profile:
                i = new Intent().setClass(context, ProfilePage.class);
                context.startActivity(i);
                return true;
            case R.id.action_settings:
                i = new Intent().setClass(context, SettingsPage.class);
                context.startActivity(i);
                return true;
            case R.id.edit_name:
                final EditText name = new EditText(context);

                new AlertDialog.Builder(context)
                        .setTitle("Set Name")
                        .setMessage("Enter your name!")
                        .setView(name)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String s = name.getText().toString();
                                ProfilePage.setName(s);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;
            case R.id.edit_dp:

                i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((Activity)context).startActivityForResult(Intent.createChooser(i, "Choose Profile Pictures"), 1);
                return true;
            default:
                return false;
        }
    }


}
