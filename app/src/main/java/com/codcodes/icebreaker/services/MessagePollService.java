package com.codcodes.icebreaker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.codcodes.icebreaker.auxilary.INTERVALS;

/**
 * Created by Casper on 2016/08/10.
 */
public class MessagePollService extends IntentService
{
    private final String TAG="IB/MsgPollService";

    public MessagePollService()
    {
        super("Message Poll Service");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle b = intent.getExtras();
        final String username = b.getString("Username");
        /*new CountDownTimer(0, INTERVAL)
        {
            @Override
            public void onTick(long l)
            {
                poll(username);
            }

            @Override
            public void onFinish()
            {
                Log.d(TAG,"Done server poll.");
            }
        }.start();*/

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intnt = new Intent(this,OnMessageReceive.class);
        intnt.putExtra("Username",username);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,intnt,PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d(TAG, "Set up alarm");

        //Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.SECOND, INTERVAL);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVALS.BG_SERVC_POLL_DELAY.getValue(), pi);
    }


}
