package dualtech.chatapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Jesz on 18-Jul-15.
 */
public class DBProvider extends ContentProvider {
    //DATABASE HANDLER CLASS
    Sqlite sql;

    @Override
    public boolean onCreate() {
        sql = new Sqlite(getContext(), "Contact",null , 1);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    //SQL CLASS HELPER
    private static class Sqlite extends SQLiteOpenHelper {
        public Sqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String create_table = "CREATE TABLE Contacts" + "("
                    + "reg_id" + " INTEGER PRIMARY KEY," + "reg_name" + " TEXT,"
                    + "phone_number" + " TEXT" + ")";;
            db.execSQL(create_table);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(Sqlite.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS Contacts");
            // Create tables again
            onCreate(db);
        }
    }
/*
    JSON message format accepted by GCM
    String input = "{\"registration_ids\" : [\"Specify token you got from GCM\"],\"data\" : {\"message\": \"hai  welcome\"},}";*/
}