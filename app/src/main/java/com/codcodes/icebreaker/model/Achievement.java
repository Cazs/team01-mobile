package com.codcodes.icebreaker.model;

/**
 * Created by Casper on 2016/04/12.
 */
public class Achievement
{
    private String achName;
    private boolean achAchieved;
    private String achId;
    private String achDescription;
    private int achLevel;
    private int achTarget;
    private int achReward;

    public Achievement(String achName,boolean achAchieved,String achDescription,int achLevel,int achTarget,int achReward)
    {
        this.achName = achName;
        this.achAchieved = achAchieved;
        this.achDescription = achDescription;
        this.achLevel = achLevel;
        this.achTarget = achTarget;
        this.achReward = achReward;
        //TODO: achId = stripVowels(achName);
    }

    public Achievement(String achName,boolean achAchieved,String achId,String achDescription,int achLevel,int achTarget)
    {
        this.achName = achName;
        this.achAchieved = achAchieved;
        this.achId = achId;
        this.achDescription = achDescription;
        this.achLevel = achLevel;
        this.achTarget = achTarget;
        this.achReward = achReward;
    }

    //Accessors
    public String getAchName()
    {
        return this.achName;
    }

    public String getAchId()
    {
        return this.achId;
    }

    public String getAchDescription()
    {
        return this.achDescription;
    }

    public boolean getAchStatus()
    {
        return this.achAchieved;
    }

    //Mutators

    /**
     * Method to set the name of an Achievement.
     * @param achName - The new name to be set.
     */
    public void setAchName(String achName)
    {
        this.achName = achName;
    }

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
     * Method to set the description of an Achievement.
     * @param achDescription - The new name to be set.
     */
    public void setAchDescription(String achDescription)
    {
        this.achDescription = achDescription;
    }

    /**
     * Method to set the status of an Achievement.
     * @param achAchieved - The state to change to.
     */
    public void setAchAchieved(boolean achAchieved)
    {
        this.achAchieved = achAchieved;
    }

    public void setAchTarget(int achTarget) {
        this.achTarget = achTarget;
    }

    public void setAchLevel(int achLevel) {
        this.achLevel = achLevel;
    }

    public int getAchTarget() {
        return achTarget;
    }

    public int getAchLevel() {
        return achLevel;
    }

    public int getAchReward() {
        return achReward;
    }

    public void setAchReward(int achReward) {
        this.achReward = achReward;
    }
}
