package dualtech.chatapp;

/**
* Created by Jesz on 11-Aug-15.
*/
public class ChatDbProvider {
    /** Chat history data provider
     * */
    String msg;
    int s_id;
    String date;
    int status;
    ChatDbProvider(String s, int i, String d, int sid){
        msg = s;
        s_id = i;
        date = d;
        status = sid;
    }

}
