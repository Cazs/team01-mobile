package com.codcodes.icebreaker.screens;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.AchievementsAdapter;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.User;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MrSekati on 9/12/2016.
 */
public class AchievementFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private ListView list;
    private AchievementsAdapter adapter;
    //private ArrayList<Achievement> achList;
    private String counter = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_achievement,container,false);
        list = (ListView) v.findViewById(R.id.achievementList);
        setAdapter();
        return v;
    }

    public void setAdapter()
    {
        if(RewardsActivity.achievements!=null)
        {
            if(!RewardsActivity.achievements.isEmpty())
            {
                for(Achievement ach : RewardsActivity.achievements)
                {
                    setAchScore(ach);
                }
                adapter = new AchievementsAdapter(getActivity(),RewardsActivity.achievements,0);
                if(adapter!=null && list!=null && getActivity()!=null)
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            list.setAdapter(adapter);
                        }
                    });
                }
            }
            else Toast.makeText(getActivity(), "No Achievements found, or they are still loading.",Toast.LENGTH_LONG).show();
        }
    }

    public static AchievementFragment newInstance(Context context)
    {
        AchievementFragment e = new AchievementFragment();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }

    private void functionA()
    {
        final String username = SharedPreference.getUsername(this.getContext());;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = (RemoteComms.sendGetRequest("getUserIcebreakCount/"+ username));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void functionB()
    {
        final String username = SharedPreference.getUsername(this.getContext());;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = (RemoteComms.sendGetRequest("getUserSuccessfulIcebreakCount/"+ username));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private void functionC()
    {
        final String username = SharedPreference.getUsername(this.getContext());;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = (RemoteComms.sendGetRequest("getMaxUserIcebreakCountAtOneEvent/"+ username));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void functionD()
    {
        final String username = SharedPreference.getUsername(this.getContext());;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = (RemoteComms.sendGetRequest("getMaxUserSuccessfulIcebreakCountAtOneEvent/"+ username));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private void functionE()
    {
        final String username = SharedPreference.getUsername(this.getContext());;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = (RemoteComms.sendGetRequest("getUserIcebreakCountXHoursApart/"+ username + "/" + 2));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private void functionF()
    {
        final String username = SharedPreference.getUsername(this.getContext());;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = (RemoteComms.sendGetRequest("getUserSuccessfulIcebreakCountXHoursApart/"+ username + "/" + 1));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void functionAB()
    {
        final String username = SharedPreference.getUsername(this.getContext());;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = Integer.valueOf(RemoteComms.sendGetRequest("getUserIcebreakCount/"+ username));
                    int success = Integer.valueOf(RemoteComms.sendGetRequest("getUserSuccessfulIcebreakCount/"+ username));
                    total -= success;
                    counter = String.valueOf(total);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    private void setAchScore(Achievement ach)
    {

            switch(ach.getAchMethod())
            {
                case "A":
                {
                    functionA();
                    if(counter != null)
                    {
                        if(Integer.valueOf(counter) > ach.getAchTarget())
                            counter = String.valueOf(ach.getAchTarget());
                        ach.setAchValue(Integer.valueOf(counter));
                        counter = null;
                    }
                    break;
                }
                case "B":
                {
                    functionB();
                    if(counter != null)
                    {
                        if(Integer.valueOf(counter) > ach.getAchTarget())
                            counter = String.valueOf(ach.getAchTarget());
                        ach.setAchValue(Integer.valueOf(counter));
                        counter = null;
                    }
                    break;
                }
                case "C":
                {
                    functionC();
                    if(counter != null)
                    {
                        if(Integer.valueOf(counter) > ach.getAchTarget())
                            counter = String.valueOf(ach.getAchTarget());
                        ach.setAchValue(Integer.valueOf(counter));
                        counter = null;
                    }
                    break;
                }
                case "D":
                {
                    functionD();
                    if(counter != null)
                    {
                        if(Integer.valueOf(counter) > ach.getAchTarget())
                            counter = String.valueOf(ach.getAchTarget());
                        ach.setAchValue(Integer.valueOf(counter));
                        counter = null;
                    }
                    break;
                }
                case "E":
                {
                    functionE();
                    if(counter != null)
                    {
                        if(Integer.valueOf(counter) > ach.getAchTarget())
                            counter = String.valueOf(ach.getAchTarget());
                        ach.setAchValue(Integer.valueOf(counter));
                        counter = null;
                    }
                    break;
                }
                case "F":
                {
                    functionAB();
                    if(counter != null)
                    {
                        if(Integer.valueOf(counter) > ach.getAchTarget())
                            counter = String.valueOf(ach.getAchTarget());
                        ach.setAchValue(Integer.valueOf(counter));
                        counter = null;
                    }
                    break;
                }


        }
    }


}
