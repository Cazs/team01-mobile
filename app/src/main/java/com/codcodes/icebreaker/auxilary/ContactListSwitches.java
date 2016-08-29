package com.codcodes.icebreaker.auxilary;

/**
 * Created by Casper on 2016/08/09.
 */
public enum ContactListSwitches
{
    SHOW_USERS_AT_EVENT(0),
    SHOW_USER_CONTACTS(1);

    private int swtch;

    private ContactListSwitches(int swtch)
    {this.swtch = swtch;}

    public int getSwitch(){return  this.swtch;}
}
