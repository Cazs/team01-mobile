package com.codcodes.icebreaker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.codcodes.icebreaker.dummy.DummyContent;
import com.codcodes.icebreaker.tabs.ChatsFragment;
import com.codcodes.icebreaker.tabs.ContactsFragment;
import com.codcodes.icebreaker.tabs.RecentFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity implements RecentFragment.OnListFragmentInteractionListener,
        ContactsFragment.OnListFragmentInteractionListener,
        ChatsFragment.OnListFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener//ContactsFragment.OnListFragmentInteractionListener
{
    private ListView lstAchs;
    //private ArrayList<String> achs = null;
    private String[] arr = new String[20];
    private SlidingTabLayout tabs;
    private CharSequence labels[] = {"Recent", "Chats", "Contacts"};
    private int tabCount = 5;

    //
    private LinearLayout toolbar;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private TextView toolbarText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbarText = (TextView)findViewById(R.id.toolbarTitle);
        toolbarText.setText("Kerry Washington");
        // Creating The Toolbar and setting it as the Toolbar for the activity
        //toolbar = (LinearLayout) findViewById(R.id.toolbar);
        /*toolbar.setTitle("Kerry Washington");
        setSupportActionBar(toolbar);*/
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setIcon(R.drawable.trophy);


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabLevel);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action\n\n\n\n\n\n\n\n\n\n\n\n", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();
            }
        });*/

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Create pager for tabs
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), labels, tabCount);

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setViewPager(pager);

        final SlidingUpPanelLayout supl = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);

        supl.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener()
        {
            @Override
            public void onPanelSlide(View panel, float slideOffset)
            {
                //if(slideOffset==1.0)

                //else

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState)
            {

            }
        });

    }

    @Override
    public void onBackPressed()
    {
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    public void showMenu(View v)
    {
        /*if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }*/
        drawer.openDrawer(navigationView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            startActivity(new Intent(this,IceBreakActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item)
    {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        //Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile)
        {
            startActivity(new Intent(this,ProfileActivity.class));
        }
        else if (id == R.id.nav_gallery)
        {

        }
        else if (id == R.id.nav_attended)
        {

        }
        else if (id == R.id.nav_settings)
        {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (id == R.id.nav_find)
        {
            startActivity(new Intent(this, EventMapActivity.class));
        }
        else if (id == R.id.nav_share)
        {

        }
        else if (id == R.id.nav_send)
        {

        }
        else if (id == R.id.nav_achievements)
        {
            Log.d("IBE","Checkpoint0");
            startActivity(new Intent(MainActivity.this,AchievementsActivity.class));
            //lstAchs = (ListView)findViewById(R.id.lstAchs);
            //Log.d("IBE","Is Null:"+(lstAchs == null));
            //lstAchs.setAdapter(new AchievementsAdapter(arr,this));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
