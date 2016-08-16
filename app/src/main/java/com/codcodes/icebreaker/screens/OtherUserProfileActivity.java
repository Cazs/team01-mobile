package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;

public class OtherUserProfileActivity extends AppCompatActivity {
    private TextView profile;
    private String fname;
    private String lname;
    private String username;
    private String age;
    private String occupation;
    private String bio;
    private String gender;
    private ProgressDialog progress;
    private boolean prog_bar = false;
    private final int MSG_ID_LEN = 20;
    private static final String TAG = "IB/OtherUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_one);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            //All this information will need to be sent to other party
            fname = extras.getString("Firstname");
            lname = extras.getString("Lastname");
            username = extras.getString("Username");
            age = Integer.toString(extras.getInt("Age"));
            occupation = extras.getString("Occupation");
            bio = extras.getString("Bio");
            gender = extras.getString("Gender");
        }
        else
        {
            //Go back to the EventsFragment
            Intent intentMainAct = new Intent(this,MainActivity.class);
            startActivity(intentMainAct);
        }

        profile = (TextView) findViewById(R.id.Profile);
        Typeface heading = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        profile.setTypeface(heading);

        //Load and render selected user's profile
        final Activity ctxt = this;
        final ImageView profileImage = (ImageView) findViewById(R.id.other_pic);
        Thread tUserProfileLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                final Bitmap bitmap = Restful.getImage(ctxt, username, ".png", "/profile", options);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileImage.setImageBitmap(bitmap);
                    }
                });
            }
        });
        tUserProfileLoader.start();

        Button icebreak = (Button) findViewById(R.id.icebreak);
        icebreak.setTypeface(heading);

        Typeface h = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        TextView txtName = (TextView) findViewById(R.id.other_profile_name);
        txtName.setTypeface(h);
        txtName.setText(fname + " " + lname); // TODO: get name from database

        TextView txtAge = (TextView) findViewById(R.id.other_profile_age);
        txtAge.setTypeface(h);
        txtAge.setText("Age:" + age);

        TextView txtOccupation = (TextView) findViewById(R.id.other_profile_occupation);
        txtOccupation.setTypeface(h);
        txtOccupation.setText(occupation);

        TextView txtGender = (TextView) findViewById(R.id.other_profile_gender);
        txtGender.setTypeface(h);
        txtGender.setText(gender);

        TextView txtBioTitle = (TextView) findViewById(R.id.other_profile_bio_title);
        txtBioTitle.setTypeface(h);
        txtBioTitle.setText("Bio:");

        TextView txtBio = (TextView) findViewById(R.id.other_profile_bio);
        txtBio.setTypeface(h);
        txtBio.setText(bio);



        icebreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog.show();
                Thread tSender = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Looper.prepare();

                        //Store on local DB
                        MessagePollHelper dbHelper = new MessagePollHelper(ctxt);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        dbHelper.onCreate(db);//Create Message table if it doesn't exist

                        String msgId = WritersAndReaders.getRandomIdStr(MSG_ID_LEN);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                        /*String query = "INSERT INTO " + MessagePollContract.MessageEntry.TABLE_NAME
                                + " VALUES(?,?,?,?,?)";
                        String[] values = {msgId,};*/
                        ContentValues msg_data = new ContentValues();
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID,msgId);
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE,"ICEBREAK");
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS,String.valueOf(MESSAGE_STATUSES.ICEBREAK.getStatus()));
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER,SharedPreference.getUsername(getBaseContext()).toString());
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER,username);
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME,sdf.format(new Date()));

                        long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME,null,msg_data);
                        db.close();
                        Log.d(TAG, "Inserted into Message table: new row=" + newRowId);

                        //showProgressBar();
                        ArrayList<AbstractMap.SimpleEntry<String, String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_id", msgId));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message", "ICEBREAK"));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status", String.valueOf(MESSAGE_STATUSES.ICEBREAK.getStatus())));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender", SharedPreference.getUsername(getBaseContext()).toString()));//TODO
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", username));//TODO

                        //Send to server
                        try
                        {
                            final int response_code = Restful.postData("addMessage", msg_details);
                            //Update UI
                            //prog_bar = false;
                            //progress.hide();
                            if(response_code != HttpURLConnection.HTTP_OK)
                            {
                                Toast.makeText(getBaseContext(),"Could not send Icebreak request: " + response_code, Toast.LENGTH_LONG).show();
                                Log.d(TAG,"Could not send Icebreak request: " + response_code);
                            }
                            else
                            {
                                Log.d(TAG,"Icebreak Sent");
                                Toast.makeText(getBaseContext(), "Icebreak Sent", Toast.LENGTH_LONG).show();
                            }
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });*/
                        }
                        catch (IOException e)
                        {
                            //e.printStackTrace();
                            Log.d(TAG, e.getMessage());
                            Toast.makeText(getBaseContext(), "Unable to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                tSender.start();
            }
        });
               /*Thread tSender = new Thread(new Runnable()
               {
                   @Override
                   public void run()
                   {
                       Looper.prepare();

                       try {
                           //Populate arrays
                           ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                           ArrayList<AbstractMap.SimpleEntry<String, String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                           msg_details.add(new AbstractMap.SimpleEntry<String, String>("message", "ICEBREAKING"));
                           msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status", String.valueOf(MESSAGE_STATUSES.ICEBREAK)));
                           msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender", SharedPreference.getUsername(getBaseContext())));
                           msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", username));//TODO

                           //Write to server
                           int code = Restful.postData("addMessage", msg_details);
                           if(code != HttpURLConnection.HTTP_OK)
                           {
                               Toast.makeText(getBaseContext(),"Could not send request: " + code, Toast.LENGTH_LONG).show();
                               Log.d(TAG,"Could not send request: " + code);
                           }
                           else
                           {
                               Log.d(TAG,"Icebreak Sent");
                           }
                           runOnUiThread(new Runnable()
                           {
                               @Override
                               public void run()
                               {
                                   progress.hide();
                               }
                           });
                       }
                       catch (IOException e)
                       {
                           //TODO: Logging
                           Log.d(TAG,e.getMessage());
                       }
                   }
               });
               tSender.start();*/
    }

    public void hideProgressBar()
    {
        progress.hide();
    }

    public void showProgressBar()
    {
        progress=new ProgressDialog(this);
        progress.setMessage("Sending request");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        //while (prog_bar)
            progress.show();
        //progress.hide();
    }
}