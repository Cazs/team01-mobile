package com.codcodes.icebreaker.model;

import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;

/**
 * Created by Casper on 2016/08/04.
 */
public class Message implements IJsonable
{
    private  String id;
    private  String message;
    private String sender;
    private String receiver;
    private String time;
    private int status = 0;

    public Message()
    {

    }

    public String getMessage()
    {
        return  this.message;
    }

    public String getSender()
    {
        return  this.sender;
    }

    public String getReceiver()
    {
        return  this.receiver;
    }

    public int getStatus()
    {
        return  this.status;
    }

    public String getTime()
    {
        return  this.time;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getId()
    {
        return  this.id;
    }

    public void setSender(String sender)
    {
        this.sender = sender;
    }

    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    public void setId(String id)
    {
        this.id=id;
    }

    @Override
    public void setVarValue(String var, String value)
    {
        switch (var)
        {
            case "Message_receiver":
                setReceiver(value);
                break;
            case "Message_sender":
                setSender(value);
                break;
            case "Message_time":
                setTime(value);
                break;
            case "Message_status":
                setStatus(Integer.valueOf(value));
                break;
            case "Message_id":
                setId(value);
                break;
            case "Msg":
                setMessage(value);
                break;
        }
    }
}
