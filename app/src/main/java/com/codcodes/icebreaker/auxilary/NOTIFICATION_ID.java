package com.codcodes.icebreaker.auxilary;

/**
 * Created by Casper on 2016/08/15.
 */
public enum NOTIFICATION_ID
{
    NOTIF_REQUEST(1),
    NOTIF_ACCEPTED(2),
    NOTIF_REJECTED(3);

    private final int value;

    private NOTIFICATION_ID(int id)
    {
        this.value = id;
    }

    public int getId()
    {
        return this.value;
    }
}
