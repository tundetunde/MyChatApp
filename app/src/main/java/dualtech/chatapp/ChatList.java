package dualtech.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatList extends ListFragment implements View.OnClickListener{
    DbSqlite db;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_list, container, false);

        db = new DbSqlite(getActivity());
        ArrayList<String> chatList = (ArrayList)db.getChatList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1, chatList);
        setListAdapter(adapter);

        return v;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), ChatView.class);
        String s = l.getItemAtPosition(position).toString();
        intent.putExtra("contact", s);
        startActivity(intent);
    }
}