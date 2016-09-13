package com.codcodes.icebreaker.auxilary;

/**
 * Created by Casper on 2016/09/11.
 */
public enum Config
{
    DLG_ACTIVE("ib_dlg_active"),
    DLG_ACTIVE_TRUE("true"),
    DLG_ACTIVE_FALSE("false"),
    LOC_LAT("dev_loc_lat"),
    LOC_LNG("dev_loc_lng"),
    EVENT_ID("event_id");

    private String value;

    Config(String value){this.value=value;}

    public String getValue(){return this.value;}
}
