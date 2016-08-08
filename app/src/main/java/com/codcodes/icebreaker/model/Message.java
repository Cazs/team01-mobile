package com.codcodes.icebreaker.model;

/**
 * Created by Casper on 2016/08/04.
 */
public class Message
{
    private  String message;

    public Message(String msg)
    {
        this.message = msg;
    }

    public String getMessage()
    {
        return  message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
