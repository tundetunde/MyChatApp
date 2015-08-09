package dualtech.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//SQL CLASS HELPER
public class DbSqlite extends SQLiteOpenHelper {

    final static String TAG = "DB";
    final static String DB_NAME = "chat_db";
    final static int DB_VERSION = 1;
    final static String TABLE_FEED = "feed";
    final static String TABLE_MESSAGES = "messages";
    final static String TABLE_CONTACTS = "contacts";

    String contact_table = "CREATE TABLE " + TABLE_CONTACTS + "("
            + "regId" + " INTEGER PRIMARY KEY, " + "regName" + " TEXT,"
            + " phoneNumber" + " TEXT" + ")";
    String feed_table = "CREATE TABLE " + TABLE_FEED + "("
            + "id" + " INTEGER PRIMARY KEY autoincrement, " + "status" + " TEXT" + ")";

    public DbSqlite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(feed_table);
        db.execSQL(contact_table);
        db.execSQL("CREATE TABLE messages(_id integer primary key autoincrement, msg text, regName text, regName2 text, at datetime default current_timestamp);");
        insertDemo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbSqlite.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS messages");
        db.execSQL("DROP TABLE IF EXISTS contacts");
        db.execSQL("DROP TABLE IF EXISTS feed");
        // Create tables again
        onCreate(db);
    }

    public void insert(String s){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("status", s);

        db.insert(TABLE_FEED, null, values);
        Log.d(TAG, "ADDED " + s);
        db.close();
    }

    public List<Collection> getAllFeed(){

        List<Collection> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_FEED;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Collection c = new ArrayList<>();
                c.add(cursor.getString(0));
                c.add(cursor.getString(1));
        // Adding contact to list
                update.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "ALL FEED");
        return update;
    }

    public List<String> getChatList(){

        List<String> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT regName FROM " + TABLE_MESSAGES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                update.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT LIST");
        return update;
    }

    private void insertDemo(SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put("regId", 1);
        values.put("regName", "tunde");
        values.put("phoneNumber", "07944447710");
        db.insert("contacts", null, values);

        values = new ContentValues();
        values.put("regId", 2);
        values.put("regName", "Jesz");
        values.put("phone_number", "02077084296");
        db.insert("contacts", null, values);

        values = new ContentValues();
        values.put("msg", "hi");
        values.put("regName", "Jesz");
        values.put("regName2", "Tunde");
        db.insert("messages", null, values);

        values = new ContentValues();
        values.put("msg", "hi");
        values.put("regName", "Tunde");
        values.put("regName2", "Jesz");
        db.insert("messages", null, values);
    }
}
