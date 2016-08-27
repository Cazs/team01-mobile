package com.codcodes.icebreaker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.INTERVALS;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.NOTIFICATION_ID;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Casper on 2016/08/10.
 */
public class MessageFcmService extends FirebaseMessagingService//IntentService
{
    private final String TAG="IB/MsgPollService";

    public MessageFcmService()
    {
        System.err.println("FirebaseMessagingService:>_ starting..");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Map<String,String> notif_data = remoteMessage.getData();
        Log.d(TAG, "FCM Message Data: " + notif_data);
        String ib_req = notif_data.toString();
        ib_req = ib_req.substring(1,ib_req.length()-1);//get rid of braces
        String[] kv_pair = ib_req.split("=");
        if(kv_pair[0].equals("msg_id"))
        {
            //Get message from server
            try
            {
                String json_msg = RemoteComms.sendGetRequest("getMessageById/"+kv_pair[1]);
                ArrayList<Message> msgs = new ArrayList<Message>();
                JSON.<Message>getJsonableObjectsFromJson(json_msg, msgs, Message.class);

                //Decode message
                Message msg = msgs.get(0);
                switch (msg.getStatus())
                {
                    case 101:
                    case 102:
                        //Add user to local contacts
                        if(msg.getReceiver().equals(MainActivity.uhandle))
                        {
                            User rem_usr = LocalComms.getContact(getApplicationContext(),msg.getSender());
                            if(rem_usr==null)
                                rem_usr = RemoteComms.getUser(getApplicationContext(), msg.getSender());

                            //notify user
                            String name = LocalComms.getValidatedName(rem_usr);
                            LocalComms.showNotification(getApplicationContext(),name + " would like to get to know you.", NOTIFICATION_ID.NOTIF_REQUEST.getId());
                        }
                        break;
                    case 103:
                        if(msg.getSender().equals(MainActivity.uhandle))//local user got accepted
                        {
                            User rem_usr = LocalComms.getContact(getApplicationContext(),msg.getReceiver());
                            if(rem_usr==null)
                                rem_usr = RemoteComms.getUser(getApplicationContext(), msg.getReceiver());

                            //Save contact to disk
                            LocalComms.addContact(this, rem_usr);

                            //notify user
                            String name = LocalComms.getValidatedName(rem_usr);
                            LocalComms.showNotification(getApplicationContext(),name + " would also like to get to know you.", NOTIFICATION_ID.NOTIF_REQUEST.getId());
                        }
                        break;
                    case 104:
                        if(msg.getSender().equals(MainActivity.uhandle))//local user got rejected
                        {
                            User rem_usr = LocalComms.getContact(getApplicationContext(), msg.getReceiver());
                            if(rem_usr==null)
                                rem_usr = RemoteComms.getUser(getApplicationContext(), msg.getReceiver());

                            //notify user
                            String name = LocalComms.getValidatedName(rem_usr);
                            LocalComms.showNotification(getApplicationContext(),name + " is not in the mood right now, better luck next time ;)", NOTIFICATION_ID.NOTIF_REQUEST.getId());
                        }
                        break;
                }

                //Add message (or update status) on local DB
                LocalComms.addMessageToLocalDB(getApplicationContext(),msg);

                //System.err.println(json_msg);
            } catch (IOException e)
            {
                Log.d(TAG,e.getMessage());//TODO: better logging
                e.printStackTrace();
            }catch (InstantiationException e)
            {
                Log.d(TAG,e.getMessage());//TODO: better logging
                e.printStackTrace();
            }catch (IllegalAccessException e)
            {
                Log.d(TAG,e.getMessage());//TODO: better logging
                e.printStackTrace();
            }
        }
        /*Log.d(TAG, "FCM Notification Message: " +
                .getNotification().getBody());*/
    }

}
