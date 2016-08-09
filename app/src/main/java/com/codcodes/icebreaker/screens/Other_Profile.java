package com.codcodes.icebreaker.screens;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private String ImageID;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_one);

        Bundle extras = this.getIntent().getExtras();
        if(extras!=null)
        {
            //All this information will need to be sent to other party
            fName = extras.getString("First Name");
            LName = extras.getString("Last Name");
            Age = Integer.toString(extras.getInt("Age"));
            Occupation = extras.getString("Occupation");
            Bio = extras.getString("Bio");
            Gender = extras.getString("Gender");
            ImageID = extras.getString("ImageID");
        }

        profile = (TextView)findViewById(R.id.Profile);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        profile.setTypeface(heading);

        ImageView profileImage = (ImageView) findViewById(R.id.other_pic);
        profileImage.setImageBitmap(BitmapFactory.decodeFile(ImageID));
        Button icebreak = (Button) findViewById(R.id.icebreak);
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

        TextView gender = (TextView) findViewById(R.id.other_profile_gender);
        gender.setTypeface(h);
        gender.setText(Gender);

        TextView bio_title = (TextView) findViewById(R.id.other_profile_bio_title);
        bio_title.setTypeface(h);
        bio_title.setText("Bio:");

        TextView bio = (TextView) findViewById(R.id.other_profile_bio);
        bio.setTypeface(h);
        bio.setText(Bio);


        TextView popupname = (TextView) dialog.findViewById(R.id.popup1_profile_name);
        popupname.setText(fName+" "+LName);
        popupname.setTypeface(h);

        TextView popupage = (TextView) dialog.findViewById((R.id.popup1_profile_age)) ;
        popupage.setText("Age:"+ Age);
        popupage.setTypeface(h);

        TextView popupgender = (TextView) dialog.findViewById((R.id.popup1_profile_gender)) ;
        popupgender.setText(Gender);
        popupgender.setTypeface(h);

        TextView popupbiotitle = (TextView) dialog.findViewById((R.id.popup1_profile_bio_title)) ;
        popupbiotitle.setText("Bio:");
        popupbiotitle.setTypeface(h);

        TextView popupbio = (TextView) dialog.findViewById((R.id.popup1_profile_bio)) ;
        popupbio.setText(Bio);
        popupbio.setTypeface(h);

        Button accept = (Button) dialog.findViewById(R.id.popup1_Accept);
        accept.setTypeface(heading);

        Button reject = (Button) dialog.findViewById(R.id.popup1_Reject);
        reject.setTypeface(heading);

       icebreak.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view)
           {
                dialog.show();
           }
       });

    }




}
