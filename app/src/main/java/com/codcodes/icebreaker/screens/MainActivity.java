package com.codcodes.icebreaker.screens;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IJsonable;
import com.codcodes.icebreaker.services.IbTokenRegistrationService;
import com.codcodes.icebreaker.services.IcebreakService;
import com.codcodes.icebreaker.services.MessageFcmService;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.tabs.EventsFragment;
import com.codcodes.icebreaker.tabs.ProfileFragment;
import com.codcodes.icebreaker.tabs.UserContactsFragment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
    public static ContactListSwitches val_switch = ContactListSwitches.SHOW_USERS_AT_EVENT;


    private static final String TAG = "IB/MainActivity";
    public static String uhandle = "";
    private static boolean cview_set = false;
    private static boolean dlg_visible = false;

    //Since this class is called before any other class, we can do this
    private long event_id = 0;
    private Event event = null;
    //public static ArrayList<User> users_at_event = new ArrayList<>();

    private User lcl = null;

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
        //validateLocationPermissions();
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
            //TODO: Better logging
            toastHandler("PackageManager name not found.").obtainMessage().sendToTarget();
            Log.wtf(TAG,e.getMessage(),e);
        } catch (NoSuchAlgorithmException e)
        {
            //TODO: Better logging
            toastHandler("No such algorithm.").obtainMessage().sendToTarget();
            Log.wtf(TAG,e.getMessage(),e);
        }

        //Init Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        setContentView(R.layout.activity_main);

        //Load User data.
        try
        {
            loadUserData();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        //Start Icebreak checker service that checks the local DB for Icebreaks
        Intent icebreakChecker = new Intent(this,IcebreakService.class);
        startService(icebreakChecker);
        Log.d(TAG,"Started IcebreakService");

        //Start Message listener service
        Intent intMsgService = new Intent(this, MessageFcmService.class);
        startService(intMsgService);
        Log.d(TAG,"Started MessageFcmService");

        //Start token registration service
        Intent intTokenService = new Intent(this, IbTokenRegistrationService.class);
        startService(intTokenService);
        Log.d(TAG,"Started IbTokenRegistrationService");

        //Load UI components
        actionBar = (LinearLayout)findViewById(R.id.actionBar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tablayout = (TabLayout) findViewById(R.id.tab_layout);
        TextView headingTextView = (TextView) findViewById(R.id.main_heading);
        ttfInfinity = Typeface.createFromAsset(this.getAssets(),"Ailerons-Typeface.otf");
        //final FloatingActionButton fabSwitch = (FloatingActionButton)findViewById(R.id.fabSwitch);
        //fabSwitch.hide();

        //Setup UI components
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(),MainActivity.this));
        tablayout.setupWithViewPager(mViewPager);// Set up the ViewPager with the sections adapter.
        tablayout.getTabAt(0).setIcon(viewPagerIcons[0]);
        tablayout.getTabAt(1).setIcon(viewPagerIcons[1]);
        tablayout.getTabAt(2).setIcon(viewPagerIcons[2]);
        headingTextView.setTypeface(ttfInfinity);
        headingTextView.setTextSize(30);

        Intent i = getIntent();
        String frag=i.getStringExtra("Fragment");
        if(frag!=null)
        {
            if(frag.equals(UserContactsFragment.class.getName()))
                mViewPager.setCurrentItem(1, true);
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                TextView title = (TextView)MainActivity.this.findViewById(R.id.main_heading);

                if(position == 2)
                {
                    title.setText("Your Profile");
                    title.setTextSize(30);
                }
                else
                {
                    title.setTextSize(35);
                    title.setText("IceBreak");
                }
            }

            @Override
            public void onPageSelected(int position)
            {
                TextView title = (TextView)MainActivity.this.findViewById(R.id.main_heading);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }

    private void loadUserData() throws IOException
    {
        //Load last Event User was at if it exists.
        String tmp = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
        if(tmp!=null)
            if(!tmp.isEmpty() && !tmp.equals("null"))
                event_id = Long.valueOf(tmp);
        if(event_id>0)
        {
            Thread tEventLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        event = RemoteComms.getEvent(event_id);
                    }
                    catch (IOException e)
                    {
                        Log.wtf(TAG,"IOE (on event init): " + e.getMessage());
                        toastHandler("Could not load your current Event.").obtainMessage().sendToTarget();
                    }
                }
            });
            tEventLoader.start();
        }

        //Try to get local user from DB
        lcl = LocalComms.getContact(this,SharedPreference.getUsername(this).toString());
        uhandle = SharedPreference.getUsername(this).toString();
        if(lcl==null)//not in local DB, get from remote DB
        {
            Thread tLocalUserLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        lcl = RemoteComms.getUser(getApplicationContext(), SharedPreference.getUsername(getBaseContext()).toString());
                        if(lcl==null)
                        {
                            Log.d(TAG, "Could not get user from remote DB.");
                            toastHandler("Could not get user from remote DB. Check your Internet connection.").obtainMessage().sendToTarget();
                        }
                        else
                            Log.d(TAG,"Added a user to local DB.");
                    } catch (IOException e)
                    {
                        Log.d(TAG,"Couldn't add local user to local DB: " + e.getMessage());
                        toastHandler("Couldn't add local user to local DB: " + e.getMessage()).obtainMessage().sendToTarget();
                    }
                }
            });
            tLocalUserLoader.start();
        }else Log.d(TAG,"Local user already in local DB.");
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
        if(item!=null)
        {
            if(item instanceof User)
            {
                if (!((User) item).getFirstname().equals(getString(R.string.msg_not_in_event)))
                {
                    Intent intent = new Intent(this, OtherUserProfileActivity.class);
                    intent.putExtra("User", ((User) item));
                    startActivity(intent);

                    Log.d(TAG, "Loaded other User.");
                } else
                {
                    Log.d(TAG, "Either there are no IceBreak users at this event or you don't have an internet connection or you don't have any contacts.");
                    Toast.makeText(this, "Either there are no IceBreak users at this event or you don't have an internet connection or you don't have any contacts.", Toast.LENGTH_LONG).show();
                }
            }
            if(item instanceof Event)
            {
                if (!((Event) item).getTitle().equals(getString(R.string.msg_no_events)))
                {
                    Intent intent = new Intent(this,EventDetailActivity.class);
                    intent.putExtra("Event",((Event) item));

                    startActivity(intent);
                    Log.d(TAG, "Loaded other Event.");
                } else
                {
                    Log.d(TAG, "Either there are no IceBreak events or you don't have an internet connection or you don't have any contacts.");
                    Toast.makeText(this, "Either there are no IceBreak events or you don't have an internet connection or you don't have any contacts.", Toast.LENGTH_LONG).show();
                }
            }
        }
        else
        {
            Log.d(TAG,"User object is null.");
            Toast.makeText(this, "User object is null.", Toast.LENGTH_LONG).show();
        }
    }

    /*private void validateLocationPermissions()
    {
        LocationManager locationMgr;
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this);
    }*/

    /*@Override
    public void onLocationChanged(Location location)
    {
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(),String.valueOf(location.getLatitude()));
            WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(),String.valueOf(location.getLongitude()));
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
        Log.d(TAG,"["+location.getLatitude()+","+location.getLongitude()+"]");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onProviderDisabled(String s)
    {

    }*/

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
                    return EventsFragment.newInstance(context,getIntent().getExtras());
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
        super.onStop();
        //setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
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
        //setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        try
        {
            loadUserData();
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
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