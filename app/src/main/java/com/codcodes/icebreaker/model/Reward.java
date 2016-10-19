package com.codcodes.icebreaker.model;

/**
 * Created by MrSekati on 9/12/2016.
 */
public class Reward implements IJsonable
{
    private String rwId;
    private String rwName;
    private String rwDescription;
    private String rwCode;//Code used by Event manager to redeem reward for user
    private int rwCost;
    private String event_id;//Event that Reward is applicable to

    public Reward(String rwId, String rwName, String rwDescription, String rwCode, int rwCost, String event_id)
    {
        this.rwId = rwId;
        this.rwName = rwName;
        this.rwDescription = rwDescription;
        this.rwCode = rwCode;
        this.rwCost = rwCost;
        this.event_id=event_id;
    }

    public String getRwId() { return rwId;}

    public String getRwName() {
        return rwName;
    }

    public String getRwDescription() {
        return rwDescription;
    }

    public String getRwCode() {
        return rwCode;
    }

    public int getRwCost() {
        return rwCost;
    }

    public String getRwEventID() {
        return event_id;
    }


    public void setRwId(String rwId) {
        this.rwId = rwId;
    }

    public void setRwName(String rwName) {
        this.rwName = rwName;
    }

    public void setRwDescription(String rwDescription) {
        this.rwDescription = rwDescription;
    }

    public void setRwCode(String rwCode) {
        this.rwCode = rwCode;
    }

    public void setRwCost(int rwCost) {
        this.rwCost = rwCost;
    }

    public void setRwEventID(String event_id) {
        this.event_id = event_id;
    }

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var.toLowerCase())
        {
            case "id":
            {
                setRwId(value);
                break;
            }
            case "name":
            {
                setRwName(value);
                break;
            }
            case "description":
            {
                setRwDescription(value);
                break;
            }
            case "cost":
            {
                setRwCost(Integer.valueOf(value));
                break;
            }
            case "code":
            {
                setRwCode(value);
                break;
            }
            case "event_id":
            {
                setRwEventID(value);
                break;
            }
            default:
                System.err.println("Unknown Reward attribute '" + var + "'");
        }
    }

}
