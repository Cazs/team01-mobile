package com.codcodes.icebreaker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

/**
 * Created by Casper on 2016/04/09.
 */
public class AchievementsActivity extends Activity
{
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
        AchievementsAdapter adapter = new AchievementsAdapter(new String[]{"Popular Kid",
                "Smooth Criminal",
                "Star Of The Night",
                "IceBreak Queen",
                "Heartbreaker",
                "Happy Person",
                "Achievement 1",
                "Achievement 2",
                "Achievement 3",
                "Achievement 4",
                "Achievement 5",
        },this);

        ListView listView = (ListView) findViewById(R.id.lstAchs);

        listView.setAdapter(adapter);
    }
}
