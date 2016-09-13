package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Achievement;

import java.util.ArrayList;

/**
 * Created by Casper on 2016/04/09.
 */
public class AchievementsAdapter extends ArrayAdapter
{
    private Context context;
    private ArrayList<Achievement> data;
    private static LayoutInflater inflater = null;

    public AchievementsAdapter(Context context, ArrayList<Achievement> data,int resource) {
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView==null)
            convertView = inflater.inflate(R.layout.ach_list_row_item, parent, false);//inflater.inflate(R.layout.ach_list_row_item,null);
        TextView ach = (TextView) convertView.findViewById(R.id.achName);

        ach.setText("\n" +data.get(position).getAchName() + "\n" + data.get(position).getAchDescription());

        ImageView imgAch = (ImageView)convertView.findViewById(R.id.imgAch);

        if(position==0)
            imgAch.setImageResource(R.drawable.test2);
        else if(position==3)
            imgAch.setImageResource(R.drawable.test);
        else if(position==1)
            imgAch.setImageResource(R.drawable.test4);
        else if(position==2)
            imgAch.setImageResource(R.drawable.test5);
        else
            imgAch.setImageResource(R.drawable.test3);
        return convertView;
    }
}
