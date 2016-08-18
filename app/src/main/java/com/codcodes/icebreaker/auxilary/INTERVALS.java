package com.codcodes.icebreaker.auxilary;

/**
 * Created by Casper on 2016/08/16.
 */
public enum INTERVALS
{
    IB_CHECK_DELAY(1500),
    BG_SERVC_POLL_DELAY(1000),
    UI_UPDATE_DELAY(500);

    private final int value;

    private INTERVALS(int interval)
    {
        this.value = interval;
    }

    public int getValue()
    {
        return this.value;
    }
}
