package com.codcodes.icebreaker;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * Created by tevin on 2016/07/13.
 */
public class EventsActivity extends android.support.v4.app.Fragment
{

    private ListView list;
    private static AssetManager mgr;
    private ArrayList<String> eventNames;
    private ArrayList<String> eventDescriptions;
    private ArrayList<Integer> eventIcons;
    private static final boolean DEBUG = true;
    private final String TAG = "ICEBREAK";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.event_page,container,false);
        list = (ListView) v.findViewById(R.id.list);;
        eventNames = new ArrayList<>();
        eventDescriptions = new ArrayList<>();
        eventIcons = new ArrayList<>();

        Thread eventsThread = new Thread(new Runnable()
       {
           @Override
           public void run()
           {
               ArrayList<Event> events = readEvents();
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
                   //for(int i =0;i<events.size();i++)
                   for(Event e:events)
                   {
                       eventNames.add(e.getTitle());
                       eventDescriptions.add(e.getDescription());
                       eventIcons.add(R.drawable.ultra_icon);//temporarily use this icon for all events
                   }

                   CustomListAdapter adapter = new CustomListAdapter(getActivity(),(String[])eventNames.toArray(),(Integer[])eventIcons.toArray(),(String[])eventDescriptions.toArray());
                   list.setAdapter(adapter);
                   Log.d(TAG,"Done reading events");
               }
           }
       });
        eventsThread.start();

        Typeface h = Typeface.createFromAsset(mgr,"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) v.findViewById(R.id.main_heading);
        headingTextView.setTypeface(h);

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
            pulsator.postDelayed(new Runnable() {
                @Override
                public void run() {
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
                // TODO Auto-generated method stub
                /*String Selcteditem = EventNames[+position];
                String eventDescrip = EventDescrp[+position];
                int imageID = imgid[+position];


                Intent intent = new Intent(view.getContext(),EventDetailActivity.class);
                intent.putExtra("Event Name",Selcteditem);
                intent.putExtra("Event Description",eventDescrip);
                intent.putExtra("Image ID",imageID);

                startActivity(intent);*/
            }
        });
        return v;
    }



    public static EventsActivity newInstance(Context context, Bundle b)
    {
        EventsActivity e = new EventsActivity();
        mgr = context.getAssets();
        e.setArguments(b);
        return e;
    }


    public ArrayList<Event> readEvents()
    {
            try
            {
                Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                Log.d(TAG,"Connection established");
                PrintWriter out = new PrintWriter(soc.getOutputStream());
                Log.d(TAG,"Sending request");

                out.print("GET /IBUserRequestService.svc/readEvent HTTP/1.1\r\n"
                        + "Host: icebreak.azurewebsites.net\r\n"
                        + "Content-Type: text/plain;\r\n"// charset=utf-8
                        + "Content-Length: 0\r\n\r\n");
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                String resp;
                //Wait for response indefinitely TODO: Time-Out
                while(!in.ready()){}

                String eventsJson = "";
                boolean openEventRead = false;
                while((resp = in.readLine())!=null)
                {
                    //if(DEBUG)System.out.println(resp);

                    if(resp.equals("0"))
                    {
                        out.close();
                        //in.close();
                        soc.close();
                        if(DEBUG)System.out.println(">>Done<<");
                        break;//EOF
                    }

                    if(resp.isEmpty())
                        if(DEBUG)System.out.println("\n\nEmpty Line\n\n");

                    if(resp.contains("["))
                    {
                        if(DEBUG)System.out.println("Opening at>>" + resp.indexOf("["));
                        openEventRead = true;
                    }

                    if(openEventRead)
                        eventsJson += resp;//.substring(resp.indexOf('['));

                    if(resp.contains("]"))
                    {
                        if(DEBUG)System.out.println("Closing at>>" + resp.indexOf("]"));
                        openEventRead = false;
                    }
                }

                if(DEBUG)System.out.println("Reading events.");
                //System.out.println(eventsJson);
                ArrayList<Event> events = getEvents(eventsJson);
                return events;
            }
            catch (UnknownHostException e)
            {
                System.err.println(e.getMessage());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
    }

    private static ArrayList<Event> getEvents(String json)
    {
        ArrayList<Event> events = new ArrayList<Event>();
        //remove square brackets
        json = json.replaceAll("\\[", "");
        json = json.replaceAll("\\]", "");
        //System.out.println("Processing: " + json);
        while(json.contains("{") && json.contains("}"))
        {
            int endPos = json.indexOf("}");
            int startPos = json.indexOf("{");
            System.out.println(startPos+" to " + endPos);
            String event = json.substring(startPos,endPos);//remove braces
            System.out.println("Event>>"+json.substring(startPos,endPos));
            json = json.substring(endPos, json.length());
            System.out.println("new json: " + json);
            Event ev = null;//getEvent(event);
            events.add(ev);
        }
        return events;
    }

    private static Event getEvent(String json)
    {
        System.out.println("Reading event: " + json);
        String p2 = "\"([a-zA-Z0-9\\s~`!@#$%^&*)(_+-={}\\[\\];',./\\|<>?]*)\"\\:(\"[a-zA-Z0-9\\s~`!@#$%^&*()_+-={}\\[\\];',./\\|<>?]*\"|\"[0-9,]\"|\\d+)";
        Pattern p = Pattern.compile(p2);
        Matcher m = p.matcher(json);
        Event ev = new Event();
        while(m.find())
        {
            String pair = m.group(0);
            //process key value pair
            pair = pair.replaceAll("\"", "");
            if(pair.contains(":"))
            {
                //if(DEBUG)System.out.println("Found good pair");
                String[] kv_pair = pair.split(":");
                String var = kv_pair[0];
                String val = kv_pair[1];
                switch(var)
                {
                    case "Title":
                        ev.setTitle(val);
                        //System.out.println(val);
                        break;
                    case "Id":
                        ev.setId(Integer.valueOf(val));
                        break;
                    case "Gps_Location":
                        ev.setGPS(val);
                        break;
                    case "Address":
                        ev.setAddress(val);
                        break;
                    case "Radius":
                        ev.setRadius(Integer.valueOf(val));
                        break;
                }
            }
            //look for next pair
            json = json.substring(m.end());
            m = p.matcher(json);
        }
        return ev;
    }



}
