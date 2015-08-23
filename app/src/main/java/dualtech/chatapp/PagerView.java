package dualtech.chatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Jesz on 22-Aug-15.
 */
public class PagerView extends FragmentStatePagerAdapter {

    CharSequence Titles[]; //Titles of the Tabs
    int tabNum; //number of tabs

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public PagerView(FragmentManager fm,CharSequence tt[], int tn) {
        super(fm);
        this.Titles = tt;
        this.tabNum = tn;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new ChatList();
            case 1:
                return new FeedView();
            case 2:
                //return new ChatList();
                return new ContactView();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    @Override
    public int getCount() {
        return tabNum;
    }
}
