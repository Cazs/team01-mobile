package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
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
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.auxilary.UserContactsRecyclerViewAdapter;
import com.codcodes.icebreaker.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.internal.zzir.runOnUiThread;
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
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private static boolean CHUNKED = false;
    private IOnListFragmentInteractionListener mListener;
    //private SwipeListAdapter swipeAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserContactsFragment()
    {
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

                    //TODO: Refresh action
                }
            }
        );
        if(view != null)
            rview = view.findViewById(R.id.userContactList);
        // Set the adapter
        if (rview instanceof RecyclerView)
        {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) rview;
            if (mColumnCount <= 1)
            {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            }
            else
            {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            Thread tContactsLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Looper.prepare();
                    try
                    {
                        String contactsJson = Restful.getJsonFromURL("getUserContacts");
                        final ArrayList<User> contacts = new ArrayList<>();
                        JSON.<User>getJsonableObjectsFromJson(contactsJson, contacts, User.class);
                        //Attempt to load images into memory and set the list adapter
                        final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                        Bitmap circularbitmap = null;
                        Bitmap bitmap = null;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                        for (User u : contacts)
                        {
                            //Look for user profile image
                            if (!new File(Environment.getExternalStorageDirectory().getPath()
                                    + "/Icebreak/profile/" + u.getUsername() + ".png").exists())
                            {
                                //if (imageDownload(u.getUsername() + ".png", "/profile")) {
                                if (Restful.imageDownloader(u.getUsername(), ".png", "/profile", getActivity()))
                                {
                                    bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/" + u.getUsername() + ".png", options);
                                    //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
                                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                } else //user has no profile yet - attempt to load default profile image
                                {
                                    if (!new File(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/profile_default.png").exists())
                                    {
                                        //Attempt to download default profile image
                                        if (Restful.imageDownloader("profile_default",".png", "/profile", getActivity()))
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
                                        + "/Icebreak/profile/" + u.getUsername() + ".png",getActivity());
                                circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                            }
                            if(bitmap == null || circularbitmap == null)
                            {
                                System.err.println("Bitmap is null");
                            }
                            else
                            {
                                bitmaps.add(circularbitmap);
                                bitmap.recycle();
                            }
                        }
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                recyclerView.setAdapter(new UserContactsRecyclerViewAdapter(contacts, bitmaps, mListener));
                                Log.d(TAG,"Set contact list");
                            }
                        });
                    }
                    catch (IOException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                    catch (java.lang.InstantiationException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                }
            });
            tContactsLoader.start();
        }
        return view;
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

    }
}
