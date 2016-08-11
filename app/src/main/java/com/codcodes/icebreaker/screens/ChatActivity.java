package com.codcodes.icebreaker.screens;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity
{
    private ImageButton btnSend;
    private EditText txtMessage;
    private ListView messageList;
    private String otherUsername;
    private final String TAG = "IB/ChatActivity";

    private ArrayAdapter<String> messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        otherUsername = this.getIntent().getExtras().getString("Username");

        btnSend = (ImageButton)findViewById(R.id.sendMessageButton);
        txtMessage = (EditText)findViewById(R.id.sendText);
        messageList = (ListView)findViewById(R.id.msgListView);

        messageAdapter = new ArrayAdapter<String>(this,R.layout.activity_send_bubble,R.id.send_text);
        messageList.setAdapter(messageAdapter);


        //Retrieve messages from local db
        final ArrayList<Message> messages = new ArrayList<Message>();
        SQLiteDatabase db = new MessagePollHelper(this).getReadableDatabase();
        String[] projection =
                {
                        MessagePollContract.MessageEntry.COL_MESSAGE_SENDER,
                        MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER,
                        MessagePollContract.MessageEntry.COL_MESSAGE_STATUS,
                        MessagePollContract.MessageEntry.COL_MESSAGE_TIME,
                        MessagePollContract.MessageEntry.COL_MESSAGE
                };
        String selection =
                /*MessagePollContract.MessageEntry.COL_MESSAGE_SENDER+"=?"
                        + " AND " +
                        MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + "=?"
                        + " OR " +/*/
                        MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ?"
                        + " AND " +
                        MessagePollContract.MessageEntry.COL_MESSAGE_SENDER+" = ?";

        /*String[] selection_args = {SharedPreference.getUsername(this),otherUsername};//,SharedPreference.getUsername(getBaseContext()),otherUsername};
        Cursor c = db.query(
                MessagePollContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
              selection_args,
                null,
                null,
                null
        );*/
        String localUser = SharedPreference.getUsername(this);
        /*String query ="SELECT * FROM Message WHERE "+ MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ?"
                        + " AND " + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER +" = ?";// OR*/
        String query ="SELECT * FROM Message";/* WHERE "
                        + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER +" = ? AND "
                        + MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ? OR "
                        + MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ? AND "
                        + MessagePollContract.MessageEntry.COL_MESSAGE_SENDER +" = ?";// OR*/;

        Cursor c =  db.rawQuery(query, new String[] {/*otherUsername, localUser, otherUsername, localUser*/});
        //System.err.println("Message Count between["+SharedPreference.getUsername(getBaseContext())+" "+otherUsername+"]:" + String.valueOf(c.getCount()));
        //c.moveToFirst();
        while(c.moveToNext())
        {
            Message m = new Message();
            int id = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_ID));
            String sen = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER));
            String rec = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER));
            int status = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS));
            String time = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_TIME));
            String msg = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE));
            System.err.println(">>>>>>>>" +id);
            m.setMessage(msg);
            m.setStatus(status);
            m.setTime(time);
            m.setSender(sen);
            m.setReceiver(rec);
            messages.add(m);
            messageAdapter.add(m.getMessage());
        }
        db.close();

        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(txtMessage.getText() == null)
                    return;
                if(txtMessage.getText().length()<=0)
                    return;
                //Send message to server
                Thread tSender = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Looper.prepare();

                        ArrayList<AbstractMap.SimpleEntry<String,String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message",txtMessage.getText().toString()));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status","0"));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender","Ghost"));//TODO
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver","Aaron"));//TODO

                        //Send to server
                        try
                        {
                            int response_code = Restful.postData("addMessage",msg_details);
                            //Update UI
                            if(response_code == HttpURLConnection.HTTP_OK)
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        messageAdapter.add(txtMessage.getText().toString());
                                        txtMessage.setText(null);
                                    }
                                });
                            }
                            else
                                Toast.makeText(getBaseContext(),"Unable to send message: " + response_code, Toast.LENGTH_LONG).show();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                            Log.d(TAG,e.getMessage());
                            Toast.makeText(getBaseContext(),"Unable to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                tSender.start();
            }
        });
    }
}
