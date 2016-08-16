package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
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
<<<<<<< HEAD
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
=======
import com.codcodes.icebreaker.auxilary.ImageConverter;
>>>>>>> 11913b0910bfe2ead083b83a128da876a4a90394
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.SharedPreference;

<<<<<<< HEAD
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
=======
import org.w3c.dom.Text;

public class OtherUserProfileActivity extends AppCompatActivity
{
>>>>>>> 11913b0910bfe2ead083b83a128da876a4a90394

public class OtherUserProfileActivity extends AppCompatActivity {
    private TextView profile;
    private String fname;
    private String lname;
    private String username;
    private String age;
    private String occupation;
    private String bio;
    private String gender;
<<<<<<< HEAD
    private ProgressDialog progress;
    private boolean prog_bar = false;
    private static final String TAG = "IB/OtherUserActivity";

=======
    Button reject;
    Button accept;
    private Bitmap circularbitmap1;
    private Bitmap circularbitmap2;
    private Bitmap circularbitmap3;
    private Bitmap circularbitmap4;
>>>>>>> 11913b0910bfe2ead083b83a128da876a4a90394
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Dialog dialog = new Dialog(this);
        final Dialog acceptDialog = new Dialog(this);
        final Dialog rejectDialog = new Dialog(this);
        dialog.setContentView(R.layout.pop_up_one);
        acceptDialog.setContentView(R.layout.pop_up_two);
        rejectDialog.setContentView(R.layout.pop_up_three);

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

        Button icebreak = (Button) findViewById(R.id.btnIcebreak);
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
        txtBioTitle.setText("bio:");

        TextView txtBio = (TextView) findViewById(R.id.other_profile_bio);
        txtBio.setTypeface(h);
        txtBio.setText(bio);



<<<<<<< HEAD
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

                        //showProgressBar();
                        ArrayList<AbstractMap.SimpleEntry<String, String>> msg_details = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message", "ICEBREAK"));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_status", String.valueOf(MESSAGE_STATUSES.ICEBREAK.getStatus())));
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_sender", SharedPreference.getUsername(getBaseContext()).toString()));//TODO
                        msg_details.add(new AbstractMap.SimpleEntry<String, String>("message_receiver", username.toString()));//TODO

                        //Send to server
                        try {
                            final int response_code = Restful.postData("addMessage", msg_details);
                            //Update UI
                            prog_bar = false;
                            //progress.hide();
                            if(response_code != HttpURLConnection.HTTP_OK)
                            {
                                Toast.makeText(getBaseContext(),"Could not send request: " + response_code, Toast.LENGTH_LONG).show();
                                Log.d(TAG,"Could not send request: " + response_code);
                            }
                            else
                            {
                                Log.d(TAG,"Icebreak Sent");
                                Toast.makeText(getBaseContext(), "Message Sent", Toast.LENGTH_LONG).show();
                            }
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });*/
                        } catch (IOException e) {
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
=======
        TextView txtPopupbioTitle = (TextView) dialog.findViewById((R.id.popup1_profile_bio_title)) ;
        txtPopupbioTitle.setText("Bio:");
        txtPopupbioTitle.setTypeface(h);

        TextView txtPopupbio = (TextView) dialog.findViewById((R.id.popup1_profile_bio)) ;
        txtPopupbio.setText(bio);
        txtPopupbio.setTypeface(h);

        accept = (Button) dialog.findViewById(R.id.popup1_Accept);
        accept.setTypeface(heading);

        reject = (Button) dialog.findViewById(R.id.popup1_Reject);
        reject.setTypeface(heading);

       icebreak.setOnClickListener(new View.OnClickListener()
       {
           @Override
           public void onClick(View view)
           {
                dialog.show();
           }
       });
        ImageView circularImageView1;
        ImageView circularImageView2;


        Bitmap bitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.liam);
        circularbitmap1 = ImageConverter.getRoundedCornerBitMap(bitmap1, R.dimen.dp_size_300);

        Bitmap bitmap2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.seleena);
        circularbitmap2 = ImageConverter.getRoundedCornerBitMap(bitmap2, R.dimen.dp_size_300);

        circularImageView1 = (ImageView) acceptDialog.findViewById(R.id.other_pic1);
        circularImageView1.setImageBitmap(circularbitmap1);


        circularImageView2 = (ImageView) acceptDialog.findViewById(R.id.other_pic2);
        circularImageView2.setImageBitmap(circularbitmap2);

        TextView sucess = (TextView) acceptDialog.findViewById(R.id.SuccessfulMatch);
        sucess.setTypeface(heading);
        TextView phrase = (TextView) acceptDialog.findViewById(R.id.phrase);
        phrase.setTypeface(heading);
        TextView or = (TextView) acceptDialog.findViewById(R.id.or);
        or.setTypeface(heading);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptDialog.show();
            }
        });


        ImageView circularImageView3;
        ImageView circularImageView4;

        TextView unsucess = (TextView) rejectDialog.findViewById(R.id.UnsuccessfulMatch);
        unsucess.setTypeface(heading);
        TextView phrase2 = (TextView) rejectDialog.findViewById(R.id.phrase2);
        phrase2.setTypeface(heading);

        Bitmap bitmap3 = BitmapFactory.decodeResource(this.getResources(),R.drawable.liam);
        circularbitmap3 = ImageConverter.getRoundedCornerBitMap(bitmap3, R.dimen.dp_size_300);

        Bitmap bitmap4 = BitmapFactory.decodeResource(this.getResources(),R.drawable.seleena);
        circularbitmap4 = ImageConverter.getRoundedCornerBitMap(bitmap4, R.dimen.dp_size_300);

        circularImageView3 = (ImageView) rejectDialog.findViewById(R.id.other_pic3);
        circularImageView3.setImageBitmap(circularbitmap3);


        circularImageView4 = (ImageView) rejectDialog.findViewById(R.id.other_pic4);
        circularImageView4.setImageBitmap(circularbitmap4);

        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rejectDialog.show();
            }
        });


>>>>>>> 11913b0910bfe2ead083b83a128da876a4a90394
    }
}
