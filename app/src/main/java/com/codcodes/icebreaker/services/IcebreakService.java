package com.codcodes.icebreaker.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.INTERVALS;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.LocationDetector;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.IBDialog;
import com.codcodes.icebreaker.screens.MainActivity;
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
    private boolean active = false;
    public static boolean status_changing = false;
    //private Dialog dialog;
    //public static Location lastKnownLoc = null;
    private double lat = 0;
    private double lng = 0;
    private static LatLng me = null;
    private final String TAG = "IB/IcebreakService";
    public  static int semaphore = 0;
    //private Handler mHandler;

    public IcebreakService()
    {
        super("IcebreakService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //mHandler = new Handler();//Bind to main/UI thread
        //validatePermissions();
        return super.onStartCommand(intent, flags, startId);
    }

    public static LatLng getMe()
    {
        return me;
    }

    /*private void validatePermissions()
    {
        LocationManager locationMgr;
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (AppCompatActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }*/

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //validatePermissions();
        if(intent!=null)// && mHandler!=null
        {
            try
            {
                //Check for IceBreaks indefinitely
                while (true)
                {
                    String stat = WritersAndReaders.readAttributeFromConfig(Config.DLG_ACTIVE.getValue());
                    if(stat!=null)
                        active = stat.toLowerCase().equals("true");
                    //Verify User location if at an Event as often as you update IceBreaks.
                    if(!active)//don't check when UI is visible
                    {
                        long ev_id = SharedPreference.getEventId(IcebreakService.this);
                        if (ev_id > 0)
                        {
                            //is at Event
                            Event e = RemoteComms.getEvent(ev_id);
                            if (e != null)
                            {
                                if (e.getBoundary() != null)
                                {
                                    //if(lastKnownLoc!=null)
                                    {
                                        me = new LatLng(lat, lng);
                                        if (!LocationDetector.containsLocation(me, e.getBoundary(), true))
                                        {
                                            System.out.println("Logging out of Event.");
                                            logOutUserFromEvent();
                                        } else Log.d(TAG, "**User location valid.");
                                    } //else Log.d(TAG, "Last known User location is null.");
                                } else Log.d(TAG, "Boundary for Event: " + ev_id + " is null.");
                            } else Log.d(TAG, "Event: " + ev_id + " is null,");
                        } else Log.d(TAG, "User not at a valid Event.");
                        Log.d(TAG, "Checking for local inbound and outbound Icebreaks.");
                        ArrayList<Message> messages = LocalComms.getInboundMessages(this,
                                SharedPreference.getUsername(this).toString());

                        //If there are IceBreaks
                        if (messages.size() > 0)
                        {
                            Log.d(TAG, "Found IceBreak/s.");

                            //Get first IceBreak
                            icebreak_msg = messages.get(0);

                            receiving_user = LocalComms.getContact(this, icebreak_msg.getReceiver());
                            if (receiving_user == null)//attempt to download user details
                                receiving_user = RemoteComms.getUser(this, icebreak_msg.getReceiver());

                            requesting_user = LocalComms.getContact(this, icebreak_msg.getSender());
                            if (requesting_user == null)//attempt to download user details
                                requesting_user = RemoteComms.getUser(this, icebreak_msg.getSender());

                            Log.d(TAG + "/IBC", "IBDialog active: " + active);//SharedPreference.isDialogActive(this));

                            //Always wait for pending message status changes to complete
                            //while (IBDialog.status_changing){System.err.println("IBDialog says> The status of an object is changing.");}
                            //if (!SharedPreference.isDialogActive(this))
                            //if(!LocalComms.getDlgStatus())
                            if (!active)
                            {
                                //Show IceBreak Dialog
                                Intent dlgIntent = new Intent(getApplicationContext(), IBDialog.class);
                                dlgIntent.putExtra("Message", icebreak_msg);
                                dlgIntent.putExtra("Receiver", receiving_user);
                                dlgIntent.putExtra("Sender", requesting_user);
                                dlgIntent.putExtra("Request_Code", String.valueOf(IBDialog.INCOMING_REQUEST));

                                dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //if(!LocalComms.getDlgStatus())
                                startActivity(dlgIntent);
                                //active=true;
                            }
                        } else Log.d(TAG, "<No local inbound IceBreaks>");

                        ArrayList<Message> out_messages = LocalComms.getOutboundMessages(this,
                                SharedPreference.getUsername(this).toString());
                        //Check for cases where local user is sender
                        if (out_messages.size() > 0)
                        {
                            Log.d(TAG + "/OBC", "IBDialog active: " + active);//SharedPreference.isDialogActive(this));
                            for (Message m : out_messages)
                            {
                                //TODO: send messages to server if they haven't been sent
                                //Always wait for pending message status changes to complete
                                //while (status_changing){System.err.println("IBDialog says> The status of an object is changing.");}
                                //if (!SharedPreference.isDialogActive(this))
                                //if(!LocalComms.getDlgStatus())
                                if (!active)
                                {
                                    //If local user has been accepted or rejected, show appropriate dialog
                                    if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus() ||
                                            m.getStatus() == MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
                                    {
                                        receiving_user = LocalComms.getContact(this, m.getReceiver());
                                        if (receiving_user == null)//attempt to download user details
                                            receiving_user = RemoteComms.getUser(this, m.getReceiver());

                                        requesting_user = LocalComms.getContact(this, m.getSender());
                                        if (requesting_user == null)//attempt to download user details
                                            requesting_user = RemoteComms.getUser(this, m.getSender());
                                        //Show dialog
                                        Intent dlgIntent = new Intent(getApplicationContext(), IBDialog.class);
                                        dlgIntent.putExtra("Message", m);
                                        dlgIntent.putExtra("Receiver", receiving_user);
                                        dlgIntent.putExtra("Sender", requesting_user);

                                        if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus())
                                            dlgIntent.putExtra("Request_Code", String.valueOf(IBDialog.RESP_ACCEPTED));
                                        if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
                                            dlgIntent.putExtra("Request_Code", String.valueOf(IBDialog.RESP_REJECTED));

                                        dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(dlgIntent);
                                    }
                                }
                            }
                        } else
                        {
                            Log.d(TAG, "<No local outbound IceBreaks>");
                        }
                    }else{Log.d(TAG,"UI is active, skipping checks.");}
                    //Take a break, take a KatKit
                    Thread.sleep(INTERVALS.IB_CHECK_DELAY.getValue());
                }
            }
            catch (IOException e)
            {
                if(e.getMessage()!=null)
                    Log.d(TAG,e.getMessage());
                else
                    e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                if(e.getMessage()!=null)
                    Log.d(TAG,e.getMessage());
                else
                    e.printStackTrace();
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
        //IcebreakService.lastKnownLoc = location;
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {
        Log.d(TAG,"onStatusChanged Status: " + s + " >" + i);
    }

    @Override
    public void onProviderEnabled(String s)
    {
        Log.d(TAG,"onProviderEnabled Status: " + s);
    }

    @Override
    public void onProviderDisabled(String s)
    {
        Log.d(TAG,"onProviderDisabled Status: " + s);
    }
}
