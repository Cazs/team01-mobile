package com.codcodes.icebreaker;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.codcodes.icebreaker.Edit_ProfileActivity;
import com.codcodes.icebreaker.InitialActivity;
import com.codcodes.icebreaker.LoginActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.RewardsActivity;
import com.codcodes.icebreaker.SharedPreference;

/**
 * Created by tevin on 2016/07/13.
 */
public class rewardsList extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;

    private String[] PeopleNames =
            {
                    "Selena Gomez",
                    "Lindsey Morgan"


            };
    private String[] PeopleDescrp =
            {
                    "They call me casanova",
                    "Where Dreams come true"


            };
    private Integer[] imgid=
            {
                    R.drawable.seleena,
                    R.drawable.seleena
            };

    private ListView lv;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        View v = inflater.inflate(R.layout.rewardlist_activity,container,false);
        String[] rewards = {"Top Drinker","winner At Life","This better work"};
        ListView lv = (ListView) v.findViewById(R.id.rewardsList);

        return v;


    }





    public static rewardsList newInstance(Context context)
    {
        rewardsList e = new rewardsList();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }
}
