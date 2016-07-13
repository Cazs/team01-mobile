package com.codcodes.icebreaker;

import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by tevin on 2016/07/13.
 */
public class event extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        View v = inflater.inflate(R.layout.event_page,container,false);

        Typeface h = Typeface.createFromAsset(mgr,"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) v.findViewById(R.id.main_heading);
        headingTextView.setTypeface(h);


        return v;



    }


    public static event newInstance(Context context)
    {
        event e = new event();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }


}
