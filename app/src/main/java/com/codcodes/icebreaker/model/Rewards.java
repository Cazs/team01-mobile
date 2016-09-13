package com.codcodes.icebreaker.model;

/**
 * Created by MrSekati on 9/12/2016.
 */
public class Rewards {

    private String rwName;
    private boolean rwAchieved;
    private String rwId;
    private String rwDescription;

    public Rewards(String rwName,boolean rwAchieved,String rwId,String rwDescription)
    {
        this.rwName = rwName;
        this.rwAchieved = rwAchieved;
        this.rwId = rwId;
        this.rwDescription = rwDescription;
        //TODO: achId = stripVowels(achName);
    }

    public Rewards(String rwName,boolean rwAchieved,String rwDescription)
    {
        this.rwName = rwName;
        this.rwAchieved = rwAchieved;
        this.rwDescription = rwDescription;
        //TODO: achId = stripVowels(achName);
    }
    public String getRwDescription() {
        return rwDescription;
    }

    public void setRwDescription(String rwDescription) {
        this.rwDescription = rwDescription;
    }

    public String getRwId() {
        return rwId;
    }

    public void setRwId(String rwId) {
        this.rwId = rwId;
    }

    public boolean isRwAchieved() {
        return rwAchieved;
    }

    public void setRwAchieved(boolean rwAchieved) {
        this.rwAchieved = rwAchieved;
    }

    public String getRwName() {
        return rwName;
    }

    public void setRwName(String rwName) {
        this.rwName = rwName;
    }
}
