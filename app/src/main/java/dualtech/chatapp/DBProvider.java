package dualtech.chatapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DBProvider extends ContentProvider {
    //DATABASE HANDLER CLASS
    Sqlite contact_sql;


    public static final String COL_ID = "_id";

    //Table and Column names
    public static final String TABLE_MESSAGES = "messages";
    public static final String COL_MSG = "msg";
    public static final String COL_FROM = "reg_name";
    public static final String COL_TO = "reg_name2";
    public static final String COL_AT = "at";

    //Table and column names
    public static final String TABLE_CONTACTS = "contacts";
    public static final String COL_REGID = "reg_id";
    public static final String COL_NAME = "reg_name";
    public static final String COL_PHONENUMBER = "phone_number";
    public static final String COL_COUNT = "county";

    public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://dualtech.chatapp.provider/messages");
    public static final Uri CONTENT_URI_PROFILE = Uri.parse("content://dualtech.chatapp.provider/contacts");

    private static final int MESSAGES_ALLROWS = 1;
    private static final int MESSAGES_SINGLE_ROW = 2;
    private static final int CONTACTS_ALLROWS = 3;
    private static final int CONTACTS_SINGLE_ROW = 4;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("dualtech.chatapp.provider", "messages", MESSAGES_ALLROWS);
        uriMatcher.addURI("dualtech.chatapp.provider", "messages/#", MESSAGES_SINGLE_ROW);
        uriMatcher.addURI("dualtech.chatapp.provider", "contacts", CONTACTS_ALLROWS);
        uriMatcher.addURI("dualtech.chatapp.provider", "contacts/#", CONTACTS_SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        contact_sql = new Sqlite(getContext(), "chat_db1",null , 1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = contact_sql.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case CONTACTS_ALLROWS:
                qb.setTables(getTableName(uri));
                break;

            case MESSAGES_SINGLE_ROW:
            case CONTACTS_SINGLE_ROW:
                qb.setTables(getTableName(uri));
                qb.appendWhere("_id = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = contact_sql.getWritableDatabase();

        long id;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
                id = db.insertOrThrow(TABLE_MESSAGES, null, values);
                if (values.get(COL_TO) == null) {
                    db.execSQL("update contacts set county=county+1 where reg_name = ?", new Object[]{values.get(COL_FROM)});
                    getContext().getContentResolver().notifyChange(CONTENT_URI_PROFILE, null);
                }
                break;

            case CONTACTS_ALLROWS:
                id = db.insertOrThrow(TABLE_CONTACTS, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Uri insertUri = ContentUris.withAppendedId(uri, id);
        getContext().getContentResolver().notifyChange(insertUri, null);
        return insertUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = contact_sql.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case CONTACTS_ALLROWS:
                count = db.delete(getTableName(uri), selection, selectionArgs);
                break;

            case MESSAGES_SINGLE_ROW:
            case CONTACTS_SINGLE_ROW:
                count = db.delete(getTableName(uri), "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = contact_sql.getWritableDatabase();

        int count;
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case CONTACTS_ALLROWS:
                count = db.update(getTableName(uri), values, selection, selectionArgs);
                break;

            case MESSAGES_SINGLE_ROW:
            case CONTACTS_SINGLE_ROW:
                count = db.update(getTableName(uri), values, "_id = ?", new String[]{uri.getLastPathSegment()});
                break;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //SQL CLASS HELPER
    private static class Sqlite extends SQLiteOpenHelper {


        final static String TABLE_FEED = "feed";

        String contact_table = "CREATE TABLE Contacts" + "(" + "_id integer primary key autoincrement,"
                + "reg_id" + "text," + "reg_name" + " TEXT,"
                + "phone_number" + " TEXT" + ")";
        String feed_table = "CREATE TABLE " + TABLE_FEED + "("
                + "id" + " INTEGER PRIMARY KEY autoincrement, " + "status" + " TEXT" + ")";

        public Sqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //db.execSQL(contact_table);
            db.execSQL("create table messages (_id integer primary key autoincrement, msg text, reg_name text, reg_name2 text, at datetime default current_timestamp);");
            db.execSQL("create table contacts (_id integer primary key autoincrement, reg_id text unique, reg_name text,county integer default 0, phone_number text);");
            db.execSQL(feed_table);
            insertDemo(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(Sqlite.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            // Drop older table if existed
            db.execSQL("DROP TABLE IF EXISTS contacts");
            // Create tables again
            onCreate(db);
        }

        private void insertDemo(SQLiteDatabase db){
            ContentValues values = new ContentValues();
            values.put("reg_id", "3333");
            values.put("reg_name", "tunde");
            values.put("phone_number", "07944447710");
            db.insert("contacts", null, values);

            values = new ContentValues();
            values.put("reg_id", "336666");
            values.put("reg_name", "Jesz");
            values.put("phone_number", "02077084296");
            db.insert("contacts", null, values);

            values = new ContentValues();
            values.put("msg", "hi");
            values.put("reg_name", "Jesz");
            values.put("reg_name2", "Tunde");
            db.insert("messages", null, values);

            values = new ContentValues();
            values.put("msg", "hi");
            values.put("reg_name", "Tunde");
            values.put("reg_name2", "Jesz");
            db.insert("messages", null, values);
        }
    }

    private String getTableName(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case MESSAGES_ALLROWS:
            case MESSAGES_SINGLE_ROW:
                return TABLE_MESSAGES;

            case CONTACTS_ALLROWS:
            case CONTACTS_SINGLE_ROW:
                return TABLE_CONTACTS;
        }
        return null;
    }
}
