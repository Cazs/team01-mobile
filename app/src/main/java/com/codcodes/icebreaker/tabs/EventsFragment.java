package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.CustomListAdapter;
import com.codcodes.icebreaker.auxilary.EventsRecyclerViewAdapter;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.UserListRecyclerViewAdapter;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.EventDetailActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.screens.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * Created by tevin on 2016/07/13.
 */
public class EventsFragment extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static AssetManager mgr;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final String TAG = "IB/EventsFragment";
    private LinearLayout animContainer;
    private static EventsFragment eventsFrag;

    private RecyclerView recyclerView;
    private int mColumnCount = 1;
    private PulsatorLayout pulsator;
    private IOnListFragmentInteractionListener mListener;

    public static final int LOAD_LOCAL_EVENTS = 0;
    public static final int LOAD_REMOTE_EVENTS = 1;
    private Dialog dlgGpsHack;
    public static ArrayList<AbstractMap.SimpleEntry<String,LatLng>> debug_locations=new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        populateOverrideLocations();

        setLogoClickListener();
    }

    public void setLogoClickListener()
    {
        if(getActivity()!=null)
        {
            ImageView dbg_anim_logo = (ImageView) getActivity().findViewById(R.id.imgLogo);

            if (dbg_anim_logo != null)
            {
                dbg_anim_logo.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        dlgGpsHack = new Dialog(getActivity());
                        if (dlgGpsHack == null)
                            return false;

                        dlgGpsHack.setContentView(R.layout.gps_hack);
                        if (!dlgGpsHack.isShowing())
                            dlgGpsHack.show();

                        RadioButton rbtnAud = (RadioButton) dlgGpsHack.findViewById(R.id.rbtn_auditorium);
                        RadioButton rbtnStud = (RadioButton) dlgGpsHack.findViewById(R.id.rbtn_student_center);
                        RadioButton rbtnPond = (RadioButton) dlgGpsHack.findViewById(R.id.rbtn_pond);
                        RadioButton rbtnLib = (RadioButton) dlgGpsHack.findViewById(R.id.rbtn_library);

                        rbtnAud.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                mockLocation(debug_locations.get(0).getValue().latitude, debug_locations.get(0).getValue().longitude);
                            }
                        });

                        rbtnStud.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                mockLocation(debug_locations.get(1).getValue().latitude, debug_locations.get(0).getValue().longitude);
                            }
                        });

                        rbtnPond.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                mockLocation(debug_locations.get(2).getValue().latitude, debug_locations.get(0).getValue().longitude);
                            }
                        });

                        rbtnLib.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                mockLocation(debug_locations.get(3).getValue().latitude, debug_locations.get(0).getValue().longitude);
                            }
                        });

                        return true;
                    }
                });
            }
        }else Log.wtf(TAG,"MainActivity is null.");
    }

    public static void populateOverrideLocations()
    {
        debug_locations.add(new AbstractMap.SimpleEntry<>("Auditorium",new LatLng(-26.183261, 27.996542)));
        debug_locations.add(new AbstractMap.SimpleEntry<>("Student Center",new LatLng(-26.182587, 27.995996)));
        debug_locations.add(new AbstractMap.SimpleEntry<>("Pond",new LatLng(-26.183599, 27.997475)));
        debug_locations.add(new AbstractMap.SimpleEntry<>("Library",new LatLng(-26.182891, 27.997931)));
        debug_locations.add(new AbstractMap.SimpleEntry<>("Mill Junction",new LatLng(-26.182891, 27.997931)));
    }

    public void mockLocation(double lat, double lng)
    {
        String msg="Going to ["+lat+","+lng+"]";
        Toast.makeText(getActivity(),msg,Toast.LENGTH_LONG).show();

        Location mockLocation = new Location(MainActivity.mocLocationProvider); // a string
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lng);
        mockLocation.setTime(System.currentTimeMillis());

        //onLocationChanged(mockLocation);

        if(dlgGpsHack!=null)
            if(dlgGpsHack.isShowing())
                dlgGpsHack.hide();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_events,container,false);

        animContainer = (LinearLayout)v.findViewById(R.id.animContainer);
        pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        View rview = null;

        if(v != null)
            rview = v.findViewById(R.id.eventsList);

        if (rview instanceof RecyclerView)
        {
            Context context = v.getContext();
            recyclerView = (RecyclerView) rview;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
        }

        //Bundle extras = getArguments();
        /*if (animContainer != null)
            animContainer.setVisibility(View.VISIBLE);
        pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        pulsator.setInterpolator(PulsatorLayout.INTERP_LINEAR);
        pulsator.setDuration(4000);
        pulsator.start();
        reloadEvents(LOAD_LOCAL_EVENTS);*/

        setAdapter();
        return v;
    }

    public void setAdapter()
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run()
            {
                Looper.prepare();
                if (recyclerView != null)
                {
                    if(MainActivity.events==null)
                        MainActivity.events = new ArrayList<>();

                    //Check if list empty
                    if(MainActivity.events.isEmpty())
                    {
                        Event temp = new Event();
                        if(!pulsator.isStarted())
                            temp.setTitle(getString(R.string.msg_no_events));
                        Toast.makeText(getContext(),"No events found.",Toast.LENGTH_LONG).show();

                        Log.d(TAG, "Events list is empty.");
                        startPulsator();
                        //reloadEvents(LOAD_LOCAL_EVENTS);
                    }
                    //Set adapter
                    if(getActivity()!=null)
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (animContainer != null)
                                    animContainer.setVisibility(View.GONE);
                                if (pulsator != null)
                                    pulsator.stop();

                                recyclerView.setAdapter(new EventsRecyclerViewAdapter(MainActivity.events, MainActivity.bitmaps, mListener));
                                MainActivity.is_reloading_events=false;
                            }
                        });
                    }else Log.wtf(TAG,"MainActivity is null.");

                    if(getActivity()!=null)
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(swipeRefreshLayout!=null)
                                    swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }else Log.wtf(TAG,"MainActivity is null.");
                }
            }
        });
        t.start();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof IOnListFragmentInteractionListener)
        {
            mListener = (IOnListFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement IOnListFragmentInteractionListener");
        }
        //Toast.makeText(getActivity(),"EventsFragment has been attached.",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener=null;
    }

    public static EventsFragment newInstance(Context context, Bundle b)
    {
        if(eventsFrag==null)
            eventsFrag = new EventsFragment();
        if(context==null)
            return null;

        mgr = context.getAssets();

        if(eventsFrag.isHidden())
            eventsFrag.setArguments(b);

        return eventsFrag;
    }

    public void reloadEvents(final int src)
    {
        MainActivity.is_reloading_events=true;
        startPulsator();

        Thread eventsThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();

                while (MainActivity.mLastKnownLoc==null)
                {
                    //wait for location
                    Log.d(TAG,"Waiting for GPS location...");
                }

                //Attempt to load Events
                MainActivity.events = new ArrayList<>();
                try
                {
                    String eventIds;
                    if (MainActivity.loudness > 0)
                    {
                        eventIds = RemoteComms.sendGetRequest("getNearbyEventIdsByNoise/" + MainActivity.mLastKnownLoc.getLatitude()
                                + '/' + MainActivity.mLastKnownLoc.getLongitude() + '/' + MainActivity.range + '/' + MainActivity.loudness);
                    } else
                    {
                        eventIds = RemoteComms.sendGetRequest("getNearbyEventIds/" + MainActivity.mLastKnownLoc.getLatitude()
                                + '/' + MainActivity.mLastKnownLoc.getLongitude() + '/' + MainActivity.range);
                    }
                    eventIds=eventIds.replaceAll("\\[","");
                    eventIds=eventIds.replaceAll("\\]","");
                    eventIds=eventIds.replace("\"","");
                    eventIds=eventIds.replace('"','\0');
                    eventIds=eventIds.replaceAll("\\{","");
                    eventIds=eventIds.replaceAll("\\}","");

                    final String[] ids_arr = eventIds.split(",");
                    if(src==LOAD_LOCAL_EVENTS)
                    {
                        //Load local events
                        for (String id : ids_arr)
                        {
                            try
                            {
                                long ev_id = Long.parseLong(id);

                                Event event = LocalComms.getLocalEventRecord(getContext(), ev_id);
                                long now_in_sec = System.currentTimeMillis()/1000;
                                if(event!=null)
                                {
                                    if(now_in_sec<=event.getEndDate())
                                        MainActivity.events.add(event);
                                }
                            } catch (NumberFormatException e)
                            {
                                LocalComms.logException(e);
                            }
                        }
                    }else if(src==LOAD_REMOTE_EVENTS)
                    {
                        for (String id : ids_arr)
                        {
                            try
                            {
                                long ev_id = Long.parseLong(id);
                                Event event = LocalComms.getEvent(getContext(), ev_id);
                                MainActivity.events.add(event);
                            } catch (NumberFormatException e)
                            {
                                LocalComms.logException(e);
                            }
                            catch (IOException e)
                            {
                                LocalComms.logException(e);
                            }
                        }
                    }
                    setAdapter();//render events, no icons yet      d
                }catch (IOException e)
                {
                    LocalComms.logException(e);
                }
                    //Load remote events in the background
                    /*Thread tEventLoader = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ArrayList<Event> events = new ArrayList<>();
                            try
                            {
                                for (String id : ids_arr)
                                {
                                    try
                                    {
                                        long ev_id = Long.parseLong(id);
                                        Event event = LocalComms.getEvent(EventsFragment.this.getContext(), ev_id);
                                        events.add(event);
                                    } catch (NumberFormatException e)
                                    {
                                        LocalComms.logException(e);
                                    } catch (IOException e)
                                    {
                                        LocalComms.logException(e);
                                    }
                                }
                                //Replace main array contents with new and up to date ones and set adapter
                                if (!events.isEmpty())
                                {
                                    if (MainActivity.events==null)
                                        MainActivity.events = new ArrayList<>();

                                    MainActivity.events.clear();
                                    for(Event e:events)
                                        MainActivity.events.add(e);

                                    setAdapter();
                                }
                            }
                            catch (ConcurrentModificationException e)
                            {
                                LocalComms.logException(e);
                            }
                        }
                    });*/

                //Attempt to load bitmaps and set adapter
                if(MainActivity.events==null)
                {
                    Toast.makeText(getContext(),"Something went wrong while we were trying to read the events. Please reload.",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Something went wrong while we were trying to read the events.");
                    MainActivity.events = new ArrayList<>();
                }
                if(MainActivity.events.isEmpty())
                {
                    Toast.makeText(getContext(),"No events were found.",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"No events were found.");
                }

                MainActivity.bitmaps = new ArrayList<Bitmap>();
                try
                {
                    String iconName = "";
                    BitmapFactory.Options options = null;
                    for (Event e : MainActivity.events)
                    {
                        iconName = "event_icons-" + e.getId();
                        //Download the file only if it has not been cached
                        options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                        try
                        {
                            Bitmap bitmap = LocalComms.getImage(getContext(), iconName, ".png", "/events", options);

                            MainActivity.bitmaps.add(bitmap);
                        }
                        catch (IOException ex)
                        {
                            LocalComms.logException(ex);
                        }
                    }
                    if(getActivity()!=null)
                    {
                        getActivity().runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                setAdapter();
                            }
                        });
                    }
                }
                catch(ConcurrentModificationException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        eventsThread.start();
    }

    public void startPulsator()
    {
        if(getActivity()!=null)
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if(animContainer!=null)
                        animContainer.setVisibility(View.VISIBLE);

                    if(pulsator!=null)
                    {
                        pulsator.setInterpolator(PulsatorLayout.INTERP_LINEAR);
                        pulsator.setDuration(4000);
                        pulsator.start();
                    }
                    else Log.wtf(TAG,"***Pulsator is null.");
                }
            });
        }
    }

    @Override
    public void onRefresh()
    {
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(true);
        reloadEvents(LOAD_REMOTE_EVENTS);
    }
}
