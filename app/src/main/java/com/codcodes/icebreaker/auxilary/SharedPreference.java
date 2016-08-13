package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by tevin on 2016/07/18.
 */
public class SharedPreference
{
    static final String USERNAME="username";
    static final String CURRENT_EVENT_ID = "event_id";

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
        editor.putString(CURRENT_EVENT_ID,String.valueOf(event_id));
        editor.commit();
    }

    public static String getUsername(Context context)
    {
        return getSharedPreferences(context).getString(USERNAME,"");
    }

    public static String getEventId(Context context)
    {
        return getSharedPreferences(context).getString(CURRENT_EVENT_ID,"");
    }

    public static void logOut(Context context)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.commit();
    }


}
