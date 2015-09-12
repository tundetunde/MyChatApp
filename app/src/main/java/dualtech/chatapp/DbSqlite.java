package dualtech.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
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
    final static String TABLE_CHATLIST = "chat_list";

    String contact_table = "CREATE TABLE " + TABLE_CONTACTS + "("
            + "phoneNumber TEXT" + ")";
    String feed_table = "CREATE TABLE " + TABLE_FEED + "("
            + "id INTEGER PRIMARY KEY autoincrement, " + "user TEXT,"
            + "status TEXT," + "date_time default current_timestamp" + ")";
    String message_table = "CREATE TABLE " + TABLE_MESSAGES + "("
            + "id integer PRIMARY KEY autoincrement," + "msg TEXT,"
            + "contact_id TEXT," + "datetime default current_timestamp,"
            + "sender INTEGER DEFAULT 0 NOT NULL" + ")";
    String chatlist_table = "CREATE TABLE " + TABLE_CHATLIST + "("
            + "contact TEXT PRIMARY KEY," + "regName TEXT" + ")";

    public DbSqlite(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(feed_table);
        db.execSQL(contact_table);
        db.execSQL(chatlist_table);
        db.execSQL(message_table);
        //insertDemo(db);
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


    public void insertFeed(String u, String s, String t){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user", u);
        values.put("status", s);
        values.put("date_time", t);

        db.insert(TABLE_FEED, null, values);
        Log.d(TAG, "ADDED " + s);
        db.close();
    }

    public void insertContacts(ArrayList arr){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;
        db.delete(TABLE_CONTACTS, null, null);

        for(Object n : arr) {
            values = new ContentValues();
            values.put("phoneNumber", n.toString());
            db.insert(TABLE_CONTACTS, null, values);
            Log.d(TAG, "ADDED " + n);
        }
        db.close();
    }

    public void insertMessage(String s, String c, int sender){
        if(checkChatList(c)){
            insertChatList(c);
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("msg", s);
        values.put("contact_id", c);
        values.put("sender", sender);

        db.insert(TABLE_MESSAGES, null, values);
        Log.d(TAG, "ADDED MSG : " + s);
        db.close();
    }

    public void insertChatList(String c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("contact", c);
        db.insert(TABLE_CHATLIST, null, values);

        Log.d(TAG, "UPDATED CHATLIST");
        db.close();
    }

    public List<String> getChatList(){

        List<String> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT contact FROM " + TABLE_CHATLIST;
        SQLiteDatabase db = this.getReadableDatabase();
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

    public boolean checkChatList(String s){

        Boolean check = false;
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CHATLIST + " WHERE (contact = '" + s + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() < 1){check = true;}
        cursor.close();
        Log.d(TAG, "CHAT LIST");
        return check;
    }

    public List<String> getAllContacts(){

        List<String> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT phoneNumber FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                update.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "ALL CONTACTS");
        return update;
    }

    public List<List> getAllFeed(){

        List<List> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_FEED;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                List<String> c = new ArrayList<String>();
                c.add(cursor.getString(1));
                c.add(cursor.getString(2));
                c.add(cursor.getString(3));
                // Adding contact to list
                update.add(c);
            } while (cursor.moveToNext());
        }
        System.out.println("FEED: " + cursor.getCount());
        cursor.close();
        Log.d(TAG, "ALL FEED");
        return update;
    }

    public List<ChatDbProvider> getChatHistory(String c){

        List<ChatDbProvider> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT msg,sender, datetime FROM " + TABLE_MESSAGES + " WHERE (contact_id = '" + c + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                update.add(new ChatDbProvider(cursor.getString(0),cursor.getInt(1), cursor.getString(2)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT HISTORY");
        return update;
    }

    public void deleteChatHistory(String c){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, "contact_id = '" + c + "'", null);
        db.delete(TABLE_CHATLIST, "contact = '" + c + "'", null);
    }

    public void deleteAllChatHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_MESSAGES);
        db.execSQL("delete from " + TABLE_CHATLIST);
    }

    public String countChat(){
        SQLiteDatabase db = this.getWritableDatabase();
        String total = "";
        String selectQuery = "SELECT Count(*) FROM " + TABLE_MESSAGES;
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                total = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "COUNT TIME");
        return total;
    }

    public void deactivateDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
        db.delete(TABLE_CHATLIST, null, null);
        db.delete(TABLE_CONTACTS, null, null);
        db.delete(TABLE_FEED, null, null);
    }
}
