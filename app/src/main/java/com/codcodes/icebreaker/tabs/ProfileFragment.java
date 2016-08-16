package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.screens.Edit_ProfileActivity;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.screens.InitialActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.screens.RewardsActivity;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.internal.zzir.runOnUiThread;


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
    private User user;
    private View v;
    private Bitmap circularbitmap = null;
    private FloatingActionButton editButton;
    private static final boolean DEBUG = false;
    private static final String TAG = "IB/ProfileFragment";


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        v = inflater.inflate(R.layout.fragment_profile, container, false);
        mgr = getActivity().getAssets();
        //TODO: Use this information to send to database to see whch user it is.
        final String username = SharedPreference.getUsername(v.getContext()).toLowerCase();

        Typeface h = Typeface.createFromAsset(mgr, "Infinity.ttf");
        name = (TextView) v.findViewById(R.id.profile_name);
        name.setTypeface(h);


        age = (TextView) v.findViewById(R.id.profile_age);
        age.setTypeface(h);

        occupation = (TextView) v.findViewById(R.id.profile_occupation);
        occupation.setTypeface(h);


        final TextView rewards = (TextView) v.findViewById(R.id.profile_Rewards);
        rewards.setTypeface(h);
        rewards.setText("Rewards");

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
                    usrJson = Restful.sendGetRequest("getUser/" + username);
                    userList = new ArrayList<>();
                    JSON.<User>getJsonableObjectsFromJson(usrJson,userList,User.class);
                } catch (IOException e)
                {
                    Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d(TAG,e.getMessage());
                    //TODO: Error Logging
                    try
                    {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                    System.exit(-1);
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
                    user= userList.get(0);
                    Name = user.getFirstname() + " " + user.getLastname();
                    Age = String.valueOf(user.getAge());
                    Occupation = user.getOccupation();

                    Bio = user.getBio();
                    Catchphrase = user.getCatchphrase();
                    Gender = user.getGender();
                    profilePicture = "/Icebreak/profile/" + username + ".png";
                    //Look for user profile image
                    if (!new File(Environment.getExternalStorageDirectory().getPath()
                            + profilePicture).exists())
                    {
                        //if (imageDownload(u.getUsername() + ".png", "/profile")) {
                        if (Restful.imageDownloader(username,".png", "/profile", getActivity()))
                        {
                            bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                    + profilePicture, getActivity());
                            circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                        } else //user has no profile yet - attempt to load default profile image
                        {
                            if (!new File(Environment.getExternalStorageDirectory().getPath().toString()
                                    + "/Icebreak/profile/profile_default.png").exists())
                            {
                                //Attempt to download default profile image
                                if (Restful.imageDownloader("profile_default",".png", "/profile", getActivity()))
                                {
                                    bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/profile_default.png", getActivity());
                                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                } else //Couldn't download default profile image
                                {
                                    Toast.makeText(getActivity(), "Could not download default profile images, please check your internet connection.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else//default profile image exists
                            {
                                bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                        + "/Icebreak/profile/profile_default.png", getActivity());
                                circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                            }
                        }
                    } else//user profile image exists
                    {
                        bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                + profilePicture, getActivity());
                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                    }
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        age.setText(Age);
                        name.setText(Name);
                        occupation.setText(Occupation);
                        circularImageView = (ImageView) v.findViewById(R.id.circleview);
                        circularImageView.setImageBitmap(circularbitmap);
                        editButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        thread.start();


        rewards.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                int link_color = Color.parseColor("#4665f0");
                Intent intent = new Intent(view.getContext(), RewardsActivity.class);
                intent.putExtra("Picture",profilePicture);
                intent.putExtra("Name",user.getFirstname() + " "+user.getLastname());

                // rewards.startAnimation();
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), InitialActivity.class);

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
                startActivity(intent);
            }
        });

        Button logOut = (Button) v.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SharedPreference.logOut(view.getContext());
                Intent intent = new Intent(view.getContext(), InitialActivity.class);
                startActivity(intent);
            }
        });
        return v;
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