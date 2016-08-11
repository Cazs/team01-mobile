package com.codcodes.icebreaker.auxilary;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;
import com.codcodes.icebreaker.screens.IceBreakActivity;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/11.
 */
public class OnMessageReceive extends BroadcastReceiver
{
    private final String TAG = "IB/MessageReceiver";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        final Bundle b = intent.getExtras();
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                poll(b.getString("Username"), context);
            }
        });
        t.start();
        //System.err.println("Message received!!!!!");
    }

    public void poll(String username, Context ctxt)
    {
        IceBreakActivity ib;

        if(!username.isEmpty())
        {
            try
            {
                String response = Restful.sendGetRequest("checkUserInbox/" + username);

                if (response.length() > 0)
                {
                    String jsonMessages = response;
                    /*if (response.contains(":["))
                        jsonMessages = response.split(":")[1];*/
                    //jsonMessages.replace("{\"checkUserInboxResult\":","");
                    jsonMessages = jsonMessages.substring(jsonMessages.indexOf('['), jsonMessages.length() - 1);
                    ArrayList<Message> messages = new ArrayList<>();
                    JSON.getJsonableObjectsFromJson(jsonMessages, messages, Message.class);
                    //Write to local DB
                    MessagePollHelper dbHelper = new MessagePollHelper(ctxt);//getBaseContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    //db.execSQL(MessagePollContract.SQL_DELETE_tblMSG);
                    //db.execSQL(MessagePollContract.SQL_CREATE_tblMSG);
                    for (Message m : messages)
                    {
                        if(m.getStatus()==MESSAGE_STATUSES.ICEBREAK.getStatus()) {
                            //System.err.println("+++++++++Icebreaking+++++++");
                            showNotification(ctxt);
                        }
                        if(idExistsInDB(ctxt,m.getId()))
                        {
                            Log.d(TAG,m.getId()+" exists in DB.");
                            continue;
                        }

                        ContentValues kv_pairs = new ContentValues();
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, m.getId());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, m.getSender());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, m.getReceiver());
                        if(m.getStatus()<100)
                            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS,
                                (m.getStatus() == MESSAGE_STATUSES.SERV_RECEIVED.getStatus() ? MESSAGE_STATUSES.DELIVERED.getStatus() : m.getStatus()));
                        else//i.e. Icebreaking - send a special status
                            kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS,
                                    (m.getStatus() == MESSAGE_STATUSES.ICEBREAK.getStatus() ? MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus() : m.getStatus())
                            );
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, m.getTime());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE, m.getMessage());
                        //System.err.println("Msg=" + m.getMessage() + ", Sen=" + m.getSender() + ",Rec=" + m.getReceiver());

                        if (kv_pairs.size() > 0)
                        {
                            long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
                            Log.d(TAG, "Updated Message table: new row=" + newRowId);
                            //Send signal to sender to update delivery status
                            ArrayList<AbstractMap.SimpleEntry<String, String>> msg_pair = new ArrayList<>();
                            msg_pair.add(new AbstractMap.SimpleEntry<String, String>("Message_receiver", m.getReceiver()));
                            msg_pair.add(new AbstractMap.SimpleEntry<String, String>("Message_status",
                                    String.valueOf(MESSAGE_STATUSES.DELIVERED)));
                            int code = Restful.postData("updateUserMailbox", msg_pair);
                            if (code != HttpURLConnection.HTTP_OK)
                                Log.d(TAG, "Could not successfully update message status on server:" + code);
                        } else
                            Log.d(TAG, "Empty ContentValue map.");
                    }
                    db.close();
                }
                //TODO: Error handling
            }
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (SQLiteConstraintException e)
            {
                System.err.println(TAG + ":" + e.getMessage());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else Log.d(TAG,"Invalid username");//TODO: proper logging
    }

    private boolean idExistsInDB(Context ctxt, long id)
    {
        String query ="SELECT * FROM Message WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_ID +" = ?";

        MessagePollHelper dbHelper = new MessagePollHelper(ctxt);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c =  db.rawQuery(query, new String[] {String.valueOf(id)});
        int rowCount=c.getCount();
        db.close();
        if(rowCount>0)
            return true;
        else
            return  false;
    }

    public void showNotification(Context context)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("Icebreak")
                        .setContentText("You have a new Icebreak request!");

        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(42, mBuilder.build());
    }
}
