package com.codcodes.icebreaker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
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

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by tevin on 2016/07/13.
 */
public class EventsActivity extends android.support.v4.app.Fragment
{

    private ListView list;
    private static AssetManager mgr;
    private static ArrayList<Event> events;
    private ArrayList<String> eventNames;
    private ArrayList<String> eventDescriptions;
    private ArrayList<String> eventIcons;
    private static final boolean DEBUG = true;
    private final String TAG = "ICEBREAK";
    private static boolean CHUNKED = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        validateStoragePermissions(getActivity());

        final View v = inflater.inflate(R.layout.event_page,container,false);
        list = (ListView) v.findViewById(R.id.list);
        eventNames = new ArrayList<>();
        eventDescriptions = new ArrayList<>();
        eventIcons = new ArrayList<>();

        Thread eventsThread = new Thread(new Runnable()
        {
           @Override
           public void run()
           {
               events = readEvents();
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
                       //Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                       //Log.d(TAG,"Connection established");
                       for(Event e:events)
                       {
                           eventNames.add(e.getTitle());
                           eventDescriptions.add(e.getDescription());
                           //eventIcons.add(R.drawable.ultra_icon);//temporarily use this icon for all events
                           String iconName = "event_icons-"+e.getId()+".png";
                           //String iconName = "event_icons-10.png";
                           eventIcons.add("/Icebreak/"+iconName);
                           //Download the file only if it has not been cached
                           if(!new File(Environment.getExternalStorageDirectory().getPath()+"/Icebreak/" + iconName).exists())
                           {
                               Log.d(TAG,"No cached "+iconName+",Image download in progress..");
                               if(imageDownload(iconName))
                                   Log.d(TAG,"Image download successful");
                               else
                                   Log.d(TAG,"Image download unsuccessful");
                           }
                       }
                   }
                   catch (IOException e)
                   {
                       e.printStackTrace();
                   }
                   /*String[] eventNamesArr = (String[])eventNames.toArray();
                   Integer[] eventIconsArr = (Integer[])eventIcons.toArray();
                   String[] eventDescriptionsArr = (String[])eventDescriptions.toArray();*/
                   String[] eventNamesArr = new String[events.size()];
                   String[] eventIconsArr = new String[events.size()];
                   String[] eventDescriptionsArr = new String[events.size()];
                   eventNamesArr = eventNames.toArray(eventNamesArr);
                   eventDescriptionsArr = eventDescriptions.toArray(eventDescriptionsArr);
                   eventIconsArr = eventIcons.toArray(eventIconsArr);

                   /*Object[] eventNamesArr = eventNames.toArray();
                   Object[] eventIconsArr = eventIcons.toArray();
                   Object[] eventDescriptionsArr = eventDescriptions.toArray();*/
                   Log.d(TAG,"Preparing to read events..");
                   final CustomListAdapter adapter = new CustomListAdapter(getActivity(),eventNamesArr,eventIconsArr,eventDescriptionsArr);
                   runOnUiThread(new Runnable()
                   {
                       @Override
                       public void run()
                       {
                           list.setAdapter(adapter);
                       }
                   });
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
                Event event = events.get(position);
                Toast.makeText(getActivity(),"Event: " + event.getTitle(),Toast.LENGTH_LONG).show();
                // TODO Auto-generated method stub
                /*String Selcteditem = EventNames[+position];
                String eventDescrip = EventDescrp[+position];
                int imageID = imgid[+position];
                */

                Intent intent = new Intent(view.getContext(),EventDetailActivity.class);
                intent.putExtra("Event Name",event.getTitle());
                intent.putExtra("Event Description",event.getDescription());
                intent.putExtra("Image ID",eventIcons.get(position));

                startActivity(intent);
            }
        });
        return v;
    }

    public void validateStoragePermissions(Activity activity)
    {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE =
                {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
        //Check for write permissions
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            //No permission - prompt the user for permission
            ActivityCompat.requestPermissions
                    (
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                    );
        }
    }

    public static boolean imageDownload(String iconName) throws IOException
    {
        Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
        System.out.println("Sending image download request");
        PrintWriter out = new PrintWriter(soc.getOutputStream());
        //Android: final String base64 = ;
        String headers = "GET /IBUserRequestService.svc/imageDownload/"+iconName+" HTTP/1.1\r\n"
                + "Host: icebreak.azurewebsites.net\r\n"
                //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Type: text/plain;\r\n"// charset=utf-8
                + "Content-Length: 0\r\n\r\n";
        out.print(headers);
        out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        String resp,base64;
        while(!in.ready()){}
        Pattern pattern = Pattern.compile("^[A-F0-9]+$");//"((\\d*[A-Fa-f]\\d*){2,}|\\d{1})");//"([0-9A-Fa-f]{2,}|[0-9]{1})");//"[0-9A-Fa-f]");
        //System.out.println(pattern.matcher("4FA3").find());
        //System.out.println(hexToDecimal("7D0"));
        String payload = "";
        while((resp = in.readLine())!=null)
        {
            //System.out.println(resp);
            if(resp.toLowerCase().contains("transfer-encoding"))
            {
                String encoding = resp.split(":")[1];
                if(encoding.toLowerCase().contains("chunked"))
                {
                    CHUNKED = true;
                    System.out.println("Preparing for chunked data.");
                }
            }

            if(CHUNKED)
            {
                Matcher m = pattern.matcher(resp.toUpperCase());
                if(m.find())
                {
                    int dec = hexToDecimal(m.group(0));
                    String chunk = in.readLine();
                    //char[] chunk = new char[dec];
                    //int readCount = in.read(chunk,0,chunk.length);//sjv3
                    //System.out.println(chunk);
                    //System.out.println("Chunk size: "+ readCount);
                    if(dec==0)
                        break;//End of chunks
                    if(chunk.length()>0)
                        payload += chunk;//String.copyValueOf(chunk);
                }
            }
        }
        out.close();
        //in.close();
        soc.close();
<<<<<<< HEAD
        if(payload.length()>0)
        {
            payload = payload.split(":")[1];
=======
        //System.out.println(payload);
        if(payload.length()>0)
        {
            //payload = payload.split(":")[1];
>>>>>>> e4f08746661cde9588c6e48ed983400256080138
            payload = payload.replaceAll("\"", "");
            System.out.println(payload);
            //payload = payload.substring(1,payload.length()-1);
            /*byte[] binFileArr = javax.xml.bind.DatatypeConverter.parseBase64Binary(payload);//Base64.getDecoder().decode(payload.getBytes());
            FileOutputStream fos = new FileOutputStream("remote_circle.png");
            fos.write(binFileArr);
            fos.close();
            System.out.println("Succesfully wrote to disk");//"\n>>>>>"+base64bytes);*/
            //System.out.println(payload);
            byte[] binFileArr = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);
            WritersAndReaders.saveImage(binFileArr,iconName);
            return true;
        }
        else
        {
            return false;
        }
    }

    public static int hexToDecimal(String hex)
    {
        String possibleDigits = "0123456789ABCDEF";
        int dec = 0;
        for(int i=0;i<hex.length();i++)
        {
            char currChar = hex.charAt(i);
            int x = possibleDigits.indexOf(currChar);
            dec = 16*dec + x;
        }
        return dec;
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

                out.print("GET /IBUserRequestService.svc/readEvents HTTP/1.1\r\n"
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
            String event = json.substring(startPos,endPos+1);//remove braces
            System.out.println("Event>>"+event);
            Event ev = getEvent(event);
            events.add(ev);
            /*if(!(json.contains("{") && json.contains("}")))
                break;*/
            System.out.println("JSON: " + json);
            if(json.length()>endPos+2)
                json = json.substring(endPos+2, json.length());
            else
                break;
            System.out.println("new json: " + json);
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
                    case "Description":
                        ev.setDescription(val);
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
