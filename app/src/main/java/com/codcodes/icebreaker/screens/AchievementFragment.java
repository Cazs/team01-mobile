package com.codcodes.icebreaker.screens;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.AchievementsAdapter;
import com.codcodes.icebreaker.model.Achievement;

import java.util.ArrayList;

/**
 * Created by MrSekati on 9/12/2016.
 */
public class AchievementFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private ListView list;
    private AchievementsAdapter adapter;
    private ArrayList<Achievement> achList;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_achievement,container,false);

        achList = new ArrayList<Achievement>();
        achList.add(new Achievement("Popular Kid",false,"**Insert description here**"));
        achList.add(new Achievement("Party Animal",false,"**Insert description here**"));
        achList.add(new Achievement("Star Of The Night/Day",false,"**Insert description here**"));
        achList.add(new Achievement("IceBreak Queen/King",false,"**Insert description here**"));
        achList.add(new Achievement("Heart-breaker",false,"**Insert description here**"));
        achList.add(new Achievement("Friendly",false,"**Insert description here**"));
        list = (ListView) v.findViewById(R.id.achievementList);
        adapter = new AchievementsAdapter(getContext(),achList,0);
        list.setAdapter(adapter);
        return v;
    }

    public static AchievementFragment newInstance(Context context)
    {
        AchievementFragment e = new AchievementFragment();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }
}
