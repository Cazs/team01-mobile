package com.codcodes.icebreaker.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.Edit_ProfileActivity;
import com.codcodes.icebreaker.Event;
import com.codcodes.icebreaker.InitialActivity;
import com.codcodes.icebreaker.LoginActivity;
import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.RewardsActivity;
import com.codcodes.icebreaker.SharedPreference;
import com.codcodes.icebreaker.User;
import com.codcodes.icebreaker.WritersAndReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tevin on 2016/07/13.
 */
public class Profile_Page extends android.support.v4.app.Fragment
{
    private static AssetManager mgr;
    private TextView name;
    private TextView age;
    private TextView occupation;
    private String profilePicture;
    private String Name;
    private String Age;
    private String Occupation;
    private static final boolean DEBUG = true;
    private final String TAG = "ICEBREAK";


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        validateStoragePermissions(getActivity());

        View v = inflater.inflate(R.layout.profile_page,container,false);
        //TODO: Use this information to send to database to see whch user it is.
        final String username = SharedPreference.getUsername(v.getContext());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);

                    profilePicture = "/Icebreak/"+username;
                    if(!new File(Environment.getExternalStorageDirectory().getPath()
                            + "/Icebreak/" + username).exists())
                    {
                        Log.d(TAG,"No cached "+username+",Image download in progress..");
                        if(imageDownload(soc,username))
                            Log.d(TAG,"Image download successful");
                        else
                            Log.d(TAG,"Image download unsuccessful");
                    }


                    PrintWriter out = new PrintWriter(soc.getOutputStream());

                    String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(username, "UTF-8");

                    out.print("GET /IBUserRequestService.svc/getUser HTTP/1.1\r\n"
                            + "Host: icebreak.azurewebsites.net\r\n"
                            + "Content-Type: text/plain;\r\n"// charset=utf-8
                            + "Content-Length: " + data.length() + "\r\n\r\n"
                            + data);
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                    String resp;
                    //Wait for response indefinitely TODO: Time-Out
                    while(!in.ready()){}

                    String userJson = "";
                    boolean openUserRead = false;
                    while((resp = in.readLine())!=null)
                    {
                        //if(DEBUG)System.out.println(resp);

                        if(resp.equals("0"))
                        {
                            out.close();
                            //in.close();
                            soc.close();
                            if(DEBUG)System.out.println(">>Done<<");
                            break;//EOF
                        }

                        if(resp.isEmpty())
                            if(DEBUG)System.out.println("\n\nEmpty Line\n\n");

                        if(resp.contains("["))
                        {
                            if(DEBUG)System.out.println("Opening at>>" + resp.indexOf("["));
                            openUserRead = true;
                        }

                        if(openUserRead)
                            userJson += resp;//.substring(resp.indexOf('['));

                        if(resp.contains("]"))
                        {
                            if(DEBUG)System.out.println("Closing at>>" + resp.indexOf("]"));
                            openUserRead = false;
                        }
                        User user = getUser(userJson);
                        Name = user.getFirstname()+" "+user.getLastname();
                        Age = String.valueOf(user.getAge());
                        Occupation = user.getOccupation();

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


        Typeface h = Typeface.createFromAsset(mgr,"Infinity.ttf");
        name = (TextView) v.findViewById(R.id.profile_name);
        name.setTypeface(h);
        name.setText(Name);


        age = (TextView) v.findViewById(R.id.profile_age);
        age.setTypeface(h);
        age.setText(Age);

        occupation = (TextView) v.findViewById(R.id.profile_occupation);
        occupation.setTypeface(h);


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
                Intent intent = new Intent(view.getContext(),RewardsActivity.class);
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

        Bitmap bitmap = BitmapFactory.decodeFile(profilePicture);
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

        Button logOut = (Button) v.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreference.logOut(view.getContext());
                Intent intent = new Intent(view.getContext(),InitialActivity.class);
                startActivity(intent);

            }
        });

        return v;


    }


    public void validateStoragePermissions(Activity activity)
    {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE =
                {
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                };
        //Check for write permissions
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            //No permission - prompt the user for permission
            ActivityCompat.requestPermissions
                    (
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
        }
    }
    public static Profile_Page newInstance(Context context)
    {
        Profile_Page e = new Profile_Page();
        mgr = context.getAssets();
        Bundle b = new Bundle();
        e.setArguments(b);
        return e;
    }
    public static boolean imageDownload(Socket soc,String userName) throws IOException
    {
        System.out.println("Sending image download request");
        PrintWriter out = new PrintWriter(soc.getOutputStream());
        //Android: final String base64 = ;
        String headers = "GET /IBUserRequestService.svc/imageDownload/"+userName+" HTTP/1.1\r\n"
                + "Host: icebreak.azurewebsites.net\r\n"
                //+ "Content-Type: application/x-www-form-urlencoded\r\n"
                + "Content-Type: text/plain;\r\n"// charset=utf-8
                + "Content-Length: 0\r\n\r\n";

        out.print(headers);
        out.flush();

        BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        String resp;
        while(!in.ready()){}
        while((resp = in.readLine())!=null)
        {
            //System.out.println(resp);

            if(resp.toLowerCase().contains("payload"))
            {
                String base64bytes = resp.split(":")[1];
                base64bytes = base64bytes.substring(1, base64bytes.length());
                byte[] binFileArr = android.util.Base64.decode(base64bytes, android.util.Base64.DEFAULT);
                WritersAndReaders.saveImage(binFileArr,userName);
                return true;
            }

            if(!in.ready())
            {
                if(DEBUG)System.out.println(">>Done<<");
                break;
            }
        }
        out.close();
        //in.close();
        soc.close();
        return false;
    }
    private static User getUser(String json)
    {
        System.out.println("Reading User: " + json);
        //TODO: Regex fo user string
        String p2 = "\"([a-zA-Z0-9\\s~`!@#$%^&*)(_+-={}\\[\\];',./\\|<>?]*)\"\\:(\"[a-zA-Z0-9\\s~`!@#$%^&*()_+-={}\\[\\];',./\\|<>?]*\"|\"[0-9,]\"|\\d+)";
        Pattern p = Pattern.compile(p2);
        Matcher m = p.matcher(json);
        User user = new User();
        while(m.find())
        {
            String pair = m.group(0);
            //process key value pair
            pair = pair.replaceAll("\"", "");
            if(pair.contains(":"))
            {
                //if(DEBUG)System.out.println("Found good pair");
                String[] kv_pair = pair.split(":");
                String var = kv_pair[0];
                String val = kv_pair[1];
                switch(var)
                {
                    case "Fname":
                        user.setFirstname(val);
                        break;
                    case "Lname":
                        user.setLastname(val);
                        break;
                    case "Age":
                        user.setAge(Integer.valueOf(val));
                        break;
                    case "Occupation":
                        user.setOccupation(val);
                        break;

                }
            }
            //look for next pair
            json = json.substring(m.end());
            m = p.matcher(json);
        }
        return user;
    }
}
