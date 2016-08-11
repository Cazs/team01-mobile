package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.codcodes.icebreaker.R;

/**
 * Created by Casper on 2016/04/09.
 */
public class IceBreakActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icebreak);
        System.err.println("IBA====");
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alert.setTitle("Icebreak");
        alert.setMessage("Someone broke ice with you.");
        alert.show();
    }
}
