package com.codcodes.icebreaker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by ghost on 2016/04/11.
 */
public class ProfileActivity extends Activity
{
    private boolean arrowUp = true;
    //private static int touchCounter = 0;
    MotionEvent ev;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //RelativeLayout rl = (RelativeLayout)findViewById(R.id.ppHolder);

        final ListView profile_settings = (ListView)findViewById(R.id.profile_settings);

        ProfileListItem items[] = {new ProfileListItem("Handle","@kwashington"),
                                    new ProfileListItem("Name","Kerry"),
                                    new ProfileListItem("Surname","Washington"),
                                    new ProfileListItem("Birth-date","1995-07-20")};
        profile_settings.setAdapter(new ProfileAdapter(this,items));

        final ImageView imgArrow = (ImageView)findViewById(R.id.imgArrow);
        final SlidingUpPanelLayout supl = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);


        supl.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset)
            {
                Log.d("IBE","Offset: " + slideOffset);
                if(slideOffset==1.0)
                    imgArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                else
                    imgArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState)
            {
                /*if(newState.name().equals(SlidingUpPanelLayout.PanelState.EXPANDED.name()))
                    //Log.d("IBE","Expanded");
                    imgArrow.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                else
                    imgArrow.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);*/
            }



        });


    }

    public void goBack(View v)
    {
        finish();
    }
}
