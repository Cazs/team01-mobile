package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.Edit_ProfileActivity;
import com.codcodes.icebreaker.InitialActivity;
import com.codcodes.icebreaker.LoginActivity;
import com.codcodes.icebreaker.R;

/**
 * Created by tevin on 2016/07/13.
 */
public class Profile_Page extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;




    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {


        View v = inflater.inflate(R.layout.profile_page,container,false);



        Typeface h = Typeface.createFromAsset(mgr,"Infinity.ttf");
        TextView name = (TextView) v.findViewById(R.id.profile_name);
        name.setTypeface(h);
        name.setText("Selena Gomez"); // TODO: get name from database


        TextView age = (TextView) v.findViewById(R.id.profile_age);
        age.setTypeface(h);
        age.setText("Age: 21");

        TextView occupation = (TextView) v.findViewById(R.id.profile_occupation);
        occupation.setTypeface(h);
        occupation.setText("Singer/Songwriter/Actress");

        final TextView rewards = (TextView) v.findViewById(R.id.profile_Rewards);
        rewards.setTypeface(h);
        rewards.setText("Rewards");


        TextView settings = (TextView) v.findViewById(R.id.profile_settings);
        settings.setTypeface(h);
        settings.setText("Settings");





        rewards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int link_color = Color.parseColor("#4665f0");
                Intent intent = new Intent(view.getContext(),InitialActivity.class);
               // rewards.startAnimation();
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),InitialActivity.class);
                startActivity(intent);
            }
        });

        ImageView reward_icon = (ImageView) v.findViewById(R.id.rewards_icon);

        int color = Color.parseColor("#46bdf0");
        reward_icon.setColorFilter(color);

        ImageView setting_icon = (ImageView) v.findViewById(R.id.setting_icon);
        setting_icon.setColorFilter(color);

        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.seleena);
        Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap,100);

        ImageView circularImageView = (ImageView) v.findViewById(R.id.circleview);
        circularImageView.setImageBitmap(circularbitmap);

        FloatingActionButton editButton = (FloatingActionButton) v.findViewById(R.id.Edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Edit_ProfileActivity.class);
                startActivity(intent);
            }
        });

        return v;


    }




    public static Profile_Page newInstance(Context context)
    {
        Profile_Page e = new Profile_Page();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }
}
