package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.screens.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IOnListFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventMenuFragment extends Fragment
{
   private IOnListFragmentInteractionListener mListener;
    private EditText range=null;
    private Button btnUpdate=null;

    private final String TAG = "IB/EvtMenuFragment";

    public EventMenuFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static EventMenuFragment newInstance(Context context, Bundle b)
    {
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
        View v = inflater.inflate(R.layout.fragment_event_menu, container, false);
        btnUpdate = (Button) v.findViewById(R.id.btnUpdate);
        range = (EditText) v.findViewById(R.id.edtRange);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try
                {
                    MainActivity.range= Double.parseDouble(range.getText().toString());
                }catch (NumberFormatException e)
                {
                    if(e.getMessage()!=null)
                        Log.d(TAG,e.getMessage(),e);
                    else
                        e.printStackTrace();
                }
            }
        });
        return v;
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
