package com.codcodes.icebreaker.screens;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codcodes.icebreaker.R;

/**
 * Created by tevin on 2016/07/13.
 */
public class RewardFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_reward,container,false);

        return v;

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
