package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.AchievementsAdapter;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.Achievement;

import java.io.IOException;

/**
 * Created by Casper on 2016/04/09.
 */
public class AchievementsActivity extends Activity
{
    private AchievementsAdapter adapter;
    private ListView lstAchievements;


    @Override
    //public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState)
    protected void onCreate(Bundle savedInstanceState)
    {
        //super.onCreate(savedInstanceState, persistentState);
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_achievements);

        /*String[] arr = new String[20];
        for(int i=0;i<20;i++)
            arr[i] = new String("Achievement " + (i+1));*/


        lstAchievements = (ListView) findViewById(R.id.lstAchs);
        lstAchievements.setAdapter(adapter);

        lstAchievements.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //TODO: Display MessageDialog
            }
        });
    }

    public void displayAchievements()
    {
        //TODO: Implement displayAchievements
    }

    public void displayAchievementDialog()
    {
        //TODO: Implement displayAchievementDialog
    }
}
