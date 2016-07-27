package com.codcodes.icebreaker;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Other_Profile extends AppCompatActivity {

    private TextView profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other__profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profile = (TextView)findViewById(R.id.Profile);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        profile.setTypeface(heading);

        Button icebreak = (Button) findViewById(R.id.icebreak);
        Typeface ib = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        icebreak.setTypeface(heading);

        Typeface h = Typeface.createFromAsset(getAssets(),"Infinity.ttf");
        TextView name = (TextView) findViewById(R.id.other_profile_name);
        name.setTypeface(h);
        name.setText("Selena Gomez"); // TODO: get name from database


        TextView age = (TextView) findViewById(R.id.other_profile_age);
        age.setTypeface(h);
        age.setText("Age: 21");

        TextView occupation = (TextView) findViewById(R.id.other_profile_occupation);
        occupation.setTypeface(h);
        occupation.setText("Singer/Songwriter/Actress");
    }



}
