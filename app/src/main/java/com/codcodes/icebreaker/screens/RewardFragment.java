package com.codcodes.icebreaker.screens;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codcodes.icebreaker.R;
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
    private ListView rewardList;
    private RewardsAdapter adapter;
    private ArrayList<Reward> rewads;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_reward,container,false);
        rewardList = (ListView) v.findViewById(R.id.RewarList);
        rewads = new ArrayList<Reward>();
        /*rewads.add(new Reward("Free Icebreak Event Ticket",false,"Party Animal Achievement reward",randromCodeGenerator()));
        rewads.add(new Reward("Free Shot",false,"Most Rejected",randromCodeGenerator()));
        rewads.add(new Reward("Free VIP pass",false,"IceBreak Queen/King",randromCodeGenerator()));
        rewads.add(new Reward("Free drink ",false,"Populer Kids",randromCodeGenerator()));
        rewads.add(new Reward("Free Hamper",false,"Star Of The Night",randromCodeGenerator()));*/
        adapter = new RewardsAdapter(getContext(),rewads,0);
        rewardList.setAdapter(adapter);
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

}
