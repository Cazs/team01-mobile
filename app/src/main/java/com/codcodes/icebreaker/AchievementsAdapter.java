package com.codcodes.icebreaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Casper on 2016/04/09.
 */
public class AchievementsAdapter extends BaseAdapter
{
    private Context context;
    private Achievement[] data;
    private static LayoutInflater inflater = null;

    public AchievementsAdapter(Achievement[] data, Context context)
    {
        this.data = data;
        this.context = context;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return data.length;
    }

    @Override
    public Achievement getItem(int position)
    {
        return data[position];
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

        ach.setText(data[position].getAchName());

        ImageView imgAch = (ImageView)convertView.findViewById(R.id.imgAch);

        if(position==2||position==1||position==5||position==7)
            imgAch.setImageResource(R.drawable.trophy_grey);
        else
            imgAch.setImageResource(R.drawable.trophy);
        return convertView;
    }
}
