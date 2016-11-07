package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RewardsRecyclerViewAdapter;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.Reward;
import com.codcodes.icebreaker.screens.RewardsAchievementsActivity;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by tevin on 2016/07/13.
 */
public class RewardFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private RecyclerView recyclerView;
    private IOnListFragmentInteractionListener mListener;
    private ArrayList<Bitmap> bitmaps;
    private int mColumnCount = 1;
    private static final String TAG = "IB/RewardFragment";
    private static RewardFragment reward;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rview=null;
        View v = inflater.inflate(R.layout.fragment_reward,container,false);

        if(v != null)
            rview = v.findViewById(R.id.rew_list);

        if (rview instanceof RecyclerView)
        {
            Context context = v.getContext();
            recyclerView = (RecyclerView) rview;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
        }

        renderRewards();
        return v;
    }

    private String randromCodeGenerator()
    {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(4);
        for( int i = 0; i < 4; i++)
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public static RewardFragment newInstance(Context context)
    {
        reward = new RewardFragment();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        reward.setArguments(b);
        return reward;
    }

    public void renderRewards() throws ConcurrentModificationException
    {
        /**Prepare to set adapter**/
        //Load users at Event
        //Attempt to load images into memory and set the list adapter
        bitmaps = new ArrayList<Bitmap>();
        Bitmap circularbitmap = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

        if(RewardsAchievementsActivity.rewards!=null)
        {
            for (Reward reward : RewardsAchievementsActivity.rewards)
            {
                try
                {
                    //Look for icon
                    bitmap = LocalComms.getImage(getContext(), reward.getRwId(), ".png", "/rewards", options);
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }

                if (bitmap != null)
                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_200);

                if (bitmap == null || circularbitmap == null)
                {
                    Log.wtf(TAG, "Bitmap '" + reward.getRwId() + ".png' is null");
                    bitmaps.add(null);
                } else
                {
                    //Log.d(TAG, "Loaded bitmap to memory.");
                    bitmaps.add(circularbitmap);
                    bitmap.recycle();
                }
            }
        }
        else
        {
            RewardsAchievementsActivity.rewards = new ArrayList<>();
        }

        if(getActivity()!=null)
        {
            final ProgressBar pb = (ProgressBar) getActivity().findViewById(R.id.pb_rew_load);

            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    if(pb!=null)
                        pb.setVisibility(View.GONE);

                    if (RewardsAchievementsActivity.rewards.isEmpty())
                    {
                        /*Reward temp = new Reward();
                        temp.setRwName(getString(R.string.msg_no_rewards));
                        RewardsAchievementsActivity.rewards.add(temp);*/
                        Log.d(TAG, "Rewards list is empty.");

                        //LinearLayout contactsContainer = (LinearLayout)getActivity().findViewById(R.id.contcts_anim_container);
                        //if(contactsContainer!=null)
                        //    contactsContainer.setVisibility(View.VISIBLE);
                    } else
                    {
                        //LinearLayout contactsContainer = (LinearLayout)getActivity().findViewById(R.id.contcts_anim_container);
                        //if(contactsContainer!=null)
                        //    contactsContainer.setVisibility(View.GONE);
                    }
                    if (recyclerView != null)
                    {
                        recyclerView.setAdapter(new RewardsRecyclerViewAdapter(RewardsAchievementsActivity.rewards, bitmaps, mListener, getActivity()));
                        Log.d(TAG, "Set Rewards list.");
                    }
                    //if(swipeRefreshLayout!=null)
                    //    swipeRefreshLayout.setRefreshing(false);
                }
            };
            runOnUI(runnable);
        }else Log.wtf(TAG,"Activity is null");
    }

    public void runOnUI(Runnable r)
    {
        if(getActivity()!=null)
            getActivity().runOnUiThread(r);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof IOnListFragmentInteractionListener)
        {
            mListener = (IOnListFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString()
                    + " must implement IOnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
}
