package com.codcodes.icebreaker.model;

import android.util.Log;

import com.codcodes.icebreaker.auxilary.Config;

/**
 * Created by Casper on 2016/09/16.
 */
public class Metadata implements IJsonable
{
    private String entry;
    private String meta;

    private static String TAG = "IB/Metadata";

    public Metadata(){entry=null;meta=null;}

    public Metadata parse(String s)
    {
        String[] props = s.split(";");
        for (String prop : props)
        {
            if (prop.contains("="))
                setVarValue(prop.split("=")[0], prop.split("=")[1]);
        }
        return this;
    }

    public Metadata(String entry, String meta)
    {
        this.entry=entry;
        this.meta=meta;
    }

    public String getAttribute(String attribute)
    {
        String[] attrs = meta.split(Config.META_DELIM.getValue());
        for (String attr:attrs)
        {
            if (attr.contains("="))
            {
                if(attr.split("=")[0].equals(attribute))
                    return attr.split("=")[1];
            }
        }
        return null;
    }

    public Config compareDateModified(long date_modified)
    {
        String sdmd = getAttribute(Config.META_DATE_MODIFIED.getValue());
        if(sdmd!=null)
        {
            if (!sdmd.isEmpty())
            {
                try
                {
                    long dmd = Long.parseLong(sdmd);
                    if(date_modified>dmd)
                        return Config.META_PARAM_NEWER;
                    else return Config.META_PARAM_EQUAL;
                }
                catch (NumberFormatException e)
                {
                    if(e.getMessage()!=null)
                        Log.d(TAG,e.getMessage(),e);
                    else
                        e.printStackTrace();
                    return Config.META_CMP_ERR;
                }
            }else return Config.META_CMP_ERR;
        }else return Config.META_CMP_ERR;
    }

    public void setEntry(String entry){this.entry=entry;}
    public void setMeta(String meta){this.meta=meta;}

    public String getEntry(){return this.entry;}
    public String getMeta(){return this.meta;}

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var)
        {
            case "Entry":
                this.entry=value;
                break;
            case "Meta":
                this.meta=value;
                break;
            default:
                Log.d(TAG,"Unknown attribute: " + var);
                break;
        }
    }
}
