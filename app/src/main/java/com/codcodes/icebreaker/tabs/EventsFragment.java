package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.CustomListAdapter;
import com.codcodes.icebreaker.auxilary.EventsRecyclerViewAdapter;
import com.codcodes.icebreaker.auxilary.ImageConverter;
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

    private IOnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private int mColumnCount = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_events,container,false);

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

        reloadEvents();

        //Bundle extras = getArguments();
        /*final PulsatorLayout pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        if(extras!=null)
        {
            boolean check = extras.getBoolean("com.codcodes.icebreaker.Back");
            if (check)
            {
                pulsator.setVisibility(View.GONE);
                //list.setVisibility(View.VISIBLE);
            }
        }
        else
        {

            pulsator.start();
            pulsator.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    pulsator.setVisibility(View.GONE);
                    //list.setVisibility(View.VISIBLE);

                }
            }, 10000);
        }*/

        return v;
    }

    public void reloadEvents()
    {
        Thread eventsThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();

                events = new ArrayList<>();
                try
                {
                    String eventsJson = RemoteComms.sendGetRequest("readEvents");
                    JSON.<Event>getJsonableObjectsFromJson(eventsJson,events,Event.class);
                } catch (IOException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.wtf(TAG,e.getMessage());
                    //TODO: Better Logging
                }
                catch (java.lang.InstantiationException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                } catch (IllegalAccessException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                }
                if(events==null)
                {
                    //TODO: Notify user
                    Log.d(TAG,"Something went wrong while we were trying to read the events.");
                }
                else if(events.isEmpty())
                {
                    //TODO: Notify user
                    Log.d(TAG,"No events were found");
                }
                else//All is well
                {
                    final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
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

                            Bitmap bitmap = LocalComms.getImage(getContext(),iconName,".png","/events",options);
                            if(bitmap==null)
                                bitmap  = RemoteComms.getImage(getContext(), iconName, ".png", "/events", options);

                            bitmaps.add(bitmap);
                        }

                        Runnable runnable = new Runnable()
                        {
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
                                        ArrayList<Event> temp_lst = new ArrayList<Event>();
                                        temp_lst.add(temp);
                                        recyclerView.setAdapter(new EventsRecyclerViewAdapter(temp_lst, bitmaps, mListener));
                                        Log.d(TAG, "Events list is empty.");
                                    }
                                    else
                                    {
                                        recyclerView.setAdapter(new EventsRecyclerViewAdapter(events, bitmaps, mListener));
                                        Log.d(TAG, "Set events list.");
                                    }
                                }
                                if(swipeRefreshLayout!=null)
                                    swipeRefreshLayout.setRefreshing(false);
                            }
                        };
                        runOnUI(runnable);
                    }
                    catch(ConcurrentModificationException e)
                    {
                        if(e.getMessage()!=null)
                            Log.d(TAG, e.getMessage());
                        else
                            e.printStackTrace();
                    }
                }
            }
        });
        eventsThread.start();
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

    public void runOnUI(Runnable r)
    {
        if(EventsFragment.this!=null)
            if(EventsFragment.this.getActivity()!=null)
                EventsFragment.this.getActivity().runOnUiThread(r);
    }

    public static EventsFragment newInstance(Context context, Bundle b)
    {
        EventsFragment e = new EventsFragment();
        mgr = context.getAssets();
        e.setArguments(b);
        return e;
    }

    @Override
    public void onRefresh()
    {
        if(swipeRefreshLayout!=null)
            swipeRefreshLayout.setRefreshing(true);
        reloadEvents();
    }
}
