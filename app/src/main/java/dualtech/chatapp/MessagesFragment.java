package dualtech.chatapp;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;

public class MessagesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnFragmentInteractionListener {
        String getProfileEmail();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = new Bundle();
        args.putString(DBProvider.COL_NAME, mListener.getProfileEmail());
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_row,
                null,
                new String[]{DBProvider.COL_MSG, DBProvider.COL_AT},
                new int[]{R.id.txtFrom, R.id.txtTO},
                0);
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch(view.getId()) {
                    case R.id.txtFrom:
                        LinearLayout root = (LinearLayout) view.getParent().getParent();
                        if (cursor.getString(cursor.getColumnIndex(DBProvider.COL_FROM)) == null) {
                            root.setGravity(Gravity.RIGHT);
                            root.setPadding(50, 10, 10, 10);
                        } else {
                            root.setGravity(Gravity.LEFT);
                            root.setPadding(10, 10, 50, 10);
                        }
                        break;
                }
                return false;
            }
        });
        setListAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
