package com.codcodes.icebreaker;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.codcodes.icebreaker.tabs.ChatsFragment;
import com.codcodes.icebreaker.tabs.ContactsFragment;
import com.codcodes.icebreaker.tabs.RecentFragment;

/**
 * Created by ghost on 2016/04/12.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter
{
    private CharSequence[] titles;
    private int tabCount;

    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int tabCount)
    {
        super(fm);
        this.titles = titles;
        this.tabCount = tabCount;
    }
    /**
     * Return the Fragment associated with a specified position.
     * @param position
     */
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new RecentFragment();
            case 1:
                return new ContactsFragment();
            case 2:
                return new ChatsFragment();
        }
        return null;
    }

    /**
     * @param position - Index of tab
     * @return The titles for the Tabs in the Tab Strip
     */
    @Override
    public CharSequence getPageTitle(int position)
    {
        return titles[position];
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount()
    {
        return tabCount;
    }
}
