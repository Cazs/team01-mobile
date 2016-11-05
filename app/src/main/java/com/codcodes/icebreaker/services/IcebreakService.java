package com.codcodes.icebreaker.services;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/16.
 */
public class IcebreakService extends IntentService// implements LocationListener
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
    private Event e;
    private final String TAG = "IB/IcebreakService";
    //private Handler mHandler;

    public IcebreakService()
    {
        super("IcebreakService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //mHandler = new Handler();//Bind to main/UI thread
        return super.onStartCommand(intent, flags, startId);
    }

    public static LatLng getMe()
    {
        return me;
    }

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
                        long ev_id = 0;
                        try
                        {
                            String temp = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                            if (temp != null)
                                if (!temp.isEmpty() && !temp.equals("null"))
                                    ev_id = Long.parseLong(temp);
                        }catch (NumberFormatException e)
                        {
                            LocalComms.logException(e);
                        }
                        if (ev_id > 0)
                        {
                            //is at Event
                            if(e==null)
                                e = RemoteComms.getEvent(ev_id);
                            if (e != null)
                            {
                                if (e.getBoundary() != null)
                                {
                                    //if(lastKnownLoc!=null)
                                    {
                                        String str_lat = WritersAndReaders.readAttributeFromConfig(Config.LOC_LAT.getValue());
                                        if(str_lat!=null)
                                        {
                                            if(!str_lat.isEmpty() && !str_lat.equals("null"))
                                            {
                                                try
                                                {
                                                    lat = Double.parseDouble(str_lat);
                                                }catch (NumberFormatException ex)
                                                {
                                                    Log.wtf(TAG,"Not a number",ex);
                                                    RemoteComms.logOutUserFromEvent(this);
                                                }
                                            }
                                        }
                                        String str_lng = WritersAndReaders.readAttributeFromConfig(Config.LOC_LNG.getValue());
                                        if(str_lng!=null)
                                        {
                                            if(!str_lng.isEmpty() && !str_lng.equals("null"))
                                            {
                                                try {
                                                    lng = Double.parseDouble(str_lng);
                                                }catch (NumberFormatException ex)
                                                {
                                                    Log.wtf(TAG,"Not a number",ex);
                                                    RemoteComms.logOutUserFromEvent(this);
                                                }
                                            }
                                        }
                                        if(lat!=0&&lng!=0)
                                        {
                                            me = new LatLng(lat, lng);
                                            if (!LocationDetector.containsLocation(me, e.getBoundary(), true))
                                            {
                                                Log.wtf(TAG,"Logging out of Event.");
                                                RemoteComms.logOutUserFromEvent(this);
                                            } else Log.d(TAG, "**User location valid.");
                                        }else
                                        {
                                            Log.d(TAG, "User location hasn't been updated yet.");//TODO: if has been 0 for too long kickOut()
                                            //System.out.println("Logging out of Event.");
                                            //RemoteComms.logOutUserFromEvent(this);
                                        }
                                    } //else Log.d(TAG, "Last known User location is null.");
                                } else
                                {
                                    Log.d(TAG, "Boundary for Event: " + ev_id + " is null.");
                                    //System.out.println("Logging out of Event.");
                                    //RemoteComms.logOutUserFromEvent(this);
                                }
                            } else
                            {
                                Log.d(TAG, "Event: " + ev_id + " is null.");
                                //System.out.println("Logging out of Event.");
                                //RemoteComms.logOutUserFromEvent(this);
                            }
                            checkForIceBreaks();
                        } else
                        {
                            Log.d(TAG, "User not at a valid Event. Skipping IceBreak checks.");
                            //System.out.println("Logging out of Event.");
                            //RemoteComms.logOutUserFromEvent(this);
                        }
                    }else{Log.d(TAG,"UI is active, skipping checks.");}
                    //Take a break
                    Thread.sleep(INTERVALS.IB_CHECK_DELAY.getValue());
                }
            }catch (SocketTimeoutException e)
            {
                LocalComms.logException(e);
            }
            catch (IOException e)
            {
                LocalComms.logException(e);
            }
            catch (InterruptedException e)
            {
                LocalComms.logException(e);
            }
        }
        else
            Log.d(TAG,"intent==null:" + (intent==null));//"handler==null:" + (mHandler==null) +
    }

    public void checkForInboundIceBreaks() throws IOException
    {
        ArrayList<Message> messages = LocalComms.getInboundMessages(this, SharedPreference.getUsername(this).toString());

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
    }

    /*private void validatePermissions()
    {
        LocationManager locationMgr;
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding

            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this);
    }*/

    public void checkForOutboundIceBreaks() throws IOException
    {
        ArrayList<Message> out_messages = LocalComms.getOutboundMessages(this,
                SharedPreference.getUsername(this));
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
    }

    public void checkForIceBreaks() throws IOException
    {
        Log.d(TAG, "Checking for local inbound and outbound Icebreaks.");
        checkForInboundIceBreaks();
        checkForOutboundIceBreaks();
    }

    /*@Override
    public void onLocationChanged(Location location)
    {
        //IcebreakService.lastKnownLoc = location;
        lat = location.getLatitude();
        lng = location.getLongitude();
        System.err.println("#####################IServ:location changed!");
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
    }*/
}
