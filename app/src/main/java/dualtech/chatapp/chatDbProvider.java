package dualtech.chatapp;

/**
 * Created by Jesz on 11-Aug-15.
 */
public class chatDbProvider {
    /** Chat history data provider
     * */
    String msg;
    int s_id;
    String date;
    chatDbProvider(String s, int i, String d){
        msg = s;
        s_id = i;
        date = d;
    }

}
