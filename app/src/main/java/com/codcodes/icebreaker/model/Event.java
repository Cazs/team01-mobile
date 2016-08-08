package com.codcodes.icebreaker.model;

/**
 * Created by tevin on 2016/07/25.
 */
public class Event implements IJsonable
{
    private int id;
    private String title;
    private String description;
    private String address;
    private int radius;
    private String gps;
    private int accessID;

    public Event()
    {

    }

    public Event(int id, String title, String description, String address, int radius, String gps,int accessID)
    {
        this.id = id;
        this.title=title;
        this.description = description;
        this.address = address;
        this.radius =  radius;
        this.gps = gps;
        this.accessID = accessID;
    }

    public int getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getDescription(){return this.description;}
    public String getAddress(){return this.address;}
    public int getRadius(){return this.radius;}
    public String getGPS(){return this.gps;}
    public int getAccessID() {
        return accessID;
    }

    public void setAccessID(int accessID) {
        this.accessID = accessID;
    }
    public void setId(int id){this.id = id;}
    public void setTitle(String title){this.title = title;}
    public void setDescription(String description){this.description = description;}
    public void setAddress(String address){this.address = address;}
    public void setRadius(int radius){this.radius = radius;}
    public void setGPS(String gps){this.gps = gps;}

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
                setGPS(value);
                break;
            case "Title":
                setTitle(value);
                break;
            case "AccessID":
                setAccessID(Integer.valueOf(value));
                break;
        }
    }
}
