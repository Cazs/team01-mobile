package com.codcodes.icebreaker.model;

import android.content.Intent;

/**
 * Created by Casper on 2016/04/12.
 */
public class Achievement implements IJsonable
{
    private String achId;
    private String achName;
    private String achDescription;
    private long achDate;
    private int achTarget;
    private int achValue;
    private int notified;
    private int user_points;
    private String method;

    public Achievement(String achId, String achName, String achDescription, long achDate, int achTarget, int achValue, int notified, int pts, String method)
    {
        this.achId=achId;
        this.achName = achName;
        this.achDescription = achDescription;
        this.achDate=achDate;
        this.achTarget=achTarget;
        this.achValue=achValue;
        this.notified=notified;
        this.user_points=pts;
        this.method=method;
    }

    public Achievement(String achId, String achName, String achDescription, long achDate, int achTarget, int achValue, int notified, int pts)
    {
        this.achId=achId;
        this.achName = achName;
        this.achDescription = achDescription;
        this.achDate=achDate;
        this.achTarget=achTarget;
        this.achValue=achValue;
        this.notified=notified;
        this.user_points=pts;
    }

    public Achievement(){}

    //Accessors
    public String getAchId()
    {
        return this.achId;
    }

    public String getAchName()
    {
        return this.achName;
    }

    public String getAchDescription()
    {
        return this.achDescription;
    }

    public long getAchDate() {return achDate;}

    public int getAchTarget() {return achTarget;}

    public int getAchValue() {return achValue;}

    public int getNotified() {return notified;}

    public int getUserPoints(){return this.user_points;}

    public String getAchMethod()
    {
        return this.method;
    }

    public boolean isAchieved() { return this.achDate>0;}

    //Mutators
    /**
     * Method to set the ID of an Achievement.
     * Currently has no real use, future maybe.
     * @param achId new ID to be set.
     */
    public void setAchId(String achId)
    {
        this.achId = achId;
    }

    /**
     * Method to set the name of an Achievement.
     * @param achName - The new name to be set.
     */
    public void setAchName(String achName)
    {
        this.achName = achName;
    }

    /**
     * Method to set the description of an Achievement.
     * @param achDescription - The new name to be set.
     */
    public void setAchDescription(String achDescription)
    {
        this.achDescription = achDescription;
    }

    public void setAchValue(int achValue)
    {
        this.achValue = achValue;
    }

    public void setAchDate(long achDate)
    {
        this.achDate = achDate;
    }

    public void setAchTarget(int achTarget)
    {
        this.achTarget = achTarget;
    }

    public void setAchUserPoints(int pts)
    {
        this.user_points = pts;
    }

    public void setNotified(int notified)
    {
        this.notified = notified;
    }

    public void setAchMethod(String method)
    {
        this.method = method;
    }

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var.toLowerCase())
        {
            case "id":
            {
                setAchId(value);
                break;
            }
            case "name":
            {
                setAchName(value);
                break;
            }
            case "description":
            {
                setAchDescription(value);
                break;
            }
            case "value":
            {
                setAchValue(Integer.valueOf(value));
                break;
            }
            case "target":
            {
                setAchTarget(Integer.valueOf(value));
                break;
            }
            case "dateachieved":
            {
                setAchDate(Long.valueOf(value));
                break;
            }
            case "pts":
            {
                setAchUserPoints(Integer.valueOf(value));
                break;
            }
            case "method":
            {
                setAchMethod(value);
                break;
            }
            default:
                System.err.println("Unknown Achievement attribute '" + var + "'");
        }
    }
}
