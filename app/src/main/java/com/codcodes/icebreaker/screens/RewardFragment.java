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
import com.codcodes.icebreaker.auxilary.RewardsAdapter;
import com.codcodes.icebreaker.model.Reward;

import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by tevin on 2016/07/13.
 */
public class RewardFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private ListView list;
    private RewardsAdapter adapter;
    private ArrayList<Reward> rewads;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_reward,container,false);
        list = (ListView) v.findViewById(R.id.RewarList);
        setAdapter();
        return v;
    }

    private String randromCodeGenerator()
    {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for( int i = 0; i < 8; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static RewardFragment newInstance(Context context)
    {
        RewardFragment e = new RewardFragment();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }
    public void setAdapter()
    {
        if(RewardsActivity.rewards!=null)
        {
            if(!RewardsActivity.rewards.isEmpty())
            {
                adapter = new RewardsAdapter(getActivity(),RewardsActivity.rewards,0);
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
            else Toast.makeText(getActivity(), "No Rewards found, or they are still loading.",Toast.LENGTH_LONG).show();
        }
    }


}
