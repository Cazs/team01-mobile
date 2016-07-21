package com.codcodes.icebreaker;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * Created by tevin on 2016/07/13.
 */
public class event extends android.support.v4.app.Fragment
{
    private ListView list;
    private static AssetManager mgr;
    private String[] EventNames =
            {
                    "UJ",
                    "Ultra",
                    "Mixer Party"


            };
    private String[] EventDescrp =
            {
                    "Rethink. Reimagine. Reinvent. ",
                    "Ultra Music Fesival",
                    "Party in your city..."


            };
    private Integer[] imgid=
            {
                    R.drawable.uj_icon,
                    R.drawable.ultra_icon,
                    R.drawable.mixer_icon

            };



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        final View v = inflater.inflate(R.layout.event_page,container,false);


        CustomListAdapter adapter = new CustomListAdapter(getActivity(),EventNames,imgid,EventDescrp);
        list=(ListView) v.findViewById(R.id.list);
        list.setAdapter(adapter);

        Typeface h = Typeface.createFromAsset(mgr,"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) v.findViewById(R.id.main_heading);
        headingTextView.setTypeface(h);

        Bundle extras = getArguments();
        final PulsatorLayout pulsator = (PulsatorLayout) v.findViewById(R.id.pulsator);
        System.out.println("Hello");
        if(extras!=null)
        {

            boolean check = extras.getBoolean("com.codcodes.icebreaker.Back");
            if (check)
            {
                pulsator.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }
        }
        else
        {

            pulsator.start();
            pulsator.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pulsator.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);

                }
            }, 10000);
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                // TODO Auto-generated method stub
                String Selcteditem = EventNames[+position];
                String eventDescrip = EventDescrp[+position];
                int imageID = imgid[+position];


                Intent intent = new Intent(view.getContext(),EventDetailActivity.class);
                intent.putExtra("Event Name",Selcteditem);
                intent.putExtra("Event Description",eventDescrip);
                intent.putExtra("Image ID",imageID);


                startActivity(intent);
            }
        });


        return v;
    }



    public static event newInstance(Context context,Bundle b)
    {
        event e = new event();
        mgr = context.getAssets();
        e.setArguments(b);
        return e;
    }


}
