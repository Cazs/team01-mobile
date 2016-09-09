package com.codcodes.icebreaker.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by tevin on 2016/07/25.
 */
public class Event implements IJsonable
{
    private long id;
    private String title;
    private String description;
    private String address;
    private int radius;
    private int access_code;
    private ArrayList<LatLng> boundary = null;
    private final String TAG = "IB/Event";

    public Event() {}

    public Event(int id, String title, String description, String address, int radius, String gps,int accessCode)
    {
        this.id = id;
        this.title=title;
        this.description = description;
        this.address = address;
        this.radius =  radius;
        this.access_code = accessCode;
    }

    public long getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getDescription(){return this.description;}
    public String getAddress(){return this.address;}
    public int getRadius(){return this.radius;}
    public int getAccessCode() {
        return access_code;
    }
    public ArrayList<LatLng> getBoundary(){return this.boundary;}

    public void setAccessCode(int accessCode) {
        this.access_code = accessCode;
    }
    public void setId(long id){this.id = id;}
    public void setTitle(String title){this.title = title;}
    public void setDescription(String description){this.description = description;}
    public void setAddress(String address){this.address = address;}
    public void setRadius(int radius){this.radius = radius;}
    public void setBoundary(String bounds)
    {
        if(bounds.contains(";"))
        {

            boundary = new ArrayList<>();
            String[] coords = bounds.split(";");
            for(String coord: coords)
            {
                if(!coord.contains(":"))
                {
                    Log.wtf(TAG,"Invalid lat:lng format.");
                    boundary = null;
                    return;
                }
                double lat = Double.valueOf(coord.split(":")[0]);
                double lng = Double.valueOf(coord.split(":")[1]);
                boundary.add(new LatLng( lat, lng));
            }
        }else Log.wtf(TAG,"Invalid lat:lng;lat:lng;lat:lng format.");
    }

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var)
        {
            case "Id":
                setId(Integer.valueOf(value));
                break;
            case "Radius":
                setRadius(Integer.valueOf(value));
                break;
            case "Address":
                setAddress(value);
                break;
            case "Description":
                setDescription(value);
                break;
            case "Gps_location":
                setBoundary(value);
                break;
            case "Title":
                setTitle(value);
                break;
            case "AccessID"://TODO: AccessCode
                setAccessCode(Integer.valueOf(value));
                break;
            default:
                System.err.println("Event.class> Unknown attribute: " + var);
                break;
        }
    }
}
