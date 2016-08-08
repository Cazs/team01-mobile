package com.codcodes.icebreaker.auxilary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.model.Message;

import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/04.
 */
public class MessageListAdapter extends ArrayAdapter<String>
{
    private Activity context;
    private  String[] messages;

    public MessageListAdapter(Activity context, String[] messages)
    {
        super(context, R.layout.customlist, messages);
        this.context = context;
        this.messages = messages;
    }

    public View getView(int position, View v, ViewGroup parent)
    {
        LayoutInflater inflater = context.getLayoutInflater();
        View chatBubbleList = inflater.inflate(R.layout.customlist,null,true);
        return  chatBubbleList;
    }
}
