package dualtech.chatapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ApplicationInit extends Application {
    public static final int NOTIFICATION_ID = 0;
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_MOB_ID = "mobile_number";
    public static final String PROPERTY_USER_NAME = "user_name";
    public static final String PROPERTY_STATUS = "feed_status";
    public static final String SHARED_PREF = "SharedPref";
    public static final String KEY_MSG_ID = "message_id";
    public static final String PROPERTY_PHOTO = "photo" ;
    public static final String PROPERTY_CHAT_BG = "chat_bg";
    public static final String PROPERTY_CHAT_BG_URL = "chat_bg_url" ;
    //public static final String SERVER_ADDRESS = "http://88.105.47.134:8080/ChatServerDual/GCMServer"; //Tunde
    public static final String SERVER_ADDRESS = "http://100.71.199.173:8080/ChatServerDual/GCMServer"; //Tunde
    private static final String API_KEY = "AIzaSyDZ60w-JN-RzBHk1litPqzKtzqThmZnpaY";
    private static final String PROJECT_ID = "dual-digital-000";
    private static final String PROJECT_NO = "25515784135";
    private static SharedPreferences PREFS;
    private static SharedPreferences.Editor editor;
    private static String REGISTRATION_KEY;
    private static String MOBILE_NUMBER;
    private static String USER;

    public static int getMsgId() {
        int id = PREFS.getInt(ApplicationInit.KEY_MSG_ID, 0);
        editor.putInt(ApplicationInit.KEY_MSG_ID, ++id);
        editor.apply();
        return id;
    }

    public static String getProjectNO(){ return PROJECT_NO;}

    public static String getMobile_number(){
        MOBILE_NUMBER = PREFS.getString(PROPERTY_MOB_ID, null);
        return MOBILE_NUMBER;
    }

    public static void setMobile_number(String m){
        MOBILE_NUMBER = m;
        editor.putString(PROPERTY_MOB_ID, m);
        editor.apply();
    }

    public static String getREGISTRATION_KEY(){
        REGISTRATION_KEY = PREFS.getString(PROPERTY_REG_ID, null);
        return REGISTRATION_KEY;
    }

    public static void setREGISTRATION_KEY(String r){
        REGISTRATION_KEY  = r;
        editor.putString(PROPERTY_REG_ID, r);
        editor.apply();
    }

    public static String getUser() {
        USER = PREFS.getString(PROPERTY_USER_NAME, null);
        return USER;
    }

    public static void setUser(String user) {
        USER = user;
        editor.putString(PROPERTY_USER_NAME, user);
        editor.apply();
    }

    public static Boolean getChatBg() {
        return PREFS.getBoolean(PROPERTY_CHAT_BG, false);
    }

    public static void setChatBg(Boolean b){
        editor.putBoolean(PROPERTY_CHAT_BG, b);
        editor.apply();
    }

    public static String getChatBgURL() {
        return PREFS.getString(PROPERTY_CHAT_BG_URL, null);
    }

    public static void setChatBgURL(String s){
        editor.putString(PROPERTY_CHAT_BG_URL, s);
        editor.apply();
    }

    public static void storeUpdate(String  s){
        editor.putString(PROPERTY_STATUS, s);
        editor.apply();
    }

    public void onCreate(){
        super.onCreate();
        PREFS = getApplicationContext().getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        editor = PREFS.edit();
    }
}
