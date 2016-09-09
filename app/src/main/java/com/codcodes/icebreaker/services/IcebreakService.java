package com.codcodes.icebreaker.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.codcodes.icebreaker.auxilary.INTERVALS;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.LocationDetector;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.IBDialog;
import com.codcodes.icebreaker.screens.MainActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/16.
 */
public class IcebreakService extends IntentService implements LocationListener
{
    //private Context context = null;
    private static Message icebreak_msg = new Message();
    private static User requesting_user = new User();
    private static User receiving_user = new User();
    private static Location lastKnownLoc = null;

    private final String TAG = "IB/ListenerService";
    //private Handler mHandler;

    public IcebreakService()
    {
        super("IcebreakService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //mHandler = new Handler();//Bind to main/UI thread
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if(intent!=null)// && mHandler!=null
        {
            try
            {
                //Check for IceBreaks indefinitely
                while (true)
                {
                    //Verify User location if at an Event as often as you update IceBreaks.
                    long ev_id = SharedPreference.getEventId(IcebreakService.this);
                    if(ev_id>1)
                    {
                        //is at Event
                        Event e = RemoteComms.getEvent(ev_id);
                        if (e != null)
                        {
                            if(e.getBoundary()!=null)
                            {
                                LatLng curr = new LatLng(lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude());
                                if (!LocationDetector.containsLocation(curr, e.getBoundary(), true))
                                {
                                    System.out.println("Logging out of Event.");
                                    //logOutUserFromEvent();
                                }else Log.d(TAG,"User location valid.");
                            }else Log.d(TAG,"Boundary for Event: "+ev_id+" is null.");
                        }else Log.d(TAG,"Event: "+ev_id+" is null,");
                    }else Log.d(TAG,"User not at a valid Event.");
                    Log.d(TAG,"Checking for local inbound and outbound Icebreaks.");
                    ArrayList<Message> messages = LocalComms.getInboundMessages(this,
                            SharedPreference.getUsername(this).toString());

                    //If there are Icebreaks
                    if (messages.size() > 0)
                    {
                        Log.d(TAG, "Found IceBreak/s.");

                        //Get first IceBreak
                        icebreak_msg = messages.get(0);

                        receiving_user = LocalComms.getContact(this,icebreak_msg.getReceiver());
                        if (receiving_user == null)//attempt to download user details
                            receiving_user = RemoteComms.getUser(this,icebreak_msg.getReceiver());

                        requesting_user = LocalComms.getContact(this,icebreak_msg.getSender());
                        if (requesting_user == null)//attempt to download user details
                            requesting_user = RemoteComms.getUser(this,icebreak_msg.getSender());

                        Log.d(TAG+"/IBC", "IBDialog active: " + IBDialog.active);

                        //Always wait for pending message status changes to complete
                        while (IBDialog.status_changing){System.err.println("IBDialog says> The status of an object is changing.");}
                        if (!IBDialog.active)
                        {
                            //Show IceBreak Dialog
                            Intent dlgIntent = new Intent(getApplicationContext(), IBDialog.class);
                            dlgIntent.putExtra("Message", icebreak_msg);
                            dlgIntent.putExtra("Receiver", receiving_user);
                            dlgIntent.putExtra("Sender", requesting_user);
                            IBDialog.request_code = IBDialog.INCOMING_REQUEST;

                            dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(dlgIntent);
                        }
                    }
                    else Log.d(TAG,"<No local inbound IceBreaks>");

                    ArrayList<Message> out_messages = LocalComms.getOutboundMessages(this,
                            SharedPreference.getUsername(this).toString());
                    //Check for cases where local user is sender
                    if(out_messages.size()>0)
                    {
                        Log.d(TAG+"/OBC", "IBDialog active: " + IBDialog.active);
                        for(Message m: out_messages)
                        {
                            //TODO: send messages to server if they haven't been sent
                            //Always wait for pending message status changes to complete
                            while (IBDialog.status_changing){System.err.println("IBDialog says> The status of an object is changing.");}
                            if (!IBDialog.active)
                            {
                                //If local user has been accepted or rejected, show appropriate dialog
                                if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus() ||
                                        m.getStatus() == MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
                                {
                                    receiving_user = LocalComms.getContact(this,m.getReceiver());
                                    if (receiving_user == null)//attempt to download user details
                                        receiving_user = RemoteComms.getUser(this,m.getReceiver());

                                    requesting_user = LocalComms.getContact(this,m.getSender());
                                    if (requesting_user == null)//attempt to download user details
                                        requesting_user = RemoteComms.getUser(this,m.getSender());
                                    //Show dialog
                                    Intent dlgIntent = new Intent(getApplicationContext(), IBDialog.class);
                                    dlgIntent.putExtra("Message", m);
                                    dlgIntent.putExtra("Receiver", receiving_user);
                                    dlgIntent.putExtra("Sender", requesting_user);
                                    if(m.getStatus()==MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus())
                                        IBDialog.request_code = IBDialog.RESP_ACCEPTED;
                                    if(m.getStatus()==MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
                                        IBDialog.request_code = IBDialog.RESP_REJECTED;

                                    dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(dlgIntent);
                                }
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG,"<No local outbound IceBreaks>");
                    }
                        /*mHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {

                            }
                        });*/
                    //Take a break, take a KatKit
                    Thread.sleep(INTERVALS.IB_CHECK_DELAY.getValue());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
            }
        }
        else
            Log.d(TAG,"intent==null:" + (intent==null));//"handler==null:" + (mHandler==null) +
    }

    public void logOutUserFromEvent() throws IOException
    {
        MainActivity.event = null;
        MainActivity.event_id = 0;
        MainActivity.users_at_event = null;
        SharedPreference.setEventId(this, 0);
        //Update status on server
        User u = LocalComms.getContact(this,SharedPreference.getUsername(this));
        if(u==null)
            u = RemoteComms.getUser(this,SharedPreference.getUsername(this));
        if(u!=null)
        {
            Event e = new Event();
            e.setId(0);
            u.setEvent(e);
            RemoteComms.postData("userUpdate/"+u.getUsername(),u.toString());
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        this.lastKnownLoc = location;
    }
}
