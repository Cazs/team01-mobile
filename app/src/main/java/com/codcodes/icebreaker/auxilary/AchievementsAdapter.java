package com.codcodes.icebreaker.auxilary;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Achievement;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/04/09.
 */
public class AchievementsAdapter extends ArrayAdapter
{
    private Activity context;
    private ArrayList<Achievement> data;
    private static LayoutInflater inflater = null;
    private int counter = 0;
    private TextView score=null;

    public AchievementsAdapter(Activity context, ArrayList<Achievement> data, int resource) {
        super(context, resource);
        this.data = data;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public Achievement getItem(int position)
    {
        return data.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.ach_list_row_item, parent, false);//inflater.inflate(R.layout.ach_list_row_item,null);

        TextView name = (TextView) convertView.findViewById(R.id.achName);
        TextView description = (TextView) convertView.findViewById(R.id.achDescription);
        score = (TextView) convertView.findViewById(R.id.score);
        TextView target = (TextView) convertView.findViewById(R.id.target);
        TextView reward = (TextView) convertView.findViewById(R.id.achPts);
        //TextView achieved = (TextView) convertView.findViewById(R.id.achived);
        TextView spliter = (TextView) convertView.findViewById(R.id.spliter);
        TextView rwLable = (TextView) convertView.findViewById(R.id.achRewardlbl);

        ImageView coins = (ImageView) convertView.findViewById(R.id.imgValue);
        ProgressBar pb = (ProgressBar) convertView.findViewById(R.id.scoreBar);


        name.setText("\n" + data.get(position).getAchName());
        name.setTypeface(null, Typeface.BOLD);
        description.setText(data.get(position).getAchDescription());

        if (data.get(position).getUserPoints()>= data.get(position).getAchTarget()) {
            //Typeface heading = Typeface.createFromAsset(null,"Ailerons-Typeface.otf");
            //achieved.setTypeface(null, Typeface.BOLD_ITALIC);
            //achieved.setVisibility(View.VISIBLE);
            pb.setVisibility(View.INVISIBLE);
            score.setVisibility(View.INVISIBLE);
            target.setVisibility(View.INVISIBLE);
            reward.setVisibility(View.INVISIBLE);
            spliter.setVisibility(View.INVISIBLE);
            rwLable.setVisibility(View.INVISIBLE);
            coins.setVisibility(View.INVISIBLE);
        } else {
            pb.setMax(data.get(position).getAchTarget());

            pb.setProgress(data.get(position).getUserPoints());
            score.setText(String.valueOf(data.get(position).getUserPoints()));
            target.setText(String.valueOf(data.get(position).getAchTarget()));
            reward.setText(String.valueOf(data.get(position).getAchValue()));

            score.setTypeface(null, Typeface.BOLD);
            target.setTypeface(null, Typeface.BOLD);
            reward.setTypeface(null, Typeface.BOLD);
        }

        ImageView imgAch = (ImageView) convertView.findViewById(R.id.imgAch);

        if (position == 0)
            imgAch.setImageResource(R.drawable.test2);
        else if (position == 3)
            imgAch.setImageResource(R.drawable.test);
        else if (position == 1)
            imgAch.setImageResource(R.drawable.test4);
        else if (position == 2)
            imgAch.setImageResource(R.drawable.test5);
        else
            imgAch.setImageResource(R.drawable.test3);
        return convertView;
    }
}