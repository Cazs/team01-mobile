package com.codcodes.icebreaker.screens;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.MessageListAdapter;

import java.util.List;

public class ChatListActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatlist);

        ListView messages = (ListView)findViewById(R.id.msgListView);
        MessageListAdapter messageListAdapter = new MessageListAdapter(this,new String[]{"Message A","Message B","Message C","Message D","Message E","Message F","Message G","Message H"});
        messages.setAdapter(messageListAdapter);

        messages.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                //TODO: DO stuff
            }
        });
    }
}
