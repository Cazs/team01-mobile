package com.codcodes.icebreaker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Casper on 2016/04/09.
 */
public class AchievementsActivity extends Activity
{
    private AchievementsAdapter adapter;
    private ListView lstAchievements;
    private Achievement[] achievements = new Achievement[]
            {
                new Achievement("Popular Kid",false,"**Insert description here**"),
                new Achievement("Smooth Criminal",false,"**Insert description here**"),
                new Achievement("Star Of The Night/Day",false,"**Insert description here**"),//TODO: Programmatically figure out whether it should say Night/Day
                new Achievement("IceBreak Queen",false,"**Insert description here**"),
                new Achievement("Heart-breaker",false,"**Insert description here**"),
                new Achievement("Friendly",false,"**Insert description here**"),
                new Achievement("Popular Kid",false,"**Insert description here**"),
                new Achievement("Popular Kid",false,"**Insert description here**"),
                new Achievement("Popular Kid",false,"**Insert description here**"),
            };

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
        adapter = new AchievementsAdapter(achievements,this);

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
