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
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.AchievementsAdapter;
import com.codcodes.icebreaker.auxilary.AchievementsRecyclerViewAdapter;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.UserListRecyclerViewAdapter;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.Reward;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.RewardsAchievementsActivity;
import com.codcodes.icebreaker.screens.RewardsActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 * Created by MrSekati on 9/12/2016.
 */
public class AchievementFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private RecyclerView recyclerView;
    private int mColumnCount = 1;
    private IOnListFragmentInteractionListener mListener;
    private ArrayList<Bitmap> bitmaps;
    private String counter = null;
    private static final String TAG = "IB/AchievementFragment";
    private static AchievementFragment achievement;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rview=null;
        View v = inflater.inflate(R.layout.fragment_achievement,container,false);

        if(v != null)
            rview = v.findViewById(R.id.ach_list);

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

        renderAchievements();
        return v;
    }

    /*public void setAdapter()
    {
        if(RewardsAchievementsActivity.achievements!=null)
        {
            if(!RewardsAchievementsActivity.achievements.isEmpty())
            {
                /*for(Achievement ach : RewardsActivity.achievements)
                {
                    setAchScore(ach);
                }*
                adapter = new AchievementsAdapter(getActivity(),RewardsAchievementsActivity.achievements,0);
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
    }*/

    public void renderAchievements() throws ConcurrentModificationException
    {
        /**Prepare to set adapter**/
        //Load users at Event
        //Attempt to load images into memory and set the list adapter
        bitmaps = new ArrayList<Bitmap>();
        Bitmap circularbitmap = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

        if(RewardsAchievementsActivity.achievements!=null)
        {
            for (Achievement achievement : RewardsAchievementsActivity.achievements)
            {
                try
                {
                    //Look for icon
                    if(achievement.isAchieved())
                        bitmap = LocalComms.getImage(getActivity(),achievement.getAchId(),".png","/achievements",options);
                    else bitmap = LocalComms.getImage(getActivity(),achievement.getAchId(),"_not.png","/achievements",options);
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }

                if (bitmap != null)
                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_200);

                if (bitmap == null || circularbitmap == null)
                {
                    Log.wtf(TAG, "Bitmap '" + achievement.getAchId() + ".png' is null");
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
            RewardsAchievementsActivity.achievements = new ArrayList<>();
        }

        if(getActivity()!=null)
        {
            final ProgressBar pb = (ProgressBar) getActivity().findViewById(R.id.pb_ach_load);

            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    if(pb!=null)
                        pb.setVisibility(View.GONE);

                    if (RewardsAchievementsActivity.achievements.isEmpty())
                    {
                        /*Achievement temp = new Achievement();
                        temp.setAchName(getString(R.string.msg_no_achievements));
                        RewardsAchievementsActivity.achievements.add(temp);*/
                        Log.d(TAG, "Achievement list is empty.");

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
                        AchievementsRecyclerViewAdapter adapter = new AchievementsRecyclerViewAdapter(RewardsAchievementsActivity.achievements, bitmaps, mListener);
                        recyclerView.setAdapter(adapter);
                        recyclerView.invalidate();
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Set Achievements list.");
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

    public static AchievementFragment newInstance(Context context)
    {
        achievement = new AchievementFragment();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        achievement.setArguments(b);
        return achievement;
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

    /*private void setAchScore(Achievement ach)
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
    }*/


}
