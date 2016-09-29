package com.codcodes.icebreaker.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Casper on 2016/07/23.
 */
public class Event implements IJsonable, Parcelable
{
    private long id;
    private String title;
    private String description;
    private String address;
    //private int radius;
    private int access_code;
    private ArrayList<LatLng> boundary = null;
    private String[] places = null;
    private long date=0;
    private long end_date=0;

    private final String TAG = "IB/Event";

    public Event() {}

    /*public Event(int id, String title, String description, String address, String gps,
                 int accessCode, String places, long date, long end_date)
    {
        this.id = id;
        this.title=title;
        this.description = description;
        this.address = address;
        //this.radius =  radius;
        setBoundary(gps);
        this.access_code = accessCode;
        setMeetingPlaces(places.split(";"));
        this.date=date;
        this.end_date=end_date;
    }*/

    public long getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getDescription(){return this.description;}
    public String getAddress(){return this.address;}
    //public int getRadius(){return this.radius;}
    public int getAccessCode() {
        return access_code;
    }
    public String[] getMeetingPlaces(){return this.places;}
    public ArrayList<LatLng> getBoundary(){return this.boundary;}
    public long getDate(){return date;}
    public long getEndDate(){return end_date;}

    public void setAccessCode(int accessCode) {
        this.access_code = accessCode;
    }
    public void setId(long id){this.id = id;}
    public void setTitle(String title){this.title = title;}
    public void setDescription(String description){this.description = description;}
    public void setAddress(String address){this.address = address;}
    //public void setRadius(int radius){this.radius = radius;}
    public void setBoundary(ArrayList<LatLng> boundary) {this.boundary = boundary;}
    public void setMeetingPlaces(String[] places){this.places=places;}
    public void setDate(String date)
    {
        try
        {
            this.date = Long.parseLong(date);
        }catch (NumberFormatException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
    }
    public void setEndDate(String date)
    {
        try
        {
            this.end_date = Long.parseLong(date);
        }catch (NumberFormatException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
    }
    public void setDate(long date){this.date=date;}
    public void setEndDate(long date){this.end_date=date;}
    public void setBoundary(String bounds)
    {
        if(bounds.contains(";"))
        {
            bounds = bounds.replaceAll(" ","");//remove spaces
            boundary = new ArrayList<>();
            String[] coords = bounds.split(";");
            for(String coord: coords)
            {
                if(!coord.contains(","))
                {
                    Log.wtf(TAG,"Invalid 'lat,lng' format.");
                    boundary = null;
                    return;
                }
                double lat = Double.valueOf(coord.split(",")[0]);
                double lng = Double.valueOf(coord.split(",")[1]);
                boundary.add(new LatLng( lat, lng));
            }
        }else Log.wtf(TAG,"Invalid 'lat,lng;lat,lng;lat,lng;...' format.");
    }

    public boolean isValid()
    {
        if(this==null)
            return false;
        if(this.id<=0)
            return false;
        if (this.title==null)
            return false;
        if(this.title.isEmpty())
            return false;
        if (this.access_code<=0)
            return false;
        if(String.valueOf(this.access_code).length()<4)
            return false;
        if(this.address==null)
            return false;
        if (this.address.isEmpty())
            return false;
        if (this.date<=0)
            return false;
        if(this.end_date<=0)
            return false;
        if(this.date>end_date)
            return false;
        if(this.boundary==null)
            return false;
        if (this.boundary.isEmpty())
            return false;
        if (this.places==null)
            return false;
        if(this.places.length<=0)
            return false;
        if (this.description==null)
            return false;
        if (this.description.isEmpty())
            return false;
        //Passes
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append("ID: " + String.valueOf(id) + "\n");
        s.append("Title: " + title + "\n");
        s.append("Description: " + description + "\n");
        s.append("Address: " + address + "\n");
        s.append("Date: " + String.valueOf(date) + "\n");
        s.append("End_Date: " + String.valueOf(end_date) + "\n");
        s.append("Access_Code: " + String.valueOf(access_code) + "\n");
        s.append("Meeting_Places: " + (places==null?"null":places.length) + "\n");
        s.append("Boundary: " + (boundary==null?"null":boundary.toString()) + "\n");
        return s.toString();
    }

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var.toLowerCase())
        {
            case "id":
                setId(Long.parseLong(value));
                break;
            /*case "radius":
                setRadius(Integer.valueOf(value));
                break;*/
            case "address":
                setAddress(value);
                break;
            case "description":
                setDescription(value);
                break;
            case "gps_location":
                setBoundary(value);
                break;
            case "title":
                setTitle(value);
                break;
            case "accesscode":
                setAccessCode(Integer.valueOf(value));
                break;
            case "meeting_places":
                if(value.contains(";"))
                    this.places = value.split(";");
                break;
            case "date":
                setDate(value);
                break;
            case "end_date":
                setEndDate(value);
                break;
            default:
                System.err.println("Event.class> Unknown attribute: " + var);
                break;
        }
    }

    public LatLng getOrigin()
    {
        double maxLat = 0.0;
        double maxLng = 0.0;
        double minLat = 0.0;
        double minLng = 0.0;

        if(boundary!=null)
        {
            if(!boundary.isEmpty())
            {
                if(boundary.size()==1)
                    return new LatLng(boundary.get(0).latitude,boundary.get(0).longitude);

                maxLat = boundary.get(0).latitude;
                maxLng = boundary.get(0).longitude;
                minLat = boundary.get(0).latitude;
                minLng = boundary.get(0).longitude;

                for (LatLng loc : boundary)
                {
                    if (loc.latitude < maxLat)
                        maxLat = loc.latitude;
                    else if (loc.latitude > minLat)
                        minLat = loc.latitude;

                    if (loc.longitude > maxLng)
                        maxLng = loc.longitude;
                    else if (loc.longitude < minLng)
                        minLng = loc.longitude;
                }
            }
        }
        return new LatLng(minLat+((maxLat-minLat)/2),minLng+((maxLng-minLng)/2));
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeLong(getId());
        parcel.writeString(getTitle());
        parcel.writeString(getDescription());
        parcel.writeString(getAddress());
        parcel.writeInt(getAccessCode());
        parcel.writeList(getBoundary());
        parcel.writeLong(getDate());
        parcel.writeLong(getEndDate());
        parcel.writeArray(getMeetingPlaces());
        //parcel.writeInt(getRadius());
    }

    //Used to regenerate Event object.
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>()
    {
        public Event createFromParcel(Parcel in)
        {
            Event e = new Event();
            e.setId(in.readLong());
            e.setTitle(in.readString());
            e.setDescription(in.readString());
            e.setAddress(in.readString());
            e.setAccessCode(in.readInt());
            e.setBoundary(in.readArrayList(Event.class.getClassLoader()));
            e.setDate(in.readLong());
            e.setEndDate(in.readLong());
            Object[] arr = in.readArray(Event.class.getClassLoader());
            if(arr.length>0)
            {
                String[] plcs = new String[arr.length];
                for(int i=0;i<arr.length;i++)
                    plcs[i]=(String)(plcs[i]);
                e.setMeetingPlaces(plcs);
            }
            //e.setRadius(in.readInt());

            return e;
        }

        public Event[] newArray(int size)
        {
            return new Event[size];
        }
    };
}
