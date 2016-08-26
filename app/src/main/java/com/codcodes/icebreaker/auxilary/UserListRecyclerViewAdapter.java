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
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link User} and makes a call to the
 * specified {@link IOnListFragmentInteractionListener}.
 */
public class UserListRecyclerViewAdapter extends RecyclerView.Adapter<UserListRecyclerViewAdapter.ContactViewHolder>
{
    private final List<User> mValues;
    private final List<Bitmap> mProfileBitmaps;
    private final String TAG = "IB/UserListRecyclerViewAdapter";

    private final IOnListFragmentInteractionListener mListener;

    public UserListRecyclerViewAdapter(List<User> items, List<Bitmap> mProfileBitmaps, IOnListFragmentInteractionListener listener)
    {
        mValues = items;
        mListener = listener;
        this.mProfileBitmaps = mProfileBitmaps;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_usercontacts, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, int position)
    {
        holder.setUser(mValues.get(position));
        holder.mContactName.setText(mValues.get(position).getFirstname() + " " + mValues.get(position).getLastname());
        holder.mContactBio.setText(mValues.get(position).getCatchphrase());
        if(position<mProfileBitmaps.size())
            holder.getContactProfileImage().setImageBitmap(mProfileBitmaps.get(position));
        else
            Log.d(TAG,"Bitmap ArrayList is empty.");
        holder.getView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.getUser());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder
    {
        private final View view;
        private final TextView mContactName;
        private final TextView mContactBio;
        private final ImageView mContactProfileImage;
        private User user;

        public ContactViewHolder(View view)
        {
            super(view);
            this.view = view;
            this.mContactName = (TextView) view.findViewById(R.id.contactName);
            this.mContactBio = (TextView) view.findViewById(R.id.contactBio);
            this.mContactProfileImage = (ImageView) view.findViewById(R.id.contactProfileImage);
        }

        public TextView getContactName()
        {
            return this.mContactName;
        }

        public ImageView getContactProfileImage()
        {
            return this.mContactProfileImage;
        }

        public User getUser()
        {
            return this.user;
        }

        public View getView()
        {
            return this.view;
        }

        public void setUser(User user)
        {
            this.user = user;
        }

        public TextView getContactBio()
        {
            return this.mContactBio;
        }

        @Override
        public String toString()
        {
            return super.toString() + " '" + mContactName.getText() + "'";
        }
    }
}
