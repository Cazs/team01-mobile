package com.codcodes.icebreaker.screens;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import com.codcodes.icebreaker.R;

public class Other_Profile extends AppCompatActivity
{

    private TextView profile;
    private String fName;
    private String LName;
    private String Age;
    private String Occupation;
    private String Bio;
    private String Gender;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = this.getIntent().getExtras();
        if(extras!=null)
        {
            fName = extras.getString("First Name");
        }

        profile = (TextView)findViewById(R.id.Profile);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        profile.setTypeface(heading);

        Button icebreak = (Button) findViewById(R.id.icebreak);
        Typeface ib = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        icebreak.setTypeface(heading);

        Typeface h = Typeface.createFromAsset(getAssets(),"Infinity.ttf");
        TextView name = (TextView) findViewById(R.id.other_profile_name);
        name.setTypeface(h);

        name.setText(fName+" "+LName); // TODO: get name from database


        TextView age = (TextView) findViewById(R.id.other_profile_age);
        age.setTypeface(h);

        age.setText("Age:"+ Age);

        TextView occupation = (TextView) findViewById(R.id.other_profile_occupation);
        occupation.setTypeface(h);
        occupation.setText(Occupation);

        TextView bio_title = (TextView) findViewById(R.id.other_profile_bio_title);
        bio_title.setTypeface(h);
        bio_title.setText("Bio:");

        TextView bio = (TextView) findViewById(R.id.other_profile_bio);
        bio.setTypeface(h);
        bio.setText(Bio);
    }



}
