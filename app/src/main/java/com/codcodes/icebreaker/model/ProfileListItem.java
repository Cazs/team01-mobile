package com.codcodes.icebreaker.model;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by ghost on 2016/04/11.
 */
public class ProfileListItem
{
    private String label;
    private String value;

    public ProfileListItem(String label, String value)
    {
        this.label=label;
        this.value=value;
    }

    public String getLabel()
    {
        return label;
    }

    public String getValue()
    {
        return  value;
    }

    public  void setLabel(String label)
    {
        this.label=label;
    }
    public void setValue(String value)
    {
        this.value=value;
    }
}
