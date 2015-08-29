package dualtech.chatapp;

/**
 * Created by Jesz on 29-Aug-15.
 */

public class Contact{
    String number , name;

    Contact(String n, String num){
        number = num;
        name = n;
    }

    @Override
    public String toString(){
        return name;
    }
}
