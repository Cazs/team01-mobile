package com.codcodes.icebreaker;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.ImageView;
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
public class event extends android.support.v4.app.Fragment
{

    private ListView list;
    private static AssetManager mgr;
    private String[] EventNames;
    private static final boolean DEBUG = false;
    private String[] EventDescrp;
    private Integer[] imgid=
            {
                    R.drawable.uj_icon,
                    R.drawable.ultra_icon,
                    R.drawable.mixer_icon

            };



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        final View v = inflater.inflate(R.layout.event_page,container,false);

        readEvents();
        System.out.println("Testing");

        CustomListAdapter adapter = new CustomListAdapter(getActivity(),EventNames,imgid,EventDescrp);
        list=(ListView) v.findViewById(R.id.list);
        list.setAdapter(adapter);

        Typeface h = Typeface.createFromAsset(mgr,"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) v.findViewById(R.id.main_heading);
        headingTextView.setTypeface(h);

        Bundle extras = getArguments();
        final PulsatorLayout pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        System.out.println("Hello");
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
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                // TODO Auto-generated method stub
                String Selcteditem = EventNames[+position];
                String eventDescrip = EventDescrp[+position];
                int imageID = imgid[+position];


                Intent intent = new Intent(view.getContext(),EventDetailActivity.class);
                intent.putExtra("Event Name",Selcteditem);
                intent.putExtra("Event Description",eventDescrip);
                intent.putExtra("Image ID",imageID);

                startActivity(intent);
            }
        });
        return v;
    }



    public static event newInstance(Context context,Bundle b)
    {
        event e = new event();
        mgr = context.getAssets();
        e.setArguments(b);
        return e;
    }


    public void readEvents()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                    System.out.println("Connection established");
                    PrintWriter out = new PrintWriter(soc.getOutputStream());
                    System.out.println("Sending request");


                    //Android: final String base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);

                    String data = "";//addEvent();

                    //out.print("GET /IBUserRequestService.svc/imageDownload/circle.png HTTP/1.1\r\n"
                    out.print("GET /IBUserRequestService.svc/readEvent HTTP/1.1\r\n"
                            //out.print("POST /IBUserRequestService.svc/addEvent HTTP/1.1\r\n"
                            + "Host: icebreak.azurewebsites.net\r\n"
                            //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                            + "Content-Type: text/plain;\r\n"// charset=utf-8
                            + "Content-Length: " + data.length() + "\r\n\r\n"
                            + data);
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                    String resp;
                    //while(!in.ready()){}
                    String eventsJson = "";
                    boolean openEventRead = false;
                    while((resp = in.readLine())!=null)
                    {
                        System.out.println(resp);

                        if(resp.contains("["))
                        {
                            if(DEBUG)System.out.println("Opening at>>" + resp.indexOf("["));
                            openEventRead = true;
                        }


                        if(openEventRead)
                            eventsJson += resp;

                        if(resp.contains("]"))
                        {
                            if(DEBUG)System.out.println("Closing at>>" + resp.indexOf("]"));
                            openEventRead = false;
                        }
                        if(resp.toLowerCase().contains("payload"))
                        {
                            String base64bytes = resp.split(":")[1];
                            base64bytes = base64bytes.substring(1, base64bytes.length());
                            //byte[] binFileArr = Base64.getDecoder().decode(base64bytes);
                            FileOutputStream fos = new FileOutputStream("remote_circle.png");
                            //fos.write(binFileArr);
                            fos.close();
                            System.out.println("Succesfully wrote to disk");//"\n>>>>>"+base64bytes);
                            return;
                        }

                        if(!in.ready())
                        {
                            //if(DEBUG)System.out.println(">>Done<<");
                            break;
                        }
                    }
                    ArrayList<EventDB> events = getEvents(eventsJson);
                    //TODO: DO STUFF WITH events object
                    for(int i =0;i<events.size();i++)
                    {
                        EventNames[i] = events.get(i).getTitle();
                        EventDescrp[i] = events.get(i).getDescription();

                    }
                    out.close();
                    //in.close();
                    soc.close();
                }
                catch (UnknownHostException e)
                {
                    System.err.println(e.getMessage());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();




    }

    private static ArrayList<EventDB> getEvents(String json)
    {
        ArrayList<EventDB> events = new ArrayList<EventDB>();
        //remove square brackets
        json = json.replaceAll("\\[", "");
        json = json.replaceAll("\\]", "");
        while(json.contains("{") && json.contains("}"))
        {
            int endPos = json.indexOf("}");
            String event = json.substring(json.indexOf("{")+1,endPos);//remove braces
            json = json.substring(endPos, json.length());
            EventDB ev = getEvent(event);
            events.add(ev);
        }
        return events;
    }

    private static EventDB getEvent(String json)
    {
        String p2 = "\"([a-zA-Z0-9\\s~`!@#$%^&*)(_+-={}\\[\\];',./\\|<>?]*)\"\\:(\"[a-zA-Z0-9\\s~`!@#$%^&*()_+-={}\\[\\];',./\\|<>?]*\"|\"[0-9,]\"|\\d+)";
        Pattern p = Pattern.compile(p2);
        Matcher m = p.matcher(json);
        EventDB ev = new EventDB();
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
