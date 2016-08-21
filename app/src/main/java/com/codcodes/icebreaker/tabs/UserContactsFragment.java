package com.codcodes.icebreaker.tabs;

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
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.UserListRecyclerViewAdapter;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//import static com.google.android.gms.internal.zzir.runOnUiThread;
/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link IOnListFragmentInteractionListener}
 * interface.
 */
public class UserContactsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "IB/UserContactsFragment";
    private static final boolean DEBUG = true;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private static boolean CHUNKED = false;
    private IOnListFragmentInteractionListener mListener;
    private int curr_event_id;
    private RecyclerView recyclerView;
    //private SwipeListAdapter swipeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserContactsFragment()
    {
        this.curr_event_id = DEBUG?0:-1;
    }

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
                    //refresh();
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
            // Set the adapter
            refresh();
        }
        return view;
    }

    public void loadEventId()
    {
        //TODO: Do SQLliteDB stuff
        //for now leave at 0 - if DEBUG else -1
    }

    public void refresh()
    {
        Thread tContactsLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                if(curr_event_id >= 0)
                {
                    try
                    {
                        String contactsJson = RemoteComms.sendGetRequest("getUsersAtEvent/" + curr_event_id);
                        final ArrayList<User> contacts = new ArrayList<>();
                        JSON.<User>getJsonableObjectsFromJson(contactsJson, contacts, User.class);
                        System.err.println("Contacts at event: " + curr_event_id+ " " + contacts.size() + " people");
                        //Attempt to load images into memory and set the list adapter
                        final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                        Bitmap circularbitmap = null;
                        Bitmap bitmap = null;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                        for (User u : contacts) {
                            //Look for user profile image
                            if (!new File(Environment.getExternalStorageDirectory().getPath()
                                    + "/Icebreak/profile/" + u.getUsername() + ".png").exists()) {
                                //if (imageDownload(u.getUsername() + ".png", "/profile")) {
                                options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                Bitmap b = RemoteComms.getImage(getActivity(),u.getUsername(), ".png", "/profile", options);
                                if(b!=null)
                                {
                                    bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/" + u.getUsername() + ".png", options);
                                    //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
                                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                } else //user has no profile yet - attempt to load default profile image
                                {
                                    if (!new File(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/profile_default.png").exists()) {
                                        //Attempt to download default profile image
                                        options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                        b = RemoteComms.getImage(getActivity(),"profile_default", ".png", "/profile", options);
                                        if(b!=null)
                                        {
                                            /*bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                                    + "/Icebreak/profile/profile_default.png", getActivity());*/
                                            options = new BitmapFactory.Options();
                                            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                                    + "/Icebreak/profile/profile_default.png", options);
                                            circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                        } else //Couldn't download default profile image
                                        {
                                            Toast.makeText(getActivity(), "Could not download default profile images, please check your internet connection.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else//default profile image exists
                                    {
                                        /*bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/profile_default.png",getActivity());*/
                                        options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                        bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/profile_default.png", options);
                                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                    }
                                }
                            } else//user profile image exists
                            {
                                bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                        + "/Icebreak/profile/" + u.getUsername() + ".png", getActivity());
                                circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                            }
                            if (bitmap == null || circularbitmap == null) {
                                System.err.println("Bitmap is null");
                            } else {
                                bitmaps.add(circularbitmap);
                                bitmap.recycle();
                            }
                        }
                        if(MainActivity.val_switch == ContactListSwitches.SHOW_USERS_AT_EVENT)
                        {
                            Runnable runnable = new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (recyclerView != null)
                                    {
                                        recyclerView.setAdapter(new UserListRecyclerViewAdapter(contacts, bitmaps, mListener));
                                        Log.d(TAG, "Set contact list");
                                    }
                                }
                            };
                            runOnUI(runnable);
                            /*runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (recyclerView != null)
                                    {
                                        recyclerView.setAdapter(new UserListRecyclerViewAdapter(contacts, bitmaps, mListener));
                                        Log.d(TAG, "Set contact list");
                                    }
                                }
                            });*/
                        }
                    } catch (IOException e) {
                        //TODO: Error Logging
                        e.printStackTrace();
                    } catch (java.lang.InstantiationException e) {
                        //TODO: Error Logging
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                }
                //TODO:load contacts from local SQLiteDB and check with remote DB
                if(MainActivity.val_switch == ContactListSwitches.SHOW_USER_CONTACTS)
                {
                    /**Load contacts from local DB (and double check with server) and set adapter
                     *
                     * runOnUiThread(new Runnable() {
                    @Override public void run() {
                    if (recyclerView != null) {
                    recyclerView.setAdapter(new UserListRecyclerViewAdapter(contacts, bitmaps, mListener));
                    Log.d(TAG, "Set contact list");
                    }
                    }
                    });
                     */
                    Runnable runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (recyclerView != null)
                            {
                                swipeRefreshLayout.setRefreshing(false);
                                Log.d(TAG, "Disabled refresh");
                            }
                        }
                    };
                    runOnUI(runnable);
                }
            }
        });
        tContactsLoader.start();
    }

    public void runOnUI(Runnable r)
    {
        UserContactsFragment.this.getActivity().runOnUiThread(r);
    }

    /*public void refreshContacts()
    {
        if(curr_event_id >= 0) {
            //TODO: load contacts at that event
            Thread tContactsLoader = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        String contactsJson = RemoteComms.getJsonFromURL("getUsersAtEvent/" + curr_event_id);
                        final ArrayList<User> contacts = new ArrayList<>();
                        JSON.<User>getJsonableObjectsFromJson(contactsJson, contacts, User.class);
                        //Attempt to load images into memory and set the list adapter

                        for (User u : contacts) {

                        }
                    } catch (java.lang.InstantiationException e) {
                        Log.d(TAG, e.getMessage());
                    } catch (IllegalAccessException e) {
                        Log.d(TAG, e.getMessage());
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
            tContactsLoader.start();
        }
        else
        {
            //TODO:load contacts from local SQLiteDB and check with remote DB
        }
    }*/

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
        //refresh();
        swipeRefreshLayout.setRefreshing(true);
        Toast.makeText(getActivity(),"Refreshing",Toast.LENGTH_SHORT).show();
        //refresh();
    }
}
