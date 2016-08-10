package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.screens.EventDetailActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;

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

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by tevin on 2016/07/13.
 */
public class EventsFragment extends android.support.v4.app.Fragment
{
    private ListView list;
    private static AssetManager mgr;
    private static ArrayList<Event> events;
    private ArrayList<String> eventNames;
    private ArrayList<String> eventDescriptions;
    private ArrayList<String> eventIcons;
    private static final boolean DEBUG = false;
    public static final String TAG = "IB/EventsFragment";
    private static boolean CHUNKED = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_events,container,false);
        list = (ListView) v.findViewById(R.id.list);
        eventNames = new ArrayList<>();
        eventDescriptions = new ArrayList<>();
        eventIcons = new ArrayList<>();

        Thread eventsThread = new Thread(new Runnable()
        {
           @Override
           public void run()
           {
               events = new ArrayList<>();
               try
               {
                   String eventsJson = Restful.getJsonFromURL("readEvents");
                   JSON.<Event>getJsonableObjectsFromJson(eventsJson,events,Event.class);
               } catch (IOException e)
               {
                   Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                   Log.d(TAG,e.getMessage());
                   //TODO: Error Logging
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
                   try
                   {
                       for (Event e : events)
                       {
                           eventNames.add(e.getTitle());
                           eventDescriptions.add(e.getDescription());
                           String iconName = "event_icons-" + e.getId();
                           eventIcons.add("/Icebreak/events/" + iconName + ".png");
                           //Download the file only if it has not been cached
                           if (!new File(Environment.getExternalStorageDirectory().getPath() + "/Icebreak/events/" + iconName + ".png").exists()) {
                               Log.d(TAG, "No cached " + iconName + ",Image download in progress..");
                               if (Restful.imageDownloader(iconName, ".png", "/events", getActivity()))
                                   Log.d(TAG, "Image download successful");
                               else
                                   Log.d(TAG, "Image download unsuccessful");
                           }
                       }
                       String[] eventNamesArr = new String[events.size()];
                       String[] eventIconsArr = new String[events.size()];
                       String[] eventDescriptionsArr = new String[events.size()];
                       eventNamesArr = eventNames.toArray(eventNamesArr);
                       eventDescriptionsArr = eventDescriptions.toArray(eventDescriptionsArr);
                       eventIconsArr = eventIcons.toArray(eventIconsArr);

                       final CustomListAdapter adapter = new CustomListAdapter(getActivity(), eventNamesArr, eventIconsArr, eventDescriptionsArr);
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               list.setAdapter(adapter);
                               Log.d(TAG, "Set events list");
                           }
                       });
                   }
                   catch(ConcurrentModificationException e)
                   {
                       Log.d(TAG, e.getMessage());
                   }
               }
           }
        });
        eventsThread.start();

        Bundle extras = getArguments();
        final PulsatorLayout pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        if(extras!=null)
        {
            boolean check = extras.getBoolean("com.codcodes.icebreaker.Back");
            if (check)
            {
                pulsator.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
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
                    list.setVisibility(View.VISIBLE);

                }
            }, 10000);
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG,"Clicked on item: " + position);
                Event event = events.get(position);
                // TODO Auto-generated method stub
                /*String Selcteditem = EventNames[+position];
                String eventDescrip = EventDescrp[+position];
                int imageID = imgid[+position];
                */

                Intent intent = new Intent(view.getContext(),EventDetailActivity.class);
                intent.putExtra("Event Name",event.getTitle());
                intent.putExtra("Event Description",event.getDescription());
                intent.putExtra("Image ID",eventIcons.get(position));
                intent.putExtra("Event ID",event.getId());
                intent.putExtra("Access ID",event.getAccessID());

                startActivity(intent);
            }
        });
        return v;
    }

    public static EventsFragment newInstance(Context context, Bundle b)
    {
        EventsFragment e = new EventsFragment();
        mgr = context.getAssets();
        e.setArguments(b);
        return e;
    }
}
