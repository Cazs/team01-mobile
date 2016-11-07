package com.codcodes.icebreaker.tabs;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.screens.Edit_ProfileActivity;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.screens.InitialActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.screens.RewardsAchievementsActivity;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.screens.SettingsActivity;
import com.facebook.login.LoginManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by tevin on 2016/07/13.
 */

public class ProfileFragment extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private TextView name;
    private TextView age;
    private TextView occupation;
    private String profilePicture;
    private String Name;
    private String Age;
    private String Occupation;
    private String Bio;
    private String Catchphrase;
    private String Gender;
    private ImageView circularImageView;
    private Bitmap circularbitmap = null;
    private User user;
    private View v;
    private FloatingActionButton editButton;
    private static final boolean DEBUG = false;
    private static final String TAG = "IB/ProfileFragment";
    private ProgressBar pb_profile;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_profile, container, false);
        mgr = getActivity().getAssets();

        final String username = SharedPreference.getUsername(getContext());

        pb_profile  = (ProgressBar) v.findViewById(R.id.pb_profile_pic);
        LocalComms.showImageProgressBar(pb_profile);

        Typeface h = Typeface.createFromAsset(mgr, "Infinity.ttf");
        name = (TextView) v.findViewById(R.id.profile_name);
        name.setTypeface(h);


        age = (TextView) v.findViewById(R.id.profile_age);
        age.setTypeface(h);

        occupation = (TextView) v.findViewById(R.id.profile_occupation);
        occupation.setTypeface(h);


        final TextView rewards = (TextView) v.findViewById(R.id.profile_Rewards);
        rewards.setTypeface(h);
        rewards.setText("Reward");

        TextView settings = (TextView) v.findViewById(R.id.profile_settings);
        settings.setTypeface(h);
        settings.setText("Settings");


        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                Bitmap bitmap = null;
                ArrayList<User> userList = null;
                String usrJson = null;
                try
                {
                    usrJson = RemoteComms.sendGetRequest("getUser/" + username);
                    System.err.println(">>>>>>>>>>>"+usrJson);
                    userList = new ArrayList<>();
                    JSON.<User>getJsonableObjectsFromJson(usrJson,userList,User.class);
                } catch (IOException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,"IOE: "+e.getMessage(),e);
                    //TODO: Error Logging
                    try
                    {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1)
                    {
                        Toast.makeText(getActivity(),e1.getMessage(),Toast.LENGTH_LONG).show();
                        Log.d(TAG,"IOE: "+e1.getMessage(),e1);
                    }
                    return;
                } catch (java.lang.InstantiationException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                } catch (IllegalAccessException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                }
                //user = readUser(username);
                if(userList==null)
                {
                    //TODO: Notify user
                    Log.d(TAG,"Something went wrong while we were trying to read your profile.");
                }
                else if(userList.isEmpty())
                {
                    Toast.makeText(getActivity(),"Your username was not found",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Username '" + username + "' not found on DB");
                    //TODO: Error Logging
                }
                else if(userList.size()>1)//More than one user returned - unlikely but it doesn't hurt to be sure
                {
                    Toast.makeText(getActivity(),"Your username was not found",Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Found multiple users for user '" + username + "' on DB");
                    //TODO: Error Logging
                }
                else//All is well
                {
                    user = userList.get(0);
                    Name = user.getFirstname() + " " + user.getLastname();
                    Age = String.valueOf(user.getAge());
                    Occupation = user.getOccupation();
                    Bio = user.getBio();
                    Catchphrase = user.getCatchphrase();
                    Gender = user.getGender();
                    profilePicture = "/Icebreak/profile/" + username + ".png";

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                    try
                    {
                        bitmap = LocalComms.getImage(getActivity(), username, ".png", "/profile", options);
                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                    }
                    catch (IOException e)
                    {
                        LocalComms.logException(e);
                    }
                }

                Runnable r = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        age.setText(Age);
                        name.setText(Name);
                        occupation.setText(Occupation);
                        circularImageView = (ImageView) v.findViewById(R.id.circleview);
                        if(circularbitmap!=null)
                            circularImageView.setImageBitmap(circularbitmap);
                        editButton.setVisibility(View.VISIBLE);
                        LocalComms.hideImageProgressBar(pb_profile);
                    }
                };
                runOnUI(r);
            }
        });
        thread.start();

        rewards.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(user!=null)
                {
                    Intent intent = new Intent(view.getContext(), RewardsAchievementsActivity.class);
                    intent.putExtra("User",user);
                    startActivity(intent);
                }else
                {
                    Toast.makeText(getActivity(),"Profile has not yet been loaded.",Toast.LENGTH_LONG).show();
                }
            }
        });

        settings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), SettingsActivity.class);

                startActivity(intent);
            }
        });

        ImageView reward_icon = (ImageView) v.findViewById(R.id.rewards_icon);

        int color = Color.parseColor("#46bdf0");
        reward_icon.setColorFilter(color);

        ImageView setting_icon = (ImageView) v.findViewById(R.id.setting_icon);
        setting_icon.setColorFilter(color);

        editButton = (FloatingActionButton) v.findViewById(R.id.EditButton);
        editButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(view.getContext(), Edit_ProfileActivity.class);
                intent.putExtra("First Name",user.getFirstname());
                intent.putExtra("Last Name", user.getLastname());
                intent.putExtra("Age",Age);
                intent.putExtra("Occupation",Occupation);
                intent.putExtra("Bio",Bio);
                intent.putExtra("Catchphrase",Catchphrase);
                intent.putExtra("Gender",Gender);
                intent.putExtra("Picture",profilePicture);
                intent.putExtra("Username",SharedPreference.getUsername(getActivity()));
                startActivity(intent);
            }
        });

        Button logOut = (Button) v.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Clean up
                try
                {
                    WritersAndReaders.writeAttributeToConfig(Config.LOC_LNG.getValue(),"0");
                    WritersAndReaders.writeAttributeToConfig(Config.LOC_LAT.getValue(),"0");
                    WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),"0");
                    WritersAndReaders.writeAttributeToConfig(Config.DLG_ACTIVE.getValue(), Config.DLG_ACTIVE_FALSE.getValue());
                } catch (IOException e)
                {
                    LocalComms.logException(e);
                }
                SharedPreference.logOut(view.getContext());
                LoginManager.getInstance().logOut();
                //Go to landing page
                Intent intent = new Intent(view.getContext(), InitialActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        return v;
    }

    public void runOnUI(Runnable r)
    {
        if(ProfileFragment.this!=null)
            if(ProfileFragment.this.getActivity()!=null)
                ProfileFragment.this.getActivity().runOnUiThread(r);
    }

    public static ProfileFragment newInstance(Context context)
    {
        ProfileFragment e = new ProfileFragment();
        //mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }
}