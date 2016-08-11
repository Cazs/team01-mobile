package com.codcodes.icebreaker.auxilary;

/**
 * Created by Casper on 2016/08/10.
 */
public enum MESSAGE_STATUSES
{
    SENT(0),
    SERV_RECEIVED(1),
    DELIVERED(2),
    READ(3),
    ICEBREAK(100),
    ICEBREAK_DELIVERED(101),
    ICEBREAK_RESPONDED(102);

    private int value;

    private MESSAGE_STATUSES(int status)
    {
        this.value = status;
    }

    public int getStatus(){return this.value;}
}
