package dualtech.chatapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//SQL CLASS HELPER
public class DbManager extends SQLiteOpenHelper {

    final static String TAG = "DB";
    final static String DB_NAME = "chat_db";
    final static int DB_VERSION = 1;
    final static String TABLE_FEED = "feed";
    final static String TABLE_MESSAGES = "messages";
    final static String TABLE_STATUS = "status";
    final static String TABLE_CONTACTS = "contacts";
    final static String TABLE_CHAT_LIST = "chat_list";
    final static String TABLE_GROUP_MSG = "group_msg";
    final static String TABLE_GROUP_CONTACTS = "group_contacts";

    String contact_table = "CREATE TABLE " + TABLE_CONTACTS + "("
            + "phoneNumber TEXT," + "accepted INTEGER DEFAULT 0 NOT NULL"
            + ")";
    String group_contacts = "CREATE TABLE " + TABLE_GROUP_CONTACTS + "("
            + "id INTEGER PRIMARY KEY autoincrement," + "groupId TEXT NOT NULL," + "name TEXT,"
            + "contacts TEXT" + ")";
    String feed_table = "CREATE TABLE " + TABLE_FEED + "("
            + "id INTEGER PRIMARY KEY autoincrement, " + "user TEXT,"
            + "status TEXT," + "date_time default current_timestamp," + "picture INTEGER DEFAULT 0 NOT NULL" +")";
    String message_table = "CREATE TABLE " + TABLE_MESSAGES + "("
            + "id integer PRIMARY KEY autoincrement," + "msg TEXT,"
            + "contact_id TEXT," + "datetime default current_timestamp,"
            + "sender INTEGER DEFAULT 0 NOT NULL," + "status INTEGER DEFAULT 0 NOT NULL" + ")";
    String group_msg_table = "CREATE TABLE " + TABLE_GROUP_MSG + "("
            + "id integer PRIMARY KEY autoincrement," + "groupId TEXT," + "msg TEXT,"
            + "contact_id TEXT," + "datetime default current_timestamp,"
            + "sender INTEGER DEFAULT 0 NOT NULL," + "name string" + ")";
    String chatList_table = "CREATE TABLE " + TABLE_CHAT_LIST + "("
            + "contact TEXT PRIMARY KEY," + "regName TEXT," + "type INTEGER DEFAULT 0 NOT NULL" + ")";

    public DbManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(feed_table);
        db.execSQL(contact_table);
        db.execSQL(chatList_table);
        db.execSQL(message_table);
        db.execSQL(group_msg_table);
        db.execSQL(group_contacts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DbManager.class.getName(),
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

    public void insertGroupContacts(String name, ArrayList<String> listContacts, String groupID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String json = new Gson().toJson(listContacts);
        values.put("name", name);
        values.put("contacts", json);
        values.put("groupId", groupID);

        db.insert(TABLE_GROUP_CONTACTS, null, values);
        Log.d(TAG, "ADDED GROUP " + name);
        db.close();
    }

    public void UpdateGroupContacts(String groupID, ArrayList<String> contacts){
        SQLiteDatabase db = this.getWritableDatabase();
        Gson gson = new Gson();
        List<String> ListContacts = new ArrayList<>();
        String selectQuery = "SELECT contacts FROM " + TABLE_GROUP_CONTACTS + " WHERE (groupID = '" + groupID + "')";
        Cursor cursor = db.rawQuery(selectQuery, null);
        TypeToken<List<String>> token1 = new TypeToken<List<String>>() {};
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String x = cursor.getString(0);
                if(x != "")
                    ListContacts = gson.fromJson(x, token1.getType());
            } while (cursor.moveToNext());
        }
        cursor.close();
        for(String x: contacts){
            ListContacts.add(x);
        }
        String json = gson.toJson(ListContacts);
        ContentValues newValues = new ContentValues();
        newValues.put("contacts", json);

        db.update(TABLE_GROUP_CONTACTS, newValues, "groupID='" + groupID + "'", null);
        db.close();
    }

    public void createGroup(String g, String n, String c){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        ArrayList<String> x = new ArrayList<>();
        x.add(c);
        String json = new Gson().toJson(x);
        values.put("groupId", g);
        values.put("name", n);
        values.put("contacts", json);
        db.insert(TABLE_GROUP_CONTACTS, null, values);
        db.close();
    }

    //Overload function for Display Picture
    public void insertFeed(String u, String s, String t, int pic){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("user", u);
        values.put("status", s);
        values.put("date_time", t);
        values.put("picture", pic);

        db.insert(TABLE_FEED, null, values);
        Log.d(TAG, "ADDED " + s);
        db.close();
    }

    public void insertContacts(ArrayList arr){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;

        for(Object n : arr) {
            if(checkContList(n.toString())) {
                values = new ContentValues();
                values.put("phoneNumber", n.toString());
                db.insert(TABLE_CONTACTS, null, values);
                Log.d(TAG, "ADDED " + n);
            }
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

    //Group Message
    public void insertGroupMessage(String s, String c, int sender, String groupid){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("msg", s);
        values.put("contact_id", c);
        values.put("sender", sender);
        values.put("name", getGroupName(groupid));
        values.put("groupId", groupid);
        db.insert(TABLE_GROUP_MSG, null, values);
        Log.d(TAG, "ADDED GROUP MSG : " + s);
        db.close();
    }

    public void insertChatList(String c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("contact", c);
        db.insert(TABLE_CHAT_LIST, null, values);

        Log.d(TAG, "UPDATED CHATLIST");
        db.close();
    }

    public void insertChatList(String c, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("contact", c);
        values.put("type", type);
        db.insert(TABLE_CHAT_LIST, null, values);

        Log.d(TAG, "UPDATED CHATLIST");
        db.close();
    }

    public boolean checkChatList(String s){
        Boolean check = false;
        String selectQuery = "SELECT * FROM " + TABLE_CHAT_LIST + " WHERE (contact = '" + s + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() < 1){check = true;}
        cursor.close();
        return check;
    }

    public boolean checkContList(String s){
        Boolean check = false;
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS + " WHERE (phoneNumber = '" + s + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() < 1){check = true;}
        cursor.close();
        return check;
    }

    public String getStatus(String number){
        String name = "";
        // Select All Query
        String selectQuery = "SELECT status FROM " + TABLE_FEED + " WHERE (user = '" + number + "') ORDER BY date_time DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public List<List<String>> getChatList(){
        List<List<String>> update = new ArrayList<>();
        String selectQuery = "SELECT contact, type FROM " + TABLE_CHAT_LIST;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                List<String> c = new ArrayList<>(2);
                c.add(cursor.getString(0));
                c.add(String.valueOf(cursor.getInt(1)));
                update.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT LIST");
        return update;
    }

    public String getGroupName(String s){
        String name = "";
        String selectQuery = "SELECT name FROM " + TABLE_GROUP_CONTACTS + " WHERE (groupId = '" + s + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
           name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public List<String> getAllContacts(){
        List<String> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT phoneNumber FROM " + TABLE_CONTACTS + " WHERE (accepted = 0)";
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
                List<String> c = new ArrayList<>();
                c.add(cursor.getString(1));
                c.add(cursor.getString(2));
                c.add(cursor.getString(3));
                c.add(cursor.getString(4));
                // Adding contact to list
                update.add(c);
            } while (cursor.moveToNext());
        }
        System.out.println("FEED: " + cursor.getCount());
        cursor.close();
        Log.d(TAG, "ALL FEED");
        return update;
    }

    public String getGroupContacts(String c) {

        String update = "";
        // Select All Query
        String selectQuery = "SELECT contacts FROM " + TABLE_GROUP_CONTACTS + " WHERE (name = '" + c + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                update = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT HISTORY");
        return update;
    }

    public List<ChatDbProvider> getChatHistory(String c) {

        List<ChatDbProvider> update = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT msg,sender, datetime, status FROM " + TABLE_MESSAGES + " WHERE (contact_id = '" + c + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                // Adding contact to list
                update.add(new ChatDbProvider(cursor.getString(0),cursor.getInt(1), cursor.getString(2), cursor.getInt(3)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT HISTORY");
        return update;
    }

    //Chat History for group chat
    public List<ChatDbProvider> getGroupChatHistory(String c, String groupName) {
        List<ChatDbProvider> update = new ArrayList<>();
        String selectQuery = "SELECT msg,sender, datetime FROM " + TABLE_GROUP_MSG + " WHERE  (name = '" + groupName + "')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                update.add(new ChatDbProvider(cursor.getString(0),cursor.getInt(1), cursor.getString(2), 0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "CHAT HISTORY");
        return update;
    }

    public void deleteChatHistory(String c){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, "contact_id = '" + c + "'", null);
        db.delete(TABLE_CHAT_LIST, "contact = '" + c + "'", null);
    }

    public void deleteGroup(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROUP_CONTACTS, "groupId = '" + id + "'", null);
        db.delete(TABLE_GROUP_MSG, "groupId = '" + id + "'", null);
        db.delete(TABLE_CHAT_LIST, "contact = '" + id + "'", null);
    }

    public void deleteUserFromGroup(String contact, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Gson gson = new Gson();
        List<String> ListContacts = new ArrayList<>();
        String selectQuery = "SELECT contacts FROM " + TABLE_GROUP_CONTACTS + " WHERE (groupID = '" + id + "')";
        Cursor cursor = db.rawQuery(selectQuery, null);
        TypeToken<List<String>> token1 = new TypeToken<List<String>>() {};
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String x = cursor.getString(0);
                if(x != "")
                    ListContacts = gson.fromJson(x, token1.getType());
            } while (cursor.moveToNext());
        }
        cursor.close();
        for(String x: ListContacts){
            if(x.equals(contact))
                ListContacts.remove(x);
        }
        String json = gson.toJson(ListContacts);
        ContentValues newValues = new ContentValues();
        newValues.put("contacts", json);

        db.update(TABLE_GROUP_CONTACTS, newValues, "groupID='" + id + "'", null);
        db.close();
    }

    public void deleteAllChatHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + TABLE_MESSAGES);
        db.execSQL("delete from " + TABLE_GROUP_MSG);
        db.execSQL("delete from " + TABLE_GROUP_CONTACTS);
        db.execSQL("delete from " + TABLE_CHAT_LIST);

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
        db.delete(TABLE_CHAT_LIST, null, null);
        db.delete(TABLE_CONTACTS, null, null);
        db.delete(TABLE_FEED, null, null);
    }

    public String getLastMessage(String c){
        SQLiteDatabase db = this.getWritableDatabase();
        String msg = "";
        String selectQuery = "SELECT msg FROM " + TABLE_MESSAGES + " WHERE (contact_id = '" + c + "') OR (sender = '" + c + "')"
                + " ORDER BY id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                msg = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Log.d(TAG, "LAST MESSAGE");
        return msg;
    }

    public String getLastMessageTime(String c){
        SQLiteDatabase db = this.getWritableDatabase();
        String msg = "";
        String selectQuery = "SELECT datetime FROM " + TABLE_MESSAGES + " WHERE (contact_id = '" + c + "') OR (sender = '" + c + "')"
                + " ORDER BY id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                msg = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        System.out.println("CHAT_LISTI: " + msg);
        return msg;
    }
    public String getGrpMessageTime(String c){
        SQLiteDatabase db = this.getWritableDatabase();
        String msg = "";
        String selectQuery = "SELECT datetime FROM " + TABLE_GROUP_MSG + " WHERE (groupId = '" + c + "')"
                + " ORDER BY id DESC LIMIT 1";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                msg = cursor.getString(0);
            } while (cursor.moveToNext());
        }
        cursor.close();
        System.out.println("CHAT_LIST: "+msg);
        return msg;
    }

    public int getMsgCount(){
        SQLiteDatabase db = this.getReadableDatabase();
        String s = "Select * FROM " + TABLE_MESSAGES;
        Cursor cursor = db.rawQuery(s, null);
        int n = cursor.getCount();
        cursor.close();
        return n;
    }

    public void updateMsgStatus(int status, int id){
        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "UPDATE " + TABLE_MESSAGES + " SET status = " + status + " WHERE id = '" + id + "'";
        db.execSQL(updateQuery);
    }
}
