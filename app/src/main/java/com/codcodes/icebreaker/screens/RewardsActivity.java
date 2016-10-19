package com.codcodes.icebreaker.screens;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.Reward;

import java.io.IOException;
import java.util.ArrayList;

public class RewardsActivity extends AppCompatActivity
{
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int[] imageResId =
            {
                R.drawable.ic_grade_white_24dp,
                R.drawable.ic_school_white_24dp
            };
    private Bitmap circularbitmap,bitmap;
    private String Name,profilepic;
    public static ArrayList<Achievement> achievements=null;
    public static ArrayList<Reward> rewards=null;
    private AchievementFragment achFrag=null;
    private RewardFragment rwFrag = null;
    private static final String TAG = "RewardsActivity";
    private String counter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        // Set up the ViewPager with the sections adapter.
        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
           Name = extras.getString("Name");
            profilepic =extras.getString("Picture");
        }
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(),RewardsActivity.this));

        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) findViewById(R.id.reward_title);
        headingTextView.setTypeface(heading);

        TextView name = (TextView) findViewById(R.id.RewardName);
        name.setText(Name);
        TabLayout tablayout = (TabLayout) findViewById(R.id.tabs);
        tablayout.setupWithViewPager(mViewPager);
        tablayout.getTabAt(0).setIcon(imageResId[0]);
        tablayout.getTabAt(1).setIcon(imageResId[1]);
        tablayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        ImageView circularImageView = (ImageView) findViewById(R.id.circleviewrewards);


        bitmap = ImageUtils.getInstance().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                + profilepic, getApplicationContext());
        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
        circularImageView.setImageBitmap(circularbitmap);

       readRewards();
       readAchievements();

    }

    private void readRewards()
    {
        Thread tLoadAllAchs = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                try
                {
                    rewards = new ArrayList<>();
                    String response = RemoteComms.sendGetRequest("/getAllRewards");
                    JSON.getJsonableObjectsFromJson(response, rewards, Reward.class);

                    if(rewards!=null)
                    {
                        Log.d(TAG,rewards.size() + " Reward in remote DB.");
                        if(rwFrag!=null)
                            rwFrag.setAdapter();
                    }
                    else
                        Log.d(TAG,"Reward from remote DB are null.");

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
    private void readAchievements()
    {
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
                        setAchScore();
                        Log.d(TAG,achievements.size() + " Achievements in remote DB.");
                        if(achFrag!=null)
                            achFrag.setAdapter();

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
    private void functionA()
    {
        final String username = Name;

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
        final String username = Name;

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
        final String username = Name;

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
        final String username = Name;

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
        final String username = Name;

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
        final String username = Name;

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
        final String username = Name;
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
    private void setAchScore()
    {
        for(Achievement ach : achievements)
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                case 1: return RewardFragment.newInstance(context);
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

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        /*Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
        this.finish();
    }
}
