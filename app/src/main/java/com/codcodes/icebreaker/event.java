package com.codcodes.icebreaker;

/**
 * Created by tevin on 2016/07/25.
 */
public class Event
{
    private int id;
    private String title;
    private String description;
    private String address;
    private int radius;
    private String gps;

    public Event()
    {

    }

    public Event(int id, String title, String description, String address, int radius, String gps)
    {
        this.id = id;
        this.title=title;
        this.description = description;
        this.address = address;
        this.radius =  radius;
        this.gps = gps;
    }

    public int getId(){return this.id;}
    public String getTitle(){return this.title;}
    public String getDescription(){return this.description;}
    public String getAddress(){return this.address;}
    public int getRadius(){return this.radius;}
    public String getGPS(){return this.gps;}

    public void setId(int id){this.id = id;}
    public void setTitle(String title){this.title = title;}
    public void setDescription(String description){this.description = description;}
    public void setAddress(String address){this.address = address;}
    public void setRadius(int radius){this.radius = radius;}
    public void setGPS(String gps){this.gps = gps;}
}
