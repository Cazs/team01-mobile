package com.codcodes.icebreaker.auxilary;

import android.app.Activity;
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
import android.widget.Toast;

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
                //poll(b.getString("Username"), context);

                //Sync the local db with remote server db
                Log.d(TAG,">checkForInboundIcebreaks()");
                checkForInboundIcebreaks(b.getString("Username"), context);

                Log.d(TAG,">updateOutboundIcebreaks()");
                updateOutboundIcebreaks(b.getString("Username"),context);

                //Check for messages that still have to be sent
                Log.d(TAG,">checkForOutboundIcebreaks()");
                checkForOutboundIcebreaks(context);
            }
        });
        t.start();
        //System.err.println("Message received!!!!!");
    }

    public void checkForOutboundIcebreaks(Context context)
    {
        final String localUser = SharedPreference.getUsername(context);

        //First sync the local db with remote server db
        //updateOutboundIcebreaks(localUser, context);

        MessagePollHelper dbHelper = new MessagePollHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);//Create table if it doesn't exist

        //Check messages that haven't been ICEBREAK_DONE and haven't been READ
        String query ="SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE " +
                MessagePollContract.MessageEntry.COL_MESSAGE_SENDER +" = ? AND NOT " +
                MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND NOT " +
                MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ?";

        Cursor c =  db.rawQuery(query, new String[]
                {
                        localUser,
                        String.valueOf(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus()),
                        String.valueOf(MESSAGE_STATUSES.READ.getStatus())
                });
        while(c.moveToNext())
        {
            Message m = new Message();
            String id = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_ID));
            String send = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER));
            String recv = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER));
            int stat = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS));
            String time = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_TIME));
            String msg = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE));

            m.setId(id);
            m.setMessage(msg);
            m.setStatus(stat);
            m.setTime(time);
            m.setSender(send);
            m.setReceiver(recv);

            //messages.add(m);

            if(stat==MESSAGE_STATUSES.ICEBREAK.getStatus())//Icebreak sent but not received by server
            {
                //Check if the icebreak exists on the server
                //String response = Restful.sendGetRequest("getMessage/"+id);
                //Send Icebreak again
                if(Restful.sendMessage(context,m))
                    Log.d(TAG,"Message sent with status: " + MESSAGE_STATUSES.ICEBREAK);
                else
                    Log.d(TAG,"Message NOT sent with status: " + MESSAGE_STATUSES.ICEBREAK);
                //Get server messages where sender is local user and receiver is remote user
                //updateOutboundIcebreaks(localUser, rec,  context);
            }

            if(stat==MESSAGE_STATUSES.SENT.getStatus())//Message sent but not received by server
            {
                //Send message again
                if(Restful.sendMessage(context,m))
                    Log.d(TAG,"Message sent with status: " + MESSAGE_STATUSES.SENT);
                else
                    Log.d(TAG,"Message NOT sent with status: " + MESSAGE_STATUSES.SENT);
                //Get server messages where sender is local user and receiver is remote user
                //updateOutboundIcebreaks(localUser, rec,  context);
            }

            if(stat==MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus())//Icebreak sent but not received by server
            {
                //Notify user
                showNotification(context,"Accepted by: " + recv, NOTIFICATION_ID.NOTIF_ACCEPTED.getId());
                Log.d(TAG,"Accepted by: " + recv);
            }

            if(stat==MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())//Icebreak sent but not received by server
            {
                //Notify user
                showNotification(context,"Rejected by: " + recv, NOTIFICATION_ID.NOTIF_REJECTED.getId());
                Log.d(TAG,"Rejected by: " + recv);
            }
        }
        db.close();
    }

    public void checkForInboundIcebreaks(String receiver, Context context)
    {
        if (!receiver.isEmpty())
        {
            try
            {
                String response = Restful.sendGetRequest("checkUserInbox/" + receiver);

                if (response.length() > 0)
                {
                    String jsonMessages = response;
                    jsonMessages = jsonMessages.substring(jsonMessages.indexOf('['), jsonMessages.length() - 1);
                    ArrayList<Message> messages = new ArrayList<>();
                    JSON.getJsonableObjectsFromJson(jsonMessages, messages, Message.class);

                    if(messages.size()>0)
                    {
                        MessagePollHelper dbHelper = new MessagePollHelper(context);//getBaseContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        dbHelper.onCreate(db);

                        //Update local and remote DB
                        for (Message m : messages)
                        {
                            //Check for Icebreaks
                            if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_SERV_RECEIVED.getStatus() || m.getStatus() == MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus())
                            {
                                showNotification(context, "New IceBreak request from " + m.getSender(), NOTIFICATION_ID.NOTIF_REQUEST.getId());
                                Log.d(TAG, "New IceBreak request from " + m.getSender());
                                //m.setStatus(MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus());//set message to delivered in temp memory
                            }
                            if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_SERV_RECEIVED.getStatus())
                            {
                                showNotification(context, "New IceBreak request from " + m.getSender(), NOTIFICATION_ID.NOTIF_REQUEST.getId());
                                Log.d(TAG, "New IceBreak request from " + m.getSender());
                                m.setStatus(MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus());//set message to delivered in temp memory
                                //Change Message status to DELIVERED remotely
                                if(Restful.sendMessage(context,m))
                                {
                                    //Change Message status to DELIVERED locally
                                    updateLocalMessage(context,db,m);
                                    Log.d(TAG, "Updated local DB");
                                }
                                else
                                {
                                    Log.d(TAG, "Could not update remote message status to DELIVERED, therefore couldn't update local message either.");
                                }
                            }
                            else
                            {
                                Log.d(TAG, "IceBreak status: " + m.getStatus());
                            }
                            if (!messageIdExistsInDB(context, m.getId()))//new Message
                            {
                                ContentValues kv_pairs = new ContentValues();
                                kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, m.getId());
                                kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE, m.getMessage());
                                kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, m.getSender());
                                kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, m.getReceiver());
                                kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, m.getStatus());
                                kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, m.getTime());

                                long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
                                Log.d(TAG, "Inserted into Message table: new row=" + newRowId);
                            }
                            else //Existing Message
                            {
                                /*String query = "UPDATE " + MessagePollContract.MessageEntry.TABLE_NAME +
                                        " SET " + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS +
                                        " = ? WHERE " + MessagePollContract.MessageEntry.COL_MESSAGE_ID +
                                        " = ?";
                                db.rawQuery(query, new String[]{String.valueOf(m.getStatus()),
                                        String.valueOf(m.getId())});*/
                                updateLocalMessage(context,db,m);
                                Log.d(TAG, "Message already exists table, updated status");
                            }
                        }
                        db.close();
                    }
                    else
                        Log.d(TAG,"Remote mailbox is empty");
                }
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void updateOutboundIcebreaks(String sender, Context c)
    {
        if (!sender.isEmpty())
        {
            try
            {
                String response = Restful.sendGetRequest("checkUserOutbox/" + sender);

                if (response.length() > 0)
                {
                    String jsonMessages = response;
                    jsonMessages = jsonMessages.substring(jsonMessages.indexOf('['), jsonMessages.length() - 1);
                    ArrayList<Message> messages = new ArrayList<>();
                    JSON.getJsonableObjectsFromJson(jsonMessages, messages, Message.class);

                    MessagePollHelper dbHelper = new MessagePollHelper(c);//getBaseContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    // -- don't make sense - would be empty dbHelper.onCreate(db);//Create table if it doesn't exist
                    //Write to local DB
                    for(Message m: messages)
                    {
                        updateLocalMessage(c,db,m);
                        /*ContentValues kv_pairs = new ContentValues();
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, m.getId());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, m.getSender());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, m.getReceiver());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, m.getStatus());
                        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, m.getTime());

                        if(!messageIdExistsInDB(c,m.getId()))//Shouldn't technically ever happen
                        {

                            if (kv_pairs.size() > 0)
                            {
                                long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
                                Log.d(TAG, "Inserted into Message table: new row=" + newRowId);
                            }
                        }
                        else
                        {
                            /*String update = "UPDATE " + MessagePollContract.MessageEntry.TABLE_NAME +
                                    " SET " + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS +
                                    " = ? WHERE "+ MessagePollContract.MessageEntry.COL_MESSAGE_ID +
                                    " = ?";*
                            String where = MessagePollContract.MessageEntry.COL_MESSAGE_ID +
                                    " = ?";
                            String[] where_args = {m.getId()};

                            db.update(MessagePollContract.MessageEntry.TABLE_NAME, kv_pairs,where,where_args);
                            //db.execSQL(update);
                            /*db.rawQuery(query,new String[]{String.valueOf(m.getStatus()),
                                    m.getId()});*
                            Log.d(TAG, "Outbound Message exists (locally and remotely), updated status to " + m.getStatus());
                        }*/
                    }
                    db.close();
                }
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void updateLocalMessage(Context context, SQLiteDatabase db, Message m)
    {
        ContentValues kv_pairs = new ContentValues();
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, m.getId());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE, m.getMessage());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, m.getSender());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, m.getReceiver());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, m.getStatus());
        kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, m.getTime());

        if(!messageIdExistsInDB(context,m.getId()))//Shouldn't technically ever happen
        {

            if (kv_pairs.size() > 0)
            {
                long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
                Log.d(TAG, "Inserted into Message table: new row=" + newRowId);
            }
        }
        else
        {
            String where = MessagePollContract.MessageEntry.COL_MESSAGE_ID + " = ?";
            String[] where_args = {m.getId()};

            db.update(MessagePollContract.MessageEntry.TABLE_NAME, kv_pairs,where,where_args);
            Log.d(TAG, "Outbound Message exists (locally and remotely), updated status to " + m.getStatus());
        }
    }

    private boolean messageIdExistsInDB(Context ctxt, String id)
    {
        String query ="SELECT * FROM "+MessagePollContract.MessageEntry.TABLE_NAME+" WHERE "
                + MessagePollContract.MessageEntry.COL_MESSAGE_ID +" = ?";

        MessagePollHelper dbHelper = new MessagePollHelper(ctxt);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c =  db.rawQuery(query, new String[] {id});
        int rowCount=c.getCount();
        db.close();
        if(rowCount>0)
            return true;
        else
            return  false;
    }

    public void showNotification(Context context, String msg, int notifId)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle("IceBreak")
                        .setContentText(msg);
                        //.setContentText("New IceBreak request from "+sender+"!");

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
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager)
                                            context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notifId, mBuilder.build());
    }
}
