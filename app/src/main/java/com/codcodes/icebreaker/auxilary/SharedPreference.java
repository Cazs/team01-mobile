package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.codcodes.icebreaker.model.Event;

/**
 * Created by tevin on 2016/07/18.
 */
public class SharedPreference
{
    static final String USERNAME="username";
    static final String CURRENT_EVENT_SHARED_TITLE="event";
    static final String LAST_KNOWN_LOC="location";

    static SharedPreferences getSharedPreferences(Context ctx)
    {
        return PreferenceManager.getDefaultSharedPreferences(ctx);

    }

    public static void setUsername(Context ctx,String username)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(USERNAME,username);
        editor.commit();
    }

    public static void setEventId(Context ctx,long event_id)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putLong(CURRENT_EVENT_SHARED_TITLE,event_id);
        editor.commit();
    }

    public static void setLocation(Context ctx, String loc)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(LAST_KNOWN_LOC,loc);
        editor.commit();
    }

    public static String getUsername(Context context)
    {
        return getSharedPreferences(context).getString(USERNAME,"");
    }

    public static long getEventId(Context context)
    {
        return getSharedPreferences(context).getLong(CURRENT_EVENT_SHARED_TITLE,0);
    }

    public static String getLocation(Context context)
    {
        return getSharedPreferences(context).getString(LAST_KNOWN_LOC,"0.0,0.0");
    }

    public static void logOut(Context context)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }


}
