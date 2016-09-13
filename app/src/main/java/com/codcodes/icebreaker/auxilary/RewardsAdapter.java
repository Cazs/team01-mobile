package com.codcodes.icebreaker.auxilary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Achievement;
import com.codcodes.icebreaker.model.Rewards;

import java.util.ArrayList;

public class RewardsAdapter extends ArrayAdapter
{
    private Context context;
    private ArrayList<Rewards> data;
    private static LayoutInflater inflater = null;

    public RewardsAdapter(Context context, ArrayList<Rewards> data,int resource) {
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
    public Rewards getItem(int position)
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

        ach.setText("\n" +data.get(position).getRwName() + "\n" + data.get(position).getRwDescription());

        ImageView imgAch = (ImageView)convertView.findViewById(R.id.imgAch);

        if(position==0)
            imgAch.setImageResource(R.drawable.rw);
        else if(position==3)
            imgAch.setImageResource(R.drawable.drinkic);
        else if(position==4)
            imgAch.setImageResource(R.drawable.hamper);
        else if(position==2)
            imgAch.setImageResource(R.drawable.vip);
        else
            imgAch.setImageResource(R.drawable.trophy);
        return convertView;
    }
}
