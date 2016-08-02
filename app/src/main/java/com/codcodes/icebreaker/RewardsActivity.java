package com.codcodes.icebreaker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.tabs.reward;
import com.codcodes.icebreaker.tabs.ImageConverter;
import com.codcodes.icebreaker.tabs.Profile_Page;

public class RewardsActivity extends AppCompatActivity {

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

    private int[] imageResId = {
            R.drawable.ic_grade_white_24dp,
            R.drawable.ic_school_white_24dp
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(),RewardsActivity.this));


        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        TextView headingTextView = (TextView) findViewById(R.id.reward_title);
        headingTextView.setTypeface(heading);



        TabLayout tablayout = (TabLayout) findViewById(R.id.tabs);
        tablayout.setupWithViewPager(mViewPager);
        tablayout.getTabAt(0).setIcon(imageResId[0]);
        tablayout.getTabAt(1).setIcon(imageResId[1]);


        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.seleena);
        Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap,100);

        ImageView circularImageView = (ImageView) findViewById(R.id.circleviewrewards);
        circularImageView.setImageBitmap(circularbitmap);






    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class FragmentAdapter extends FragmentPagerAdapter
    {

        final int PAGE_COUNT = 2;
        private Context context;

        public FragmentAdapter(FragmentManager fm,Context context) {
            super(fm);
            this.context=context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 0: return reward.newInstance(context);
                case 1: return reward.newInstance(context);
                default:return reward.newInstance(context);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        //private Context context;

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {

        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_page, container, false);
            int v= getArguments().getInt(ARG_SECTION_NUMBER);
            if (v==1)
            {
                rootView = inflater.inflate(R.layout.event_page, container, false);

            }



            // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;

        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

}