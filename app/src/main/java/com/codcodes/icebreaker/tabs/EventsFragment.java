package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    private static ArrayList<Event> events;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final String TAG = "IB/EventsFragment";
    private double location_lat=0.0;
    private double location_lng=0.0;
    private LinearLayout animContainer;
    private static EventsFragment e;
    //private ProgressDialog progress = null;

    private IOnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private int mColumnCount = 1;
    private ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
    //private ImageView imgAnim;
    private PulsatorLayout pulsator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_events,container,false);

        //imgAnim = (ImageView)v.findViewById(R.id.eventAnim);
        animContainer = (LinearLayout)v.findViewById(R.id.animContainer);

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

        /*if(recyclerView.getAdapter()==null)
            setAdapter();
        else Log.d(TAG,"Event Adapter is set.");

        beginLoadingAnim();*/
        Bundle extras = getArguments();
        pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        pulsator.setInterpolator(PulsatorLayout.INTERP_ACCELERATE_DECELERATE);
        pulsator.setDuration(2000);

        if(extras!=null)
        {
            boolean check = extras.getBoolean("com.codcodes.icebreaker.Back");
            if (check)
                setAdapter();
        }
        else
        {
            if(MainActivity.is_reloading_events) {

                try {
                    WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),null);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                pulsator.start();
            }
            else setAdapter();
        }
        return v;
    }

    public void animLogoClick(View view)
    {
        Toast.makeText(getContext(),"On click",Toast.LENGTH_LONG).show();
    }

    public void setAdapter()
    {
        if(animContainer!=null)
            animContainer.setVisibility(View.GONE);
        if(pulsator!=null)
            pulsator.stop();

        MainActivity.is_reloading_events=false;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run()
            {
                if (recyclerView != null)
                {
                    if(events==null)
                        events = new ArrayList<>();

                    if(events.isEmpty())
                    {
                        Event temp = new Event();
                        temp.setTitle(getString(R.string.msg_no_events));
                        final ArrayList<Event> temp_lst = new ArrayList<Event>();
                        temp_lst.add(temp);
                        if(getActivity()!=null)
                        {
                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    recyclerView.setAdapter(new EventsRecyclerViewAdapter(temp_lst, bitmaps, mListener));
                                }
                            });
                        }else Log.wtf(TAG,"MainActivity is null.");
                        Log.d(TAG, "Events list is empty.");
                    }
                    else
                    {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        if(bitmaps==null)
                            bitmaps=new ArrayList<>();
                        try
                        {
                            if (bitmaps.isEmpty())
                            {

                                for (Event e : events)
                                    bitmaps.add(LocalComms.getImage(getContext(), "event_icons-" + e.getId(), ".png", "/events", options));
                            }
                            if(getActivity()!=null)
                            {
                                getActivity().runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        recyclerView.setAdapter(new EventsRecyclerViewAdapter(events, bitmaps, mListener));
                                    }
                                });
                            }else Log.wtf(TAG,"MainActivity is null.");
                        }
                        catch (IOException e)
                        {
                            LocalComms.logException(e);
                        }
                        Log.d(TAG, "Set events list.");
                    }

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
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public void setEvents(ArrayList<Event> events){this.events=events;}

    public void setBitmaps(ArrayList<Bitmap> bitmaps){this.bitmaps=bitmaps;}

    public void setLat(double lat){this.location_lat=lat;}

    public void setLng(double lng){this.location_lng=lng;}

    public static EventsFragment newInstance(Context context, Bundle b, ArrayList<Event> events,
                                             ArrayList<Bitmap> bitmaps,double lat, double lng)
    {
        if(e==null)
            e = new EventsFragment();
        e.setEvents(events);
        e.setBitmaps(bitmaps);
        e.setLat(lat);
        e.setLng(lng);
        mgr = context.getAssets();
        if(!e.isAdded())
            e.setArguments(b);
        return e;
    }

    public void reloadEvents()
    {
        MainActivity.is_reloading_events=true;
        if(animContainer!=null)
            animContainer.setVisibility(View.VISIBLE);
        startPulsator();


        Thread eventsThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                //progress = LocalComms.showProgressDialog(getActivity(),"Loading Events...");
                while (location_lat==0.0||location_lng==0.0){}//wait for location

                //Attempt to load Events
                events = new ArrayList<>();
                try
                {
                    String eventIds = RemoteComms.sendGetRequest("getNearbyEventIds/" + location_lat
                            + '/' + location_lng + '/' + MainActivity.range);
                    eventIds=eventIds.replaceAll("\\[","");
                    eventIds=eventIds.replaceAll("\\]","");
                    eventIds=eventIds.replaceAll("\"","");
                    eventIds=eventIds.replaceAll("\\{","");
                    eventIds=eventIds.replaceAll("\\}","");

                    String[] ids_arr = eventIds.split(",");
                    for(String id:ids_arr)
                    {
                        try
                        {
                            long ev_id = Long.parseLong(id);
                            Event event = LocalComms.getEvent(getContext(), ev_id);
                            events.add(event);
                        }
                        catch (NumberFormatException e)
                        {
                            LocalComms.logException(e);
                        }
                    }
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }

                //Attempt to load bitmaps and set adapter
                if(events==null)
                {
                    Toast.makeText(getContext(),"Something went wrong while we were trying to read the events. Please reload.",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Something went wrong while we were trying to read the events.");
                    events = new ArrayList<>();
                }
                if(events.isEmpty())
                {
                    Toast.makeText(getContext(),"No events were found.",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"No events were found.");
                }

                bitmaps = new ArrayList<Bitmap>();
                try
                {
                    String iconName = "";
                    BitmapFactory.Options options = null;
                    for (Event e : events)
                    {
                        iconName = "event_icons-" + e.getId();
                        //Download the file only if it has not been cached
                        options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                        try
                        {
                            Bitmap bitmap = LocalComms.getImage(getContext(), iconName, ".png", "/events", options);

                            bitmaps.add(bitmap);
                        }
                        catch (IOException ex)
                        {
                            LocalComms.logException(ex);
                        }
                    }
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            setAdapter();
                        }
                    });
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
        if(this.pulsator!=null)
            this.pulsator.start();
    }

    @Override
    public void onRefresh()
    {
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(true);
        reloadEvents();
    }
}
