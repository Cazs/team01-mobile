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
    private int counter = -1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_achievement,container,false);

        //achList = new ArrayList<Achievement>();
      /*  Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    String achievementsJson = RemoteComms.sendGetRequest("getAllAchievements");
                    JSON.<Achievement>getJsonableObjectsFromJson(achievementsJson, achList, Achievement.class);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });*/


        /*achList.add(new Achievement("Ice Breaker",false,"first time Successful icebreak",0,15,15));
        achList.add(new Achievement("Heart breaken",false,"get Rejected 4 times",0,15,25));
        achList.add(new Achievement("Popular",false,"get 10 Successful Icebreaks at an event",0,10,50));
        achList.add(new Achievement("Friendly Person",false,"Get 25 successful icebreaks",0,10,3));
        achList.add(new Achievement("Quick Icebreak",false,"get 10 icebreakes in less than 30 Minutes ",0,4,10));

        for(int i = 0 ; i < achList.size();i++)
        {
            switch(i)
            {
                case 0:
                {
                    icebreakAch(i);
                    //achList.get(i).setScore(counter);
                    break;
                }
                case 1:
                {
                    heartbreakAch(i);
                    //achList.get(i).setScore(counter);
                    break;
                }
                case 2:
                {
                    popularAch(i);
                   // achList.get(i).setScore(counter);
                    break;
                }
                case 3:
                {
                    counter = 4;
                    achList.get(i).setScore(counter);
                    break;
                }
                case 4:
                {
                    counter = 0;
                    achList.get(i).setScore(counter);
                    break;
                }
            }
        }
        list = (ListView) v.findViewById(R.id.achievementList);
        adapter = new AchievementsAdapter(getActivity(),achList,0);
        list.setAdapter(adapter);*/
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

    private void icebreakAch(final int index)
    {
        final String username = SharedPreference.getUsername(this.getContext());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    counter = Integer.valueOf((RemoteComms.sendGetRequest("getUserSuccessfulIcebreakCount/"+ username)));
                   // achList.get(index).setScore(counter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void heartbreakAch(final int index)
    {
        final String username = SharedPreference.getUsername(this.getContext()).toLowerCase();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int total = Integer.valueOf(RemoteComms.sendGetRequest("getUserIcebreakCount/"+ username));
                    int success = Integer.valueOf(RemoteComms.sendGetRequest("getUserSuccessfulIcebreakCount/"+ username));
                    total -= success;
                    counter = total;
                    //achList.get(index).setScore(counter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    /*private void popularAch(final int index)
    {
        final String username = SharedPreference.getUsername(this.getContext()).toLowerCase();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String event_ID = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                    if(event_ID != null)
                    {
                        counter = Integer.valueOf((RemoteComms.sendGetRequest("getUserSuccessfulIcebreakCountAtEvent/"+ username + "/" + event_ID)));
                        achList.get(index).setScore(counter);
                    }
                    else
                    {
                        counter = 0;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }*/

}
