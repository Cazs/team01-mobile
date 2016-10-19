package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessageHelper;
import com.codcodes.icebreaker.model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;

public class OtherUserProfileActivity extends AppCompatActivity
{
    private TextView profile;
    private ProgressDialog progress;
    private ProgressBar pb_profile;

    private User user = null;

    private boolean prog_bar = false;
    private final int MSG_ID_LEN = 20;
    private static final String TAG = "IB/OtherUserActivity";

    private Bitmap bmp_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        pb_profile  = (ProgressBar) findViewById(R.id.pb_other_pic);
        //Show loading icon on profile
        LocalComms.showImageProgressBar(pb_profile);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null)
        {
            //All this information will need to be sent to other party
            user = extras.getParcelable("User");
        }
        else
        {
            //Go back to the EventsFragment
            Intent intentMainAct = new Intent(this,MainActivity.class);
            startActivity(intentMainAct);
        }

        profile = (TextView) findViewById(R.id.main_heading);
        Typeface heading = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");
        profile.setTypeface(heading);
        profile.setTextSize(34);

        //Load and render selected user's profile
        final Activity ctxt = this;
        final ImageView profileImage = (ImageView) findViewById(R.id.other_pic);
        Thread tUserProfileLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                pb_profile.setProgress(pb_profile.getProgress()+10);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                try
                {
                    bmp_profile = LocalComms.getImage(ctxt, user.getUsername(), ".png", "/profile", options);
                    if (bmp_profile == null)
                        bmp_profile = RemoteComms.getImage(ctxt, user.getUsername(), ".png", "/profile", options);
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (bmp_profile != null)
                            {
                                profileImage.setImageBitmap(bmp_profile);
                                LocalComms.hideImageProgressBar(pb_profile);
                            }
                        }
                    });
                }
                catch (IOException e)
                {
                    if(e.getMessage()!=null)
                        Log.wtf(TAG,e.getMessage(),e);
                    else
                        e.printStackTrace();
                }
            }
        });
        tUserProfileLoader.start();

        Button icebreak = (Button) findViewById(R.id.icebreak);
        icebreak.setTypeface(heading);

        Typeface h = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        TextView txtName = (TextView) findViewById(R.id.other_profile_name);
        txtName.setTypeface(h);
        txtName.setText(LocalComms.getValidatedName(user));

        TextView txtAge = (TextView) findViewById(R.id.other_profile_age);
        txtAge.setTypeface(h);
        txtAge.setText("Age:" + user.getAge());

        TextView txtOccupation = (TextView) findViewById(R.id.other_profile_occupation);
        txtOccupation.setTypeface(h);
        txtOccupation.setText(user.getOccupation());

        TextView txtGender = (TextView) findViewById(R.id.other_profile_gender);
        txtGender.setTypeface(h);
        txtGender.setText(user.getGender());

        TextView txtBioTitle = (TextView) findViewById(R.id.other_profile_bio_title);
        txtBioTitle.setTypeface(h);
        txtBioTitle.setText("Bio:");

        TextView txtBio = (TextView) findViewById(R.id.other_profile_bio);
        txtBio.setTypeface(h);
        txtBio.setText(user.getBio());

        icebreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress = LocalComms.showProgressDialog(OtherUserProfileActivity.this,"Sending request...");
                Thread tSender = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Looper.prepare();

                        //Store on local DB
                        MessageHelper dbHelper = new MessageHelper(ctxt);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        dbHelper.onCreate(db);//Create Message table if it doesn't exist

                        String msgId = WritersAndReaders.getRandomIdStr(MSG_ID_LEN);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                        /*String query = "INSERT INTO " + MessagePollContract.MessageEntry.TABLE_NAME
                                + " VALUES(?,?,?,?,?)";
                        String[] values = {msgId,};*/
                        ContentValues msg_data = new ContentValues();
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID,msgId);
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE,"<ICEBREAK>");
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS,String.valueOf(MESSAGE_STATUSES.ICEBREAK.getStatus()));
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER,SharedPreference.getUsername(getBaseContext()).toString());
                        msg_data.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER,user.getUsername());
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
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", user.getUsername()));//TODO

                        //Send to server
                        try
                        {
                            final int response_code = RemoteComms.postData("addMessage", msg_details);
                            //Update UI
                            //progress.hide();
                            Message message;
                            if(response_code != HttpURLConnection.HTTP_OK)
                            {
                                message = toastHandler("Could not send Icebreak request: " + response_code).obtainMessage();
                                message.sendToTarget();
                                Log.d(TAG,"Could not send Icebreak request: " + response_code);
                            }
                            else
                            {
                                Log.d(TAG,"Icebreak Sent");
                                message = toastHandler("Icebreak sent").obtainMessage();
                                message.sendToTarget();
                            }
                        }
                        catch (IOException e)
                        {
                            //TODO: Better logging
                            Log.wtf(TAG, e.getMessage(),e);
                            Toast.makeText(getBaseContext(), "Unable to send IceBreak request.", Toast.LENGTH_LONG).show();
                        }
                        LocalComms.hideProgressBar(progress);
                    }
                });
                tSender.start();
            }
        });
    }

    private Handler toastHandler(final String text)
    {
        Handler toastHandler = new Handler(Looper.getMainLooper())
        {
            public void handleMessage(Message msg)
            {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            }
        };
        return toastHandler;
    }
}
