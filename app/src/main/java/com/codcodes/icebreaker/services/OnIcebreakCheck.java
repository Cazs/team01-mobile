package com.codcodes.icebreaker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

/**
 * Created by Casper on 2016/08/16.
 */
public class OnIcebreakCheck extends BroadcastReceiver
{
    private final String TAG = "IB/MessageReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //System.err.println("Received OnIcebreakCheck broadcast");
        Intent icebreakChecker = new Intent(context,IcebreakListenerService.class);
        //context.stopService(icebreakChecker);
        context.startService(icebreakChecker);
    }
}
