package com.codcodes.icebreaker.tabs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.INTERVALS;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.UserListRecyclerViewAdapter;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

//import static com.google.android.gms.internal.zzir.runOnUiThread;
/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IOnListFragmentInteractionListener}
 * interface.
 */
public class UserContactsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private ArrayList<User> contacts = null;
    private ArrayList<Bitmap> bitmaps;
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "IB/UserContactsFragment";
    private static final boolean DEBUG = true;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private Timer tContactsRefresh = null;
    private static boolean CHUNKED = false;
    private IOnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    //private SwipeListAdapter swipeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static UserContactsFragment newInstance(int columnCount)
    {
        UserContactsFragment fragment = new UserContactsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
        {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        //draw UI
        refresh();
    }

    public static UserContactsFragment newInstance(Context context, Bundle b)
    {
        UserContactsFragment instance = new UserContactsFragment();
        instance.setArguments(b);
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_usercontacts_list, container, false);
        View rview = null;
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable()
        {
                @Override
                public void run()
                {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        );

        if(view != null)
            rview = view.findViewById(R.id.userContactList);

        if (rview instanceof RecyclerView)
        {
            Context context = view.getContext();
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

        /*
        *The reason I did the following is so that the only entry in the contacts is the
        * R.string.msg_not_in_event message
        */
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    refreshUsersAtEvent();
                }
                catch (ConcurrentModificationException e)
                {
                    //TODO: Better logging.
                    Log.wtf(TAG,e.getMessage(),e);
                }
            }
        });
        t.start();

        return view;
    }

    public void refresh()
    {
        tContactsRefresh = new Timer();
        tContactsRefresh.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    if (MainActivity.event_id > 0)
                    {
                        ArrayList<User> users = new ArrayList<User>();
                        MainActivity.event = RemoteComms.getEvent(MainActivity.event_id);
                        String contactsJson = RemoteComms.sendGetRequest("getUsersAtEvent/" + MainActivity.event_id);
                        JSON.<User>getJsonableObjectsFromJson(contactsJson, users, User.class);
                        int i=0;
                        if(MainActivity.users_at_event==null)
                            MainActivity.users_at_event = new ArrayList<User>();
                        if(users==null)
                            return;
                        if(users.size()!=MainActivity.users_at_event.size())//then number of users has changed
                        {
                            MainActivity.users_at_event = new ArrayList<User>();
                            for(User u:users)
                                MainActivity.users_at_event.add(u);
                            refreshUsersAtEvent();
                        }
                        /* Commented out due to performance considerations
                        else //Still the same number of users, compare each user
                        {
                            boolean has_changes = false;
                            for(User u:users)
                            {
                                if(!u.getUsername().equals(MainActivity.users_at_event.get(i)))
                                    has_changes = true;
                                ++i;
                            }
                            if(has_changes)
                            {
                                MainActivity.users_at_event = new ArrayList<User>();
                                for(User u:users)
                                    MainActivity.users_at_event.add(u);
                                refreshUsersAtEvent();
                            }
                        }*/
                    } else Log.d(TAG,"User not at an event.");
                }
                catch (ConcurrentModificationException e)
                {
                    //TODO: Better logging.
                    Log.wtf(TAG,e.getMessage(),e);
                }
                catch (java.lang.InstantiationException e)
                {
                    //TODO: Better logging.
                    Log.wtf(TAG,e.getMessage(),e);
                } catch (IllegalAccessException e)
                {
                    //TODO: Better logging.
                    Log.wtf(TAG,e.getMessage(),e);
                } catch (IOException e)
                {
                    //TODO: Better logging.
                    Log.wtf(TAG,e.getMessage(),e);
                }
            }
        }, 0, INTERVALS.USERS_AT_EVENT_REFRESH_DELAY.getValue());
    }

    private void refreshUsersAtEvent() throws ConcurrentModificationException
    {
        contacts = new ArrayList<>();

        /**Prepare to set adapter**/
        //Load users at Event
        if(MainActivity.val_switch.getSwitch() == ContactListSwitches.SHOW_USERS_AT_EVENT.getSwitch())
            contacts = MainActivity.users_at_event;
        else//Load local contacts
            contacts = LocalComms.getContacts(UserContactsFragment.this.getActivity());

        if(contacts==null)
            return;
        //Attempt to load images into memory and set the list adapter
        bitmaps = new ArrayList<Bitmap>();
        Bitmap circularbitmap = null;
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

        for (User u : contacts)
        {
            //Look for user profile image
            bitmap = LocalComms.getImage(getContext(), u.getUsername(), ".png", "/profile", options);
            if (bitmap == null)
                bitmap = RemoteComms.getImage(getActivity(), u.getUsername(), ".png", "/profile", options);

            if(bitmap!=null)
                circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_200);

            if (bitmap == null || circularbitmap == null)
            {
                Log.wtf(TAG, "Bitmap "+u.getUsername()+".png is null");
                bitmaps.add(null);
            }
            else
            {
                //Log.d(TAG, "Loaded bitmap to memory.");
                bitmaps.add(circularbitmap);
                bitmap.recycle();
            }
        }

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (recyclerView != null)
                {
                    if(contacts==null)
                        contacts = new ArrayList<>();

                    if(contacts.isEmpty())
                    {
                        User temp = new User();
                        temp.setFirstname(getString(R.string.msg_not_in_event));
                        temp.setLastname("");
                        ArrayList<User> temp_lst = new ArrayList<User>();
                        temp_lst.add(temp);
                        recyclerView.setAdapter(new UserListRecyclerViewAdapter(temp_lst, bitmaps, mListener));
                        Log.d(TAG, "Contact list is empty.");
                    }
                    else
                    {
                        recyclerView.setAdapter(new UserListRecyclerViewAdapter(contacts, bitmaps, mListener));
                        Log.d(TAG, "Set contact list.");
                    }
                }
                if(swipeRefreshLayout!=null)
                    swipeRefreshLayout.setRefreshing(false);
            }
        };
        runOnUI(runnable);
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

    @Override
    public void onRefresh()
    {
        swipeRefreshLayout.setRefreshing(true);
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    refreshUsersAtEvent();
                }
                catch (ConcurrentModificationException e)
                {
                    //TODO: Better logging.
                    Log.wtf(TAG,e.getMessage(),e);
                }
            }
        });
        t.start();
    }
}
