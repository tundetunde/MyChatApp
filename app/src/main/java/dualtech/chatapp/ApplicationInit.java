package dualtech.chatapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ApplicationInit extends Application {
    private static final String API_KEY = "AIzaSyDZ60w-JN-RzBHk1litPqzKtzqThmZnpaY";
    private static final String PROJECT_ID = "dual-digital-000";
    private static final String PROJECT_NO = "25515784135";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_MOB_ID = "mobile_number";
    public static final String PROPERTY_USER_NAME = "user_name";
    public static final String SHARED_PREF = "SharedPref";
    public static final String KEY_MSG_ID = "message_id";
    //public static final String SERVER_ADDRESS = "http://192.168.1.12:8080/ChatServerDual/GCMServer";
    public static final String SERVER_ADDRESS = "http://192.168.43.165:8080/ChatServerDual/GCMServer";
    public static final String PROFILE_NAME = "TUNDE";
    private static String REGISTRATION_KEY;
    private static String MOBILE_NUMBER;
    private static String USER;

    public static String getApi(){
        return API_KEY;
    }

    public static String getProjectNO(){ return PROJECT_NO;}

    public static void setMobile_number(String m){
        MOBILE_NUMBER = m;
    }

    public static String getMobile_number(){
        return MOBILE_NUMBER;
    }

    public static void setREGISTRATION_KEY(String r){
        REGISTRATION_KEY  = r;
    }

    public static String getREGISTRATION_KEY(){
        return REGISTRATION_KEY;
    }

    public static void setUser(String user) {
        USER = user;
    }

    public static String getUser() {
        return USER;
    }
}
