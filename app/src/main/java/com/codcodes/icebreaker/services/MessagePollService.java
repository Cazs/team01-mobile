package com.codcodes.icebreaker.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;

import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.OnMessageReceive;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;
import com.codcodes.icebreaker.screens.IceBreakActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Casper on 2016/08/10.
 */
public class MessagePollService extends IntentService
{
    private final String TAG="IB/MsgPollService";
    private final int INTERVAL = 5000;//5 sec

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

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),INTERVAL, pi);
    }


}
