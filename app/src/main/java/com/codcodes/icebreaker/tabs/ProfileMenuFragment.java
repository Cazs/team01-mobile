package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IOnListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileMenuFragment extends Fragment
{
    private IOnListFragmentInteractionListener mListener;
    private static Context context=null;

    public ProfileMenuFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static EventMenuFragment newInstance(Context context)
    {
        ProfileMenuFragment.context=context;
        EventMenuFragment fragment = new EventMenuFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_menu, container, false);
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof IOnListFragmentInteractionListener)
        {
            mListener = (IOnListFragmentInteractionListener) context;
        } else
        {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
}
