package com.codcodes.icebreaker.screens;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.media.Image;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IJsonable;
import com.codcodes.icebreaker.services.IbTokenRegistrationService;
import com.codcodes.icebreaker.services.IcebreakService;
import com.codcodes.icebreaker.services.MessageFcmService;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.tabs.EventsFragment;
import com.codcodes.icebreaker.tabs.ProfileFragment;
import com.codcodes.icebreaker.tabs.UserContactsFragment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class MainActivity extends AppCompatActivity implements IOnListFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.L
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private LinearLayout actionBar;
    private Typeface ttfInfinity, ttfAilerons;
    private FloatingActionButton btnCam;

    public static String rootDir = Environment.getExternalStorageDirectory().getPath();
    public static double range = 40.0;
    public static int  min_age = 0;
    public static int max_age = 0;
    public static int pref_gender = 2;
    public static double loudness = 0.0;
    public static boolean passed_events=false;

    public static boolean fromBackPress = false;
    public static Location mLastKnownLoc;
    public static boolean is_reloading_events = false;
    public static final String mocLocationProvider = "Icebreak_Mock_Loc";
    private static final String TAG = "IB/MainActivity";
    public static ArrayList<Event> events;
    public static ArrayList<Bitmap> bitmaps;
    private EventsFragment eventsFragment;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private GoogleApiClient mGoogleApiClient;

    private int[] viewPagerIcons =
            {
                    R.drawable.ic_location_on_white_24dp,
                    R.drawable.ic_people_white_24dp,
                    R.drawable.ic_person_white_24dp
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Init Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        //Get and set hash
        try
        {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                //Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e)
        {
            toastHandler("PackageManager name not found.").obtainMessage().sendToTarget();
            LocalComms.logException(e);
        } catch (NoSuchAlgorithmException e)
        {
            toastHandler("No such algorithm.").obtainMessage().sendToTarget();
            LocalComms.logException(e);
        }

        setContentView(R.layout.activity_main);
        //
        String permission = "android.permission.RECORD_AUDIO";
        int res = getApplication().checkCallingOrSelfPermission(permission);
        boolean b =  (res == PackageManager.PERMISSION_GRANTED);
        if(!b)
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        //

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        //Load UI components
        actionBar = (LinearLayout) findViewById(R.id.actionBar);
        mViewPager = (ViewPager) findViewById(R.id.container);

        TabLayout tablayout = (TabLayout) findViewById(R.id.tab_layout);
        TextView headingTextView = (TextView) findViewById(R.id.main_heading);

        //Setup UI components
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(), MainActivity.this));

        tablayout.setupWithViewPager(mViewPager);// Set up the ViewPager with the sections adapter.
        tablayout.getTabAt(0).setIcon(viewPagerIcons[0]);
        tablayout.getTabAt(1).setIcon(viewPagerIcons[1]);
        tablayout.getTabAt(2).setIcon(viewPagerIcons[2]);
        headingTextView.setTypeface(ttfAilerons);
        headingTextView.setTextSize(40);

        //Start Icebreak checker service that checks the local DB for Icebreaks
        Intent icebreakChecker = new Intent(this, IcebreakService.class);
        startService(icebreakChecker);
        Log.d(TAG, "Started IcebreakService");

        //Start Message listener service
        Intent intMsgService = new Intent(this, MessageFcmService.class);
        startService(intMsgService);
        Log.d(TAG, "Started MessageFcmService");

        //Start token registration service
        Intent intTokenService = new Intent(this, IbTokenRegistrationService.class);
        startService(intTokenService);
        Log.d(TAG, "Started IbTokenRegistrationService");

        //Ping server - server will then update last seen and check for Achievements, Icebreaks and Rewards
        Thread tPing = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    boolean svr_res = RemoteComms.pingServer(SharedPreference.getUsername(MainActivity.this));
                    Log.d(TAG, "Pinged server, successful? " + svr_res);
                } catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        tPing.start();

        //Start Achievement checker service
        Thread tAchChecker = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    checkAchievements();
                    try
                    {
                        Thread.sleep(10000);
                    } catch (InterruptedException e)
                    {
                        LocalComms.logException(e);
                    }
                }
            }
        });
        tAchChecker.start();

        //Load Settings
        Thread tSettingsLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String dist = WritersAndReaders.readAttributeFromConfig(Config.EVENT_MAX_DIST.getValue());
                    String min_age = WritersAndReaders.readAttributeFromConfig(Config.USR_MIN_AGE.getValue());
                    String max_age = WritersAndReaders.readAttributeFromConfig(Config.USR_MAX_AGE.getValue());
                    String loudness = WritersAndReaders.readAttributeFromConfig(Config.EVENT_LOUDNESS.getValue());
                    String pref_gen = WritersAndReaders.readAttributeFromConfig(Config.USR_GEND.getValue());
                    String passed = WritersAndReaders.readAttributeFromConfig(Config.PASSED_EVENTS.getValue());

                    if (dist != null)
                        MainActivity.range = Double.parseDouble(dist);
                    if (min_age != null)
                        MainActivity.min_age = Integer.parseInt(min_age);
                    if (max_age != null)
                        MainActivity.max_age = Integer.parseInt(max_age);
                    if (loudness != null)
                        MainActivity.loudness = Double.parseDouble(loudness);
                    if (pref_gen != null)
                        MainActivity.pref_gender = Integer.parseInt(pref_gen);
                    if(passed!=null)
                    {
                        if (passed.equals("0"))MainActivity.passed_events=false;
                        if (passed.equals("1"))MainActivity.passed_events=true;
                    }

                }catch (NumberFormatException e)
                {
                  LocalComms.logException(e);
                } catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        tSettingsLoader.start();

        Intent i = getIntent();
        String frag = i.getStringExtra("Fragment");
        fromBackPress = i.getBooleanExtra("com.codcodes.icebreaker.Back",fromBackPress);

        if (frag != null)
        {
            if (frag.equals(EventsFragment.class.getName()))
            {
                mViewPager.setCurrentItem(0);
            }
            if (frag.equals(UserContactsFragment.class.getName()))
            {
                mViewPager.setCurrentItem(1);
            }
            if (frag.equals(ProfileFragment.class.getName()))
            {
                mViewPager.setCurrentItem(2);
            }
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

                TextView title = (TextView) MainActivity.this.findViewById(R.id.main_heading);
                if(position==1)
                    btnCam.setVisibility(View.VISIBLE);
                else btnCam.setVisibility(View.GONE);

                if (position == 2)
                {
                    title.setText("Your Profile");
                    title.setTextSize(25);
                } else
                {
                    title.setTextSize(40);
                    title.setText("IceBreak");
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

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        btnCam = (FloatingActionButton)findViewById(R.id.fabCam);
        btnCam.setVisibility(View.GONE);

        btnCam.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intCam = new Intent(MainActivity.this,CameraActivity.class);
                startActivity(intCam);
            }
        });
    }

    public void checkAchievements()
    {
        try
        {
            ArrayList<Achievement> tmp_unnotifd=null;
            try
            {
                tmp_unnotifd = LocalComms.getUnnotifiedAchievementsFromDB(this);
            }catch (SQLiteException e)
            {
                LocalComms.logException(e);
            }

            final ArrayList<Achievement> unnotifd = tmp_unnotifd;

            if (unnotifd != null)
            {
                if(!unnotifd.isEmpty())
                {
                    final LinearLayout popup_notif = (LinearLayout)findViewById(R.id.popup_notif);
                    final ImageView ach_icon = (ImageView)findViewById(R.id.msg_icon);
                    final TextView title = (TextView) findViewById(R.id.msg_title);
                    final TextView msg = (TextView) findViewById(R.id.msg);
                    final ImageView icon = (ImageView) findViewById(R.id.msg_icon);

                    if(title==null||msg==null||icon==null)
                        return;

                    if (this != null)
                    {
                        this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(popup_notif!=null)
                                {
                                    popup_notif.setVisibility(View.VISIBLE);

                                    Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                                    if(vibrator.hasVibrator())
                                        vibrator.vibrate(500);
                                    Achievement unlocked_ach = unnotifd.get(0);//get first new achievement unlocked
                                    msg.setText(unlocked_ach.getAchName());
                                    title.setText("Achievement Unlocked");

                                    BitmapFactory.Options opts = new BitmapFactory.Options();
                                    opts.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                    //Attempt to set icon
                                    try
                                    {
                                        Bitmap bitmap = LocalComms.getImage(MainActivity.this, unlocked_ach.getAchId(), ".png", "/achievements", opts);
                                        ach_icon.setImageBitmap(bitmap);
                                    } catch (IOException e)
                                    {
                                        LocalComms.logException(e);
                                    }
                                    //Change ach status to notified
                                    unlocked_ach.setNotified(1);
                                    LocalComms.updateAchievementOnDB(getApplicationContext(), unlocked_ach);
                                } else return;
                            }
                        });
                    }

                    if (this != null)
                    {
                        this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                            }
                        });
                    }
                    //Pause bg thread for a bit
                    try
                    {
                        Thread.sleep(7000);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    //Hide popup message box
                    if (this != null)
                    {
                        this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //Hide popup notif
                                if(popup_notif!=null)
                                    popup_notif.setVisibility(View.GONE);
                            }
                        });
                    }
                    //TODO: Change Achievement notification state
                }
                else
                {
                    Log.d(TAG,"All Achievements have been notified.");
                }
            }
        }catch (SQLiteException e)
        {
            LocalComms.logException(e);
        }
    }

    private Handler toastHandler(final String text)
    {
        Handler toastHandler = new Handler(Looper.getMainLooper())
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        };
        return toastHandler;
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
    public void onListFragmentInteraction(IJsonable item)
    {
        if (item != null)
        {
            if (item instanceof User)
            {
                if (!((User) item).getFirstname().equals(getString(R.string.msg_not_in_event)))
                {
                    Intent intent = new Intent(this, OtherUserProfileActivity.class);
                    intent.putExtra("User", ((User) item));
                    startActivity(intent);

                } else
                {
                    Log.d(TAG, "Either there are no IceBreak users at this event or you don't have an internet connection or you don't have any contacts.");
                    toastHandler("Either there are no IceBreak users at this event or you don't have an internet connection or you don't have any contacts.").obtainMessage().sendToTarget();
                }
            }
            if (item instanceof Event)
            {
                if (!((Event) item).getTitle().equals(getString(R.string.msg_no_events)))
                {
                    Intent intent = new Intent(this, EventDetailActivity.class);
                    intent.putExtra("Event", ((Event) item));

                    startActivity(intent);
                } else
                {
                    Log.d(TAG, "Either there are no IceBreak events or you don't have an internet connection or you don't have any contacts.");
                    Toast.makeText(this, "Either there are no IceBreak events matching your criteria or you don't have an internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        } else
        {
            Log.d(TAG, "User object is null.");
            Toast.makeText(this, "User object is null.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        //Toast.makeText(this, "Connected to GPS provider.", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return;
        }
        mLastKnownLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLastKnownLoc!=null)
        {
            try
            {
                WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(), String.valueOf(mLastKnownLoc.getLatitude()));
                WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(), String.valueOf(mLastKnownLoc.getLongitude()));
            } catch (IOException e)
            {
                LocalComms.logException(e);
            }
        }else
        {
            //If location is null use hardcoded GPS coordinates (UJ APK)
            mLastKnownLoc= new Location("Icebreak GPS Provider");
            mLastKnownLoc.setLatitude(-26.183478);
            mLastKnownLoc.setLatitude(27.996496);
            try
            {
                WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(), String.valueOf(mLastKnownLoc.getLatitude()));
                WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(), String.valueOf(mLastKnownLoc.getLongitude()));
            } catch (IOException e)
            {
                LocalComms.logException(e);
            }
            Log.wtf(TAG,"Last known location is null.");
            toastHandler("Couldn't get last known location.").obtainMessage().sendToTarget();
        }
        if(eventsFragment!=null)
            eventsFragment.reloadEvents(EventsFragment.LOAD_LOCAL_EVENTS);
        else Log.d(TAG,"EventsFragment is null");
        //reloadEvents();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(this,"Connection to GPS provider suspended ["+i+"]. Could not get last known location.",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(this,"Connection to GPS provider failed. Could not get last known location.",Toast.LENGTH_LONG).show();
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
                    eventsFragment = EventsFragment.newInstance(
                            context,
                            getIntent().getExtras());
                    return eventsFragment;
                case 1:
                    return UserContactsFragment.newInstance(context, getIntent().getExtras());
                case 2:
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

    private void setDlgStatus(String val)
    {
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.DLG_ACTIVE.getValue(),val);
        } catch (IOException e)
        {
            LocalComms.logException(e);
        }
    }

    @Override
    protected void onStart()
    {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.connect();
        super.onStart();
        if(eventsFragment!=null)
            eventsFragment.setAdapter();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    protected void onStop()
    {
        if(mGoogleApiClient!=null)
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(eventsFragment!=null)
            eventsFragment.setAdapter();
    }

    @Override
    public void onBackPressed()
    {
        /**Incomplete**
         back_click_count++;
         if(back_click_count>=3)
         this.finish();**/
        //Toast.makeText(this,"Clicked back from MainActivity, will close app when clicked twice in the future.",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            this.finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
}