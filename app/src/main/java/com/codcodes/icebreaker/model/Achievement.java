package com.codcodes.icebreaker.model;

/**
 * Created by Casper on 2016/04/12.
 */
public class Achievement
{
    private String achId;
    private String achName;
    private String achDescription;
    private long achDate;
    private int achTarget;
    private int achValue;
    private int notified;

    public Achievement(String achId, String achName, String achDescription, long achDate, int achTarget, int achValue, int notified)
    {
        this.achId=achId;
        this.achName = achName;
        this.achDescription = achDescription;
        this.achDate=achDate;
        this.achTarget=achTarget;
        this.achValue=achValue;
        this.notified=notified;
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

    public void setAchDate(long achDate) {this.achDate = achDate;}

    public void setAchTarget(int achTarget) {this.achTarget = achTarget;}

    public void setAchValue(int achValue) {this.achValue = achValue;}

    public void setNotified(int notified) {this.notified = notified;}
}
