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
    EVENT_ID("event_id"),
    EVENT_MAX_DIST("max_distance"),
    USR_MIN_AGE("min_age"),
    USR_MAX_AGE("max_age"),
    USR_GEND("pref_gender"),
    EVENT_LOUDNESS("loudness"),
    META_DELIM(";"),
    META_DATE_MODIFIED("dmd"),
    META_PARAM_NEWER("1"),
    META_PARAM_EQUAL("0"),
    META_CMP_ERR("meta_compare_error");

    private String value;

    Config(String value){this.value=value;}

    public String getValue(){return this.value;}
}
