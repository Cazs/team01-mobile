package com.codcodes.icebreaker.screens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.INTERVALS;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.services.IbTokenRegistrationService;
import com.codcodes.icebreaker.services.IcebreakCheckerService;
import com.codcodes.icebreaker.services.MessageFcmService;
import com.codcodes.icebreaker.services.OnIcebreakCheck;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.tabs.EventsFragment;
import com.codcodes.icebreaker.tabs.ProfileFragment;
import com.codcodes.icebreaker.tabs.UserContactsFragment;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements IOnListFragmentInteractionListener
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
    private Button accept, reject;
    private TextView txtPopupbio, txtPopupbioTitle, txtPopupgender, txtPopupage, txtPopupname;
    private LinearLayout actionBar;
    private ViewPager mViewPager;
    private Typeface ttfInfinity, ttfAilerons;

    public static String rootDir = Environment.getExternalStorageDirectory().getPath();
    public static boolean appInFG = false;
    public static ContactListSwitches val_switch = ContactListSwitches.SHOW_USERS_AT_EVENT;


    private static final String TAG = "IB/MainActivity";
    public static String uhandle = "";
    private static boolean cview_set = false;
    private static boolean dlg_visible = false;

    private User lcl = null;

    private int[] viewPagerIcons =
            {
                R.drawable.ic_location_on_white_24dp,
                R.drawable.ic_chat_bubble_white_24dp,
                R.drawable.ic_person_white_24dp
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //init Firebase
        //FirebaseApp.initializeApp(getApplicationContext(), FirebaseOptions.fromResource(getApplicationContext()));

        /*try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        /*Add local user to local db if they don't already exist in the db*/
        //Try to get local user from DB
        lcl = LocalComms.getLocalUser(SharedPreference.getUsername(this).toString(),this);
        uhandle = SharedPreference.getUsername(this).toString();
        if(lcl==null)//not in local DB
        {
            Thread tLocalUserLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        lcl = RemoteComms.getUser(SharedPreference.getUsername(getBaseContext()).toString());
                        if(lcl!=null)
                            LocalComms.addContact(getBaseContext(), lcl);
                        else
                            Log.d(TAG,"Couldn't add local user to local DB");
                    } catch (IOException e)
                    {
                        Log.d(TAG,"Couldn't add local user to local DB: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            tLocalUserLoader.start();
        }else//is in DB
        {
            Log.d(TAG,"Local user already in local DB!!");
        }

        appInFG = true;

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        //Start Icebreak searching service
        /*Thread tIBloader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                while (appInFG)
                {
                    try
                    {
                        checkForIcebreaks();
                        Thread.sleep(IB_CHECK_DELAY);
                    } catch (IOException e)//TODO: Fix error handling.
                    {
                        e.printStackTrace();
                    }catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        tIBloader.start();*/

        //Start Icebreak checker service
        Intent icebreakChecker = new Intent(this,IcebreakCheckerService.class);
        //context.stopService(icebreakChecker);
        startService(icebreakChecker);

        //Start message polling service
        Intent intMsgService = new Intent(this, MessageFcmService.class);
        //inMsg.putExtra("Username", SharedPreference.getUsername(this));
        startService(intMsgService);
        Log.d(TAG,"Started MessageFcmService");

        //Start token registration service
        Intent intTokenService = new Intent(this, IbTokenRegistrationService.class);
        startService(intTokenService);
        Log.d(TAG,"Started TokenService");

        //startIcebreakListenerService();

        //Load UI components
        actionBar = (LinearLayout)findViewById(R.id.actionBar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tablayout = (TabLayout) findViewById(R.id.tab_layout);
        TextView headingTextView = (TextView) findViewById(R.id.main_heading);
        ttfInfinity = Typeface.createFromAsset(this.getAssets(),"Ailerons-Typeface.otf");
        final FloatingActionButton fabSwitch = (FloatingActionButton)findViewById(R.id.fabSwitch);

        //Setup UI components
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(),MainActivity.this));
        tablayout.setupWithViewPager(mViewPager);// Set up the ViewPager with the sections adapter.
        tablayout.getTabAt(0).setIcon(viewPagerIcons[0]);
        tablayout.getTabAt(1).setIcon(viewPagerIcons[1]);
        tablayout.getTabAt(2).setIcon(viewPagerIcons[2]);
        headingTextView.setTypeface(ttfInfinity);
        fabSwitch.hide();

        fabSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(val_switch==ContactListSwitches.SHOW_USER_CONTACTS)
                    val_switch=ContactListSwitches.SHOW_USERS_AT_EVENT;
                else
                    val_switch=ContactListSwitches.SHOW_USER_CONTACTS;
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if(position == 1)
                {
                    fabSwitch.show();
                }else
                {
                    fabSwitch.hide();
                }
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

    private void accept()
    {

    }

    private void reject()
    {

    }

    private void startIcebreakListenerService()
    {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent uiUpdateIntent = new Intent(this,OnIcebreakCheck.class);
        //uiUpdateIntent.putExtra("Local Username",receiving_user.ge);
        PendingIntent uiPendingIntent = PendingIntent.getBroadcast(this,0,uiUpdateIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(TAG, "Set up IcebreakCheckerService alarm");

        //Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.SECOND, INTERVAL);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), INTERVALS.UI_UPDATE_DELAY.getValue(), uiPendingIntent);
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

    @Override
    public void onListFragmentInteraction(User item)
    {

        /*Dialog userProfileScreen = new Dialog(this);
        userProfileScreen.setContentView(R.layout.content_other_user_profile);

        TextView username = (TextView)userProfileScreen.findViewById(R.id.other_user_name);
        ImageView profile_image = (ImageView)userProfileScreen.findViewById(R.id.other_user_profile_image);
        TextView level = (TextView)userProfileScreen.findViewById(R.id.other_user_level);
        TextView age = (TextView)userProfileScreen.findViewById(R.id.other_user_age);
        TextView gender = (TextView)userProfileScreen.findViewById(R.id.other_user_gender);
        TextView occupation = (TextView)userProfileScreen.findViewById(R.id.other_user_occupation);
        TextView phrase = (TextView)userProfileScreen.findViewById(R.id.other_user_phrase);
        TextView bio = (TextView)userProfileScreen.findViewById(R.id.other_user_bio);

        username.setText(item.getUsername());
        /*bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/profile_default.png",getActivity());*
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        if(new File(Environment.getExternalStorageDirectory().getPath().toString()
                + "/Icebreak/profile/"+item.getUsername()+".png").exists())
        {
            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                    + "/Icebreak/profile/"+item.getUsername()+".png", options);
        }
        else
        {
            if(new File(Environment.getExternalStorageDirectory().getPath().toString()
                    + "/Icebreak/profile/profile_default.png").exists())
            {
                bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                        + "/Icebreak/profile/profile_default.png", options);
            }
        }
        if(bitmap!=null)
        {
            Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
            profile_image.setImageBitmap(circularbitmap);
        }
        else
        {
            Toast.makeText(this,"Could not get profile for selected user, nor could we find the default image.",Toast.LENGTH_LONG).show();
        }

        //level.setText(item.getLevel());
        //age.setText(item.getAge());
        gender.setText(item.getGender());
        occupation.setText(item.getOccupation());
        phrase.setText(item.getCatchphrase());
        bio.setText(item.getBio());

        userProfileScreen.show();
        */
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra("Username",item.getUsername());
        startActivity(intent);
    }

    public class FragmentAdapter extends FragmentPagerAdapter
    {
        final int PAGE_COUNT = 3;
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
                    //actionBar.setVisibility(View.VISIBLE);
                    return EventsFragment.newInstance(context,getIntent().getExtras());
                case 1:
                    //actionBar.setVisibility(View.INVISIBLE);
                    return UserContactsFragment.newInstance(context, getIntent().getExtras());
                case 2:
                    //actionBar.setVisibility(View.INVISIBLE);
                    return ProfileFragment.newInstance(context);
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        public CharSequence getPageTitle(int position)
        {
            return null;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //hideDialog();
        this.appInFG = false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.appInFG = true;
    }

    public boolean isInForeground()
    {
        return this.appInFG;
    }

    @Override
    public void onBackPressed()
    {
        Toast.makeText(this,"Clicked back from MainActivity",Toast.LENGTH_SHORT).show();
        /*Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
    }
}