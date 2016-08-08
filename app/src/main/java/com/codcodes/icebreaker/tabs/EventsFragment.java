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
        validateStoragePermissions(getActivity());

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
               //events = readEvents();
               events = new ArrayList<>();
               try
               {
                   System.out.println("Preparing to read events...");
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
                       //Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                       //Log.d(TAG,"Connection established");
                       for(Event e:events)
                       {
                           eventNames.add(e.getTitle());
                           eventDescriptions.add(e.getDescription());
                           //eventIcons.add(R.drawable.ultra_icon);//temporarily use this icon for all events
                           String iconName = "event_icons-"+e.getId()+".png";
                           //String iconName = "event_icons-10.png";
                           eventIcons.add("/Icebreak/events/"+iconName);
                           //Download the file only if it has not been cached
                           if(!new File(Environment.getExternalStorageDirectory().getPath()+"/Icebreak/" + iconName).exists())
                           {
                               Log.d(TAG,"No cached "+iconName+",Image download in progress..");
                               if(imageDownloader(iconName, "/events"))
                                   Log.d(TAG,"Image download successful");
                               else
                                   Log.d(TAG,"Image download unsuccessful");
                           }
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
                   Log.d(TAG,"Preparing to apply events adapter...");
                   final CustomListAdapter adapter = new CustomListAdapter(getActivity(),eventNamesArr,eventIconsArr,eventDescriptionsArr);
                   runOnUiThread(new Runnable()
                   {
                       @Override
                       public void run()
                       {
                           list.setAdapter(adapter);
                           Log.d(TAG,"Done applying events");
                       }
                   });
               }
           }
       });
        eventsThread.start();

        /*Typeface h = Typeface.createFromAsset(mgr,"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) v.findViewById(R.id.main_heading);
        headingTextView.setTypeface(h);*/

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
                intent.putExtra("Event ID",event.getId());
                intent.putExtra("Access ID",event.getAccessID());

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

    public static boolean imageDownloader(String image, String destPath)
    {
        try
        {
            System.out.println("Attempting to download image: " + image);
            Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
            System.out.println("Connection established, Sending request..");
            PrintWriter out = new PrintWriter(soc.getOutputStream());
            //Android: final String base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
            String headers = "GET /IBUserRequestService.svc/imageDownload/"+image+" HTTP/1.1\r\n"
                    + "Host: icebreak.azurewebsites.net\r\n"
                    + "Content-Type: text/plain;charset=utf-8;\r\n\r\n";

            out.print(headers);
            out.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String resp, base64;
            while (!in.ready()) {
            }
            Pattern pattern = Pattern.compile("^[A-F0-9]+$");//"((\\d*[A-Fa-f]\\d*){2,}|\\d{1})");//"([0-9A-Fa-f]{2,}|[0-9]{1})");//"[0-9A-Fa-f]");
            String payload = "";
            while ((resp = in.readLine()) != null)
            {
                //System.out.println(resp);
                if (resp.toLowerCase().contains("400 bad request"))
                {
                    System.out.println("<<<400 bad request>>>");
                    return false;
                }
                if (resp.toLowerCase().contains("404 not found"))
                {
                    System.out.println("<<<404 not found>>>");
                    return false;
                }
                if (resp.toLowerCase().contains("transfer-encoding"))
                {
                    String encoding = resp.split(":")[1];
                    if (encoding.toLowerCase().contains("chunked"))
                    {
                        CHUNKED = true;
                        System.out.println("Preparing for chunked data.");
                    }
                }

                if (CHUNKED)
                {
                    Matcher m = pattern.matcher(resp.toUpperCase());
                    if (m.find())
                    {
                        int dec = hexToDecimal(m.group(0));
                        String chunk = in.readLine();
                        if (dec == 0)
                            break;//End of chunks
                        if (chunk.length() > 0)
                            payload += chunk;//String.copyValueOf(chunk);
                    }
                }
            }
            out.close();
            //in.close();
            soc.close();

            //System.out.println(payload);
            payload = payload.split(":")[1];
            payload = payload.replaceAll("\"", "");

            payload = payload.substring(0,payload.length()-1);
            if(!payload.equals("FNE"))
            {
                byte[] binFileArr = android.util.Base64.decode(payload, android.util.Base64.DEFAULT);//Base64.getDecoder().decode(payload.getBytes());
                WritersAndReaders.saveImage(binFileArr, destPath + "/" + image);
                System.out.println("Succesfully wrote to disk");//"\n>>>>>"+base64bytes);
                return true;
            }
            else
            {
                //TODO: Throw FileNotFoundException
                System.err.println("Server> File not found");
                return false;
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
            return  false;
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

    public static EventsFragment newInstance(Context context, Bundle b)
    {
        EventsFragment e = new EventsFragment();
        mgr = context.getAssets();
        e.setArguments(b);
        return e;
    }
}
