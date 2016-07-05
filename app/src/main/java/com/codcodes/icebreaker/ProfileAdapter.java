package com.codcodes.icebreaker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ghost on 2016/04/11.
 */
public class ProfileAdapter extends BaseAdapter
{
    String label;
    ProfileListItem[] data;
    Context context;
    private static LayoutInflater inflater = null;

    public ProfileAdapter(Context context,ProfileListItem[] data)
    {
        this.context = context;
        this.label = label;
        this.data = data;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return data.length;
    }

    @Override
    public Object getItem(int position)
    {
        if(data.length>0 && position < data.length)
            return data[position];
        else
            return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView==null)
            convertView = inflater.inflate(R.layout.prof_list_row_item, parent, false);//inflater.inflate(R.layout.ach_list_row_item,null);

        TextView label = (TextView) convertView.findViewById(R.id.label);
        EditText value = (EditText)convertView.findViewById(R.id.value);

        label.setText(data[position].getLabel());
        value.setText(data[position].getValue());

        return convertView;
    }
}
