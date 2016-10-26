package com.codcodes.icebreaker.auxilary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;

import java.util.List;

/**
 * Created by Casper on 2016/10/24.
 */
public class AchievementsRecyclerViewAdapter extends RecyclerView.Adapter<AchievementsRecyclerViewAdapter.AchievementViewHolder>
{
    /**
     * {@link RecyclerView.Adapter} that can display a {@link User} and makes a call to the
     * specified {@link IOnListFragmentInteractionListener}.
     */
    private final List<Achievement> mValues;
    private final List<Bitmap> mIconBitmaps;
    private final String TAG = "IB/RewRcyclrViewAdapter";

    private final IOnListFragmentInteractionListener mListener;

    public AchievementsRecyclerViewAdapter(List<Achievement> items, List<Bitmap> mIconBitmaps, IOnListFragmentInteractionListener listener)
    {
        mValues = items;
        mListener = listener;
        this.mIconBitmaps = mIconBitmaps;
    }

    @Override
    public AchievementViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ach_list_row_item, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AchievementViewHolder holder, int position)
    {
        holder.setAchievement(mValues.get(position));
        //holder.mContactName.setText(mValues.get(position).getFirstname() + " " + mValues.get(position).getLastname());
        //holder.mContactBio.setText(mValues.get(position).getCatchphrase());
        if(mIconBitmaps!=null)
        {
            if (position < mIconBitmaps.size())
                holder.getAchievementIcon().setImageBitmap(mIconBitmaps.get(position));
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
                    mListener.onListFragmentInteraction(holder.getAchievement());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class AchievementViewHolder extends RecyclerView.ViewHolder
    {
        private final View view;
        private final TextView mAchievementName;
        private final TextView mAchievementDescription;
        private final ImageView mAchievementIcon;
        private final TextView mPoints;
        private final TextView mScore;
        private final TextView mTarget;
        private Achievement achievement;

        public AchievementViewHolder(View view)
        {
            super(view);
            this.view = view;
            this.mAchievementName = (TextView) view.findViewById(R.id.achName);
            this.mAchievementDescription = (TextView) view.findViewById(R.id.achDescription);
            this.mAchievementIcon = (ImageView) view.findViewById(R.id.imgAch);
            this.mPoints = (TextView) view.findViewById(R.id.achPts);
            this.mScore = (TextView) view.findViewById(R.id.score);
            this.mTarget = (TextView) view.findViewById(R.id.target);
        }

        public View getView()
        {
            return this.view;
        }

        public TextView getAchievementName()
        {
            return this.mAchievementName;
        }

        public TextView getAchievementDescription()
        {
            return this.mAchievementDescription;
        }

        public ImageView getAchievementIcon()
        {
            return this.mAchievementIcon;
        }

        public TextView getPoints()
        {
            return this.mPoints;
        }

        public TextView getScore()
        {
            return this.mScore;
        }

        public TextView getTarget()
        {
            return this.mTarget;
        }

        public Achievement getAchievement()
        {
            return this.achievement;
        }

        public void setAchievement(Achievement achievement)
        {
            if(achievement!=null)
            {
                this.achievement = achievement;
                setAchievementName(achievement.getAchName());
                setAchievementDescription(achievement.getAchDescription());
                setScore(String.valueOf(achievement.getUserPoints()));
                setTarget(String.valueOf(achievement.getAchTarget()));
                setPoints(String.valueOf(achievement.getAchValue()));
            }else Log.wtf(TAG, "Achievement is null.");
        }

        public void setAchievementName(String rwName) {this.mAchievementName.setText(rwName);}

        public void setAchievementDescription(String description) {this.mAchievementDescription.setText(description);}

        public void setAchievementIcon(Bitmap bmp) {this.mAchievementIcon.setImageBitmap(bmp);}

        public void setScore(String score) {this.mScore.setText(score);}

        public void setTarget(String target) {this.mTarget.setText(target);}

        public void setPoints(String pts) {this.mPoints.setText(pts);}

        @Override
        public String toString()
        {
            return super.toString() + " '" + mAchievementName.getText() + "'";
        }
    }
}
