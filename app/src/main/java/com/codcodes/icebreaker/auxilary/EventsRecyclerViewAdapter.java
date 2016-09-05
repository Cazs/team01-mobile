package com.codcodes.icebreaker.auxilary;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;

import java.util.List;

/**
 * Created by Casper on 2016/09/05.
 */
public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.EventsViewHolder>
{
    private final List<Event> mValues;
    private List<Bitmap> mEventIcons = null;
    private final String TAG = "IB/EventRecyclerAdapter";

    private final IOnListFragmentInteractionListener mListener;

    public EventsRecyclerViewAdapter(List<Event> items, List<Bitmap> mEventBitmaps, IOnListFragmentInteractionListener listener)
    {
        mValues = items;
        mListener = listener;
        this.mEventIcons = mEventBitmaps;
    }

    @Override
    public EventsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ib_list_item_1, parent, false);
        return new EventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final EventsViewHolder holder, int position)
    {
        holder.setEvent(mValues.get(position));
        holder.mEventName.setText(mValues.get(position).getTitle());
        holder.mEventDescription.setText(mValues.get(position).getDescription());
        if(mEventIcons!=null)
        {
            if (position < mEventIcons.size())
                holder.getEventIcon().setImageBitmap(mEventIcons.get(position));
            else
                Log.d(TAG, "Bitmap ArrayList is empty.");
        }

        holder.getView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.getEvent());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class EventsViewHolder extends RecyclerView.ViewHolder
    {
        private View view = null;
        private TextView mEventName = null;
        private TextView mEventDescription = null;
        private ImageView mEventIcon = null;
        private Event event;

        public EventsViewHolder(View view)
        {
            super(view);
            this.view = view;
            this.mEventName = (TextView) view.findViewById(R.id.contactName);
            this.mEventDescription = (TextView) view.findViewById(R.id.contactBio);
            this.mEventIcon = (ImageView) view.findViewById(R.id.contactProfileImage);
        }

        public TextView getEventName()
        {
            return this.mEventName;
        }

        public ImageView getEventIcon()
        {
            return this.mEventIcon;
        }

        public Event getEvent()
        {
            return this.event;
        }

        public View getView()
        {
            return this.view;
        }

        public void setEvent(Event event)
        {
            this.event = event;
        }

        public TextView getEventDescription()
        {
            return this.mEventDescription;
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mEventName.getText() + "'";
        }
    }
}
