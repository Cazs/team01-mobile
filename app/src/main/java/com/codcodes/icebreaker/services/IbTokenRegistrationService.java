package com.codcodes.icebreaker.services;

import android.content.Intent;
import android.util.Log;

import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.MainActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by Casper on 2016/08/20.
 */
public class IbTokenRegistrationService extends FirebaseInstanceIdService
{
    private final String TAG = "IB/IdTokenService";

    public IbTokenRegistrationService()
    {
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                onTokenRefresh();
            }
        });
        t.start();
    }

    @Override
    public void onTokenRefresh()
    {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: <token_hidden>");

        // Update Instance ID token on server.
        ArrayList<AbstractMap.SimpleEntry<String,String>> params = new ArrayList<>();
        String usr;
        if(SharedPreference.getUsername(getBaseContext()).toString().length()>0)//MainActivity.uhandle.length()
        {
            usr = SharedPreference.getUsername(getBaseContext());
            //User lcl = ;
            if(usr==null)
                return;
            if(usr.isEmpty())
                return;

            params.add(new AbstractMap.SimpleEntry<String, String>("username", usr));
            params.add(new AbstractMap.SimpleEntry<String, String>("token", refreshedToken));
            //System.err.println(">>>>>>>"+usr+":"+refreshedToken);
            try
            {
                int res_code=RemoteComms.postData("setUniqueUserToken", params);
                if(res_code== HttpURLConnection.HTTP_OK)
                {
                    Log.d(TAG, "Updated token on REST server");
                }
                else
                    System.err.println("Could not update token on server: " + res_code);
            } catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
        }
        else
        {
            Log.d(TAG,"Global username currently NULL");
        }
    }
}
