package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Restful;

public class OtherUserProfileActivity extends AppCompatActivity
{

    private TextView profile;
    private String fname;
    private String lname;
    private String username;
    private String age;
    private String occupation;
    private String bio;
    private String gender;

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
            fname = extras.getString("Firstname");
            lname = extras.getString("Lastname");
            username = extras.getString("Username");
            age = Integer.toString(extras.getInt("Age"));
            occupation = extras.getString("Occupation");
            bio = extras.getString("Bio");
            gender = extras.getString("Gender");
        }

        profile = (TextView)findViewById(R.id.Profile);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        profile.setTypeface(heading);

        //Load and render selected user's profile
        final Activity ctxt =this;
        final ImageView profileImage = (ImageView) findViewById(R.id.other_pic);
        Thread tUserProfileLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                final Bitmap bitmap = Restful.getImage(ctxt,username,".png","/profile",options);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        profileImage.setImageBitmap(bitmap);
                    }
                });
            }
        });
        tUserProfileLoader.start();

        Button icebreak = (Button) findViewById(R.id.icebreak);
        icebreak.setTypeface(heading);

        Typeface h = Typeface.createFromAsset(getAssets(),"Infinity.ttf");
        TextView txtName = (TextView) findViewById(R.id.other_profile_name);
        txtName.setTypeface(h);
        txtName.setText(fname+" "+lname); // TODO: get name from database

        TextView txtAge = (TextView) findViewById(R.id.other_profile_age);
        txtAge.setTypeface(h);
        txtAge.setText("Age:"+ age);

        TextView txtOccupation = (TextView) findViewById(R.id.other_profile_occupation);
        txtOccupation.setTypeface(h);
        txtOccupation.setText(occupation);

        TextView txtGender = (TextView) findViewById(R.id.other_profile_gender);
        txtGender.setTypeface(h);
        txtGender.setText(gender);

        TextView txtBioTitle = (TextView) findViewById(R.id.other_profile_bio_title);
        txtBioTitle.setTypeface(h);
        txtBioTitle.setText("bio:");

        TextView txtBio = (TextView) findViewById(R.id.other_profile_bio);
        txtBio.setTypeface(h);
        txtBio.setText(bio);

        TextView txtPopupname = (TextView) dialog.findViewById(R.id.popup1_profile_name);
        txtPopupname.setText(fname+" "+lname);
        txtPopupname.setTypeface(h);

        TextView txtPopupage = (TextView) dialog.findViewById((R.id.popup1_profile_age)) ;
        txtPopupage.setText("Age:"+ age);
        txtPopupage.setTypeface(h);

        TextView txtPopupgender = (TextView) dialog.findViewById((R.id.popup1_profile_gender)) ;
        txtPopupgender.setText(gender);
        txtPopupgender.setTypeface(h);

        TextView txtPopupbioTitle = (TextView) dialog.findViewById((R.id.popup1_profile_bio_title)) ;
        txtPopupbioTitle.setText("Bio:");
        txtPopupbioTitle.setTypeface(h);

        TextView txtPopupbio = (TextView) dialog.findViewById((R.id.popup1_profile_bio)) ;
        txtPopupbio.setText(bio);
        txtPopupbio.setTypeface(h);

        Button accept = (Button) dialog.findViewById(R.id.popup1_Accept);
        accept.setTypeface(heading);

        Button reject = (Button) dialog.findViewById(R.id.popup1_Reject);
        reject.setTypeface(heading);

       icebreak.setOnClickListener(new View.OnClickListener()
       {
           @Override
           public void onClick(View view)
           {
                dialog.show();
           }
       });
    }
}
