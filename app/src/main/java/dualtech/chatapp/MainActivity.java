package dualtech.chatapp;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends TabActivity {

    public static TextView tvSignedIn;
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);

            tvSignedIn = (TextView) findViewById(R.id.tvSignedIn);
            Resources res = getResources();
            TabHost tabHost = getTabHost();
// Chat tab
            Intent ChatIntent = new Intent().setClass(this, ChatList.class);
            TabHost.TabSpec chatTab = tabHost
                    .newTabSpec("Chat")
                    .setIndicator("CHAT")
                            //.setIndicator("", res.getDrawable(R.drawable.icon_android_config))
                    .setContent(ChatIntent);
// Feed tab
            Intent FeedIntent = new Intent().setClass(this,FeedView.class);
            TabHost.TabSpec feedTab = tabHost
                    .newTabSpec("Feed")
                    .setIndicator("FEED")
                            //.setIndicator("", res.getDrawable(R.drawable.icon_apple_config))
                    .setContent(FeedIntent);
// Contact tab
            Intent contactIntent = new Intent().setClass(this,ContactView.class);
            TabHost.TabSpec contactTab = tabHost
                    .newTabSpec("Contacts")
                    .setIndicator("CONTACTS")
                            //.setIndicator("", res.getDrawable(R.drawable.icon_windows_config))
                    .setContent(contactIntent);

            tabHost.addTab(chatTab);
            tabHost.addTab(feedTab);
            tabHost.addTab(contactTab);

//set Chat tab as default (zero based)
            tabHost.setCurrentTab(0);

        }

}
