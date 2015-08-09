package dualtech.chatapp;

import android.app.ActionBar;
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
import android.widget.ListView;
import android.widget.TextView;

public class ChatList extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SimpleCursorAdapter(this,
                R.layout.list_row,
                null,
                new String[]{DBProvider.COL_NAME, DBProvider.COL_COUNT},
                new int[]{R.id.txtFrom, R.id.txtTO},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch(view.getId()) {
                    case R.id.txtTO:
                        int count = cursor.getInt(columnIndex);
                        if (count > 0) {
                            ((TextView)view).setText(String.format("%d new message%s", count, count==1 ? "" : "s"));
                        }
                        return true;
                }
                return false;
            }
        });
        setListAdapter(adapter);

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowTitleEnabled(false);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this,
                DBProvider.CONTENT_URI_PROFILE,
                new String[]{DBProvider.COL_ID, DBProvider.COL_NAME, DBProvider.COL_COUNT},
                null,
                null,
                DBProvider.COL_ID + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, ChatView.class);
        intent.putExtra("TUNDE", String.valueOf(id));
        startActivity(intent);
    }
}
