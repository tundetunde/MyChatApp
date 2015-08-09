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
            + "reg_id" + " INTEGER PRIMARY KEY," + "reg_name" + " TEXT,"
            + "phone_number" + " TEXT" + ")";
    String feed_table = "CREATE TABLE " + TABLE_FEED + "("
            + "id" + " INTEGER PRIMARY KEY autoincrement, " + "status" + " TEXT" + ")";

    public DbSqlite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(contact_table + " " + feed_table);
        db.execSQL(feed_table);
        //db.execSQL("CREATE TABLE Messages(_id integer primary key autoincrement, msg text, reg_name text, reg_name2 text, at datetime default current_timestamp);");
        //insertDemo(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbSqlite.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS Contacts");
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

    public List<Collection> getChatList(){

        List<Collection> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Collection c = new ArrayList<>();
                c.add(cursor.getString(0));
                c.add(cursor.getString(2));
                // Adding contact to list
                update.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT LIST");
        return update;
    }

    private void insertDemo(SQLiteDatabase db){
        ContentValues values = new ContentValues();
        values.put("reg_id", 1);
        values.put("reg_name", "tunde");
        values.put("phone_number", "07944447710");
        db.insert("Contacts", null, values);

        values = new ContentValues();
        values.put("reg_id", 2);
        values.put("reg_name", "Jesz");
        values.put("phone_number", "02077084296");
        db.insert("Contacts", null, values);

        values = new ContentValues();
        values.put("msg", "hi");
        values.put("reg_name", "Jesz");
        values.put("reg_name2", "Tunde");
        db.insert("Messages", null, values);

        values = new ContentValues();
        values.put("msg", "hi");
        values.put("reg_name", "Tunde");
        values.put("reg_name2", "Jesz");
        db.insert("Messages", null, values);
    }
}
