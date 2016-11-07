package com.codcodes.icebreaker.screens;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.IJsonable;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.Reward;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.tabs.AchievementFragment;
import com.codcodes.icebreaker.tabs.RewardFragment;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class RewardsAchievementsActivity extends AppCompatActivity implements IOnListFragmentInteractionListener
{
    private final int[] imageResId =
            {
                    R.drawable.ic_grade_white_24dp,
                    R.drawable.ic_school_white_24dp
            };
    private Bitmap circularbitmap,bitmap;
    private String Name,profilepic,coins;
    public static ArrayList<Achievement> achievements=null;
    public static ArrayList<Reward> rewards=null;
    private AchievementFragment achFrag=null;
    private RewardFragment rwFrag = null;
    private static final String TAG = "RewardsActivity";
    private ViewPager mViewPager;
    private Typeface ttfInfinity, ttfAilerons;
    private TextView headingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards_achievements);

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        TabLayout tablayout = (TabLayout) findViewById(R.id.rew_ach_tabs);
        mViewPager = (ViewPager) findViewById(R.id.rew_ach_container);

        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), RewardsAchievementsActivity.this));
        tablayout.setupWithViewPager(mViewPager);// Set up the ViewPager with the sections adapter.

        headingTextView = (TextView) findViewById(R.id.main_heading);
        TextView txtName = (TextView) findViewById(R.id.txtName);
        TextView txtPoints = (TextView) findViewById(R.id.txtPoints);
        final ImageView img = (ImageView) findViewById(R.id.imgProfilePic);

        headingTextView.setTypeface(ttfAilerons);
        headingTextView.setTextSize(40);
        txtName.setTypeface(ttfInfinity);
        txtPoints.setTypeface(ttfInfinity);

        tablayout.getTabAt(0).setIcon(imageResId[0]);
        tablayout.getTabAt(1).setIcon(imageResId[1]);

        readAchievements();
        readRewards();

        final User u = getIntent().getParcelableExtra("User");
        txtName.setText(LocalComms.getValidatedName(u));

        txtPoints.setText("Points: " + String.valueOf(u.getPoints()));

        Thread tImgLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                try
                {
                    final ProgressBar pb = (ProgressBar) findViewById(R.id.pb_img_load);
                    final Bitmap bmp = LocalComms.getImage(RewardsAchievementsActivity.this,u.getUsername(),".png","/profile",options);
                    RewardsAchievementsActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            img.setImageBitmap(bmp);
                            if(pb!=null)
                                pb.setVisibility(View.GONE);
                        }
                    });
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        tImgLoader.start();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if(position==0)
                {
                    headingTextView.setText("Achievements");
                    headingTextView.setTextSize(28);
                }
                if(position==1)
                    headingTextView.setText("Rewards");
            }

            @Override
            public void onPageSelected(int position)
            {

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }

    private void readRewards()
    {
        final Context context = this;

        Thread tLoadAllRews = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    String eventID = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                    if (eventID != null)
                    {
                        rewards = new ArrayList<>();
                        String response = RemoteComms.sendGetRequest("/getRewardsForEvent/" + eventID);
                        //String response = RemoteComms.sendGetRequest("/getAllRewards");
                        JSON.getJsonableObjectsFromJson(response, rewards, Reward.class);

                        if (rewards != null)
                        {
                            /*for(Reward rw: rewards)
                            {
                                if(!rw.getRwEventID().equals(eventID))
                                    rewards.remove(rw);
                            }*/

                            if (rwFrag != null)
                            {
                                rwFrag.renderRewards();
                                //rwFrag.newInstance(context);
                            }
                            else Log.wtf(TAG, "RewardFragment is still null.");

                        } else Log.d(TAG, "Rewards from remote DB are null.");
                    }
                }
                catch (SocketTimeoutException e)
                {
                    LocalComms.logException(e);
                }catch (InstantiationException e)
                {
                    LocalComms.logException(e);
                } catch (IllegalAccessException e)
                {
                    LocalComms.logException(e);
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        tLoadAllRews.start();
    }

    private void readAchievements()
    {
        final Context context = this;
        Thread tLoadAllAchs = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    achievements = new ArrayList<>();
                    String response = RemoteComms.sendGetRequest("/getAllAchievements");
                    JSON.getJsonableObjectsFromJson(response, achievements, Achievement.class);

                    if(achievements!=null)
                    {
                        //Get User Achievements and Points
                        ArrayList<Achievement> usr_achievements = new ArrayList<>();
                        response = RemoteComms.sendGetRequest("/getUserAchievements/" + SharedPreference.getUsername(RewardsAchievementsActivity.this));
                        JSON.getJsonableObjectsFromJson(response, usr_achievements, Achievement.class);
                        if(!usr_achievements.isEmpty())
                        {
                            if (!achievements.isEmpty())
                            {
                                for (int i = 0; i < achievements.size(); i++)
                                {
                                    for (Achievement a : usr_achievements)
                                    {
                                        if (achievements.get(i).getAchId().equals(a.getAchId()))
                                        {
                                            achievements.set(i, a);
                                        } else
                                        {
                                            //Get User points for Achievement
                                            response = RemoteComms.sendGetRequest("/getUserAchievementPoints/" + SharedPreference.getUsername(RewardsAchievementsActivity.this) + "/" + achievements.get(i).getAchMethod());
                                            try
                                            {
                                                int pts = Integer.parseInt(response);
                                                achievements.get(i).setAchUserPoints(pts);
                                            }catch (IndexOutOfBoundsException e)
                                            {
                                                Log.wtf(TAG,e.getMessage(),e);
                                            } catch (NumberFormatException e)
                                            {
                                                Log.wtf(TAG, "Input points are not an integer.");
                                            }
                                        }
                                    }
                                }
                            } else Log.d(TAG, "User has no Achievements.");
                        }else Log.wtf(TAG,"No Achievements.");

                        Log.d(TAG,achievements.size() + " Achievements in remote DB.");

                        if(achFrag!=null)
                        {
                            achFrag.renderAchievements();
                            //achFrag.newInstance(context);
                        }
                        else Log.wtf(TAG, "AchievementFragment is still null.");
                    }
                    else
                        Log.d(TAG,"Achievements from remote DB are null.");

                }catch (InstantiationException e)
                {
                    LocalComms.logException(e);
                } catch (IllegalAccessException e)
                {
                    LocalComms.logException(e);
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        tLoadAllAchs.start();
    }

    @Override
    public void onListFragmentInteraction(IJsonable item)
    {
        System.out.println("Got object of type " + item.getClass().getName());
    }

    public class FragmentAdapter extends FragmentPagerAdapter
    {
        final int PAGE_COUNT = 2;
        private Context context;

        public FragmentAdapter(FragmentManager fm,Context context)
        {
            super(fm);
            this.context=context;
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    achFrag = AchievementFragment.newInstance(context);
                    return achFrag;
                case 1:
                    rwFrag = RewardFragment.newInstance(context);
                    return rwFrag;
                default:return RewardFragment.newInstance(context);
            }
        }

        @Override
        public int getCount()
        {
            return PAGE_COUNT;
        }

        public CharSequence getPageTitle(int position)
        {
            return null;
        }
    }
}
