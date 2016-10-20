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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
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

    public static String rootDir = Environment.getExternalStorageDirectory().getPath();
    public static double range = 40.0;
    public static boolean fromBackPress = false;
    public static Location mLastKnownLoc;
    public static boolean is_reloading_events = false;
    public static final String mocLocationProvider = "Icebreak_Mock_Loc";
    private static final String TAG = "IB/MainActivity";
    public static ArrayList<Event> events;
    public static ArrayList<Bitmap> bitmaps;
    private EventsFragment eventsFragment;

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

        //Ping server - server will then update last seen and check for Achievements, Icebreaks and Reward
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
                        Thread.sleep(30000);
                    } catch (InterruptedException e)
                    {
                        LocalComms.logException(e);
                    }
                }
            }
        });
        tAchChecker.start();

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
    }

    public void checkAchievements()
    {
        try
        {
            ArrayList<Achievement> tmp_unnotifd = null;
            try
            {
                tmp_unnotifd = LocalComms.getUnnotifiedAchievementsFromDB(this);
            }catch (SQLiteException e)
            {
                Log.d(TAG,e.getMessage());
            }

            final ArrayList<Achievement> unnotifd = tmp_unnotifd;

            if (unnotifd != null)
            {
                if(!unnotifd.isEmpty())
                {
                    final LinearLayout popup_notif = (LinearLayout)findViewById(R.id.popup_notif);

                    if (this != null)
                    {
                        this.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(popup_notif!=null)
                                    popup_notif.setVisibility(View.VISIBLE);
                                else return;
                            }
                        });
                    }

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
                                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                                if(vibrator.hasVibrator())
                                    vibrator.vibrate(1000);

                                msg.setText(unnotifd.get(0).getAchName());
                                title.setText("Achievement Unlocked");
                                //TODO: set icon
                                //TODO: Change ach stat to notified
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

    public void reloadEvents()
    {
        is_reloading_events=true;
        if(!fromBackPress)
        {
            if (eventsFragment != null)
                eventsFragment.startPulsator();
            else Log.d(TAG, "EventsFragment object is null.");
        }

        final Thread eventsThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                //progress = LocalComms.showProgressDialog(getActivity(),"Loading Events...");
                while (mLastKnownLoc==null){}//wait for location

                //Attempt to load Events
                events = new ArrayList<>();

                try
                {
                    String eventIds = RemoteComms.sendGetRequest("getNearbyEventIds/" + mLastKnownLoc.getLatitude()
                            + '/' + mLastKnownLoc.getLongitude() + '/' + MainActivity.range);
                    eventIds=eventIds.replaceAll("\\[","");
                    eventIds=eventIds.replaceAll("\\]","");
                    eventIds=eventIds.replaceAll("\"","");
                    eventIds=eventIds.replaceAll("\\{","");
                    eventIds=eventIds.replaceAll("\\}","");

                    String[] ids_arr = eventIds.split(",");
                    for(String id:ids_arr)
                    {
                        try
                        {
                            long ev_id = Long.parseLong(id);
                            Event event = LocalComms.getEvent(MainActivity.this, ev_id);
                            events.add(event);
                        }
                        catch (NumberFormatException e)
                        {
                            LocalComms.logException(e);
                            return;
                        }
                    }
                }catch (SocketTimeoutException e)
                {
                    Message message = toastHandler("We are having trouble connecting to the server, please check your internet connection.").obtainMessage();
                    message.sendToTarget();
                    Log.d(TAG, e.getMessage(), e);
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }

                //Attempt to load bitmaps and set adapter
                if(events==null)
                {
                    Toast.makeText(MainActivity.this,"Something went wrong while we were trying to read the events. Please reload.",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Something went wrong while we were trying to read the events.");
                    events = new ArrayList<>();
                }
                if(events.isEmpty())
                {
                    Toast.makeText(MainActivity.this,"No events were found.",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"No events were found.");
                }

                bitmaps = new ArrayList<>();

                try
                {
                    String iconName = "";
                    BitmapFactory.Options options = null;
                    for (Event e : events)
                    {
                        iconName = "event_icons-" + e.getId();
                        options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                        bitmaps.add(LocalComms.getImage(getApplicationContext(), iconName, ".png", "/events", options));
                    }

                    if(eventsFragment!=null)
                        eventsFragment.setAdapter();

                        //check bitmaps only if the number of Events has changed
                        //TODO: Do more in-depth check in case number didn't change but Events changed
                        /*if (bitmaps.size() == events.size() && !bitmaps.isEmpty() && !events.isEmpty())
                        {
                            Log.d(TAG,"Bitmap list is populated.");
                        }
                        else*
                        {
                            try
                            {
                                //Download the file only if it has not been cached
                                Bitmap bitmap = LocalComms.getImage(MainActivity.this, iconName, ".png", "/events", options);
                                bitmaps.add(bitmap);
                            } catch (IOException ex)
                            {
                                LocalComms.logException(ex);
                            }
                        }
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(eventsFragment!=null)
                            {
                                eventsFragment.setEvents(events);
                                eventsFragment.setBitmaps(bitmaps);
                                eventsFragment.setLat(location==null?0.0:location.getLatitude());
                                eventsFragment.setLng(location==null?0.0:location.getLongitude());
                                eventsFragment.setAdapter();
                            }else
                            {
                                eventsFragment = EventsFragment.newInstance(MainActivity.this,
                                        getIntent().getExtras(),events,bitmaps,
                                        location==null?0.0:location.getLatitude(),
                                        location==null?0.0:location.getLongitude()
                                        );
                                eventsFragment.setAdapter();
                            }
                        }
                    });*/
                }catch (SocketTimeoutException e)
                {
                    Message message = toastHandler("We are having trouble connecting to the server, please check your internet connection.").obtainMessage();
                    message.sendToTarget();
                    Log.d(TAG, e.getMessage(), e);
                }
                catch(ConcurrentModificationException e)
                {
                    LocalComms.logException(e);
                }
                catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        eventsThread.start();
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
        Toast.makeText(this, "Connected to GPS provider.", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
        }else Log.wtf(TAG,"Last known location is null.");
        reloadEvents();
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
            e.printStackTrace();
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
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(),"0");
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(),"0");
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
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
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(),"0");
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(),"0");
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(),"0");
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(),"0");
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
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
        Toast.makeText(this,"Clicked back from MainActivity, will close app when clicked twice in the future.",Toast.LENGTH_SHORT).show();
    }
}