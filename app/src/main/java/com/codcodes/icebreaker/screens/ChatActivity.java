package com.codcodes.icebreaker.screens;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.codcodes.icebreaker.R;

public class ChatActivity extends AppCompatActivity
{
    private ImageButton send_btn;
    private EditText text;
    private ListView messageList;

    private ArrayAdapter<String> messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        send_btn = (ImageButton)findViewById(R.id.sendMessageButton);
        text = (EditText)findViewById(R.id.sendText);
        messageList = (ListView)findViewById(R.id.msgListView);

        messageAdapter = new ArrayAdapter<String>(this,R.layout.activity_send_bubble,R.id.send_text);
        messageList.setAdapter(messageAdapter);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageAdapter.add(text.getText().toString());
                text.setText(null);
            }
        });
    }
}
