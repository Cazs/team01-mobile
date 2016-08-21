package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;

/**
 * Created by Casper on 2016/08/17.
 */
public class IBDialog extends Activity
{
    private Dialog dialog;
    private ProgressDialog progress;
    public static boolean active = false;
    public static boolean status_changing = false;
    public static boolean requesting = true;
    private Bitmap bitmapLocalUser,bitmapRemoteUser;
    private static final String TAG = "IB/IBDialog";
    private static Message icebreak_msg = null;
    private static User requesting_user = null;
    private static User receiving_user = null;

    private TextView txtIBReqPopup_name,txtIBReqPopup_age,txtIBReqPopup_gender,
                        txtIBReqPopup_bioTitle,txtIBReqPopup_bio, txtIBReqPopup_occ;
    private ImageView imgIBReqPopup_OtherUser;
    private Button accept,reject;

    private Typeface ttfInfinity, ttfAilerons;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(this);

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        Intent dlgIntent = getIntent();
        icebreak_msg = dlgIntent.getParcelableExtra("Message");
        receiving_user = dlgIntent.getParcelableExtra("Receiver");
        requesting_user = dlgIntent.getParcelableExtra("Sender");
        System.err.println("Sender: " + requesting_user.getUsername() +
                " FN: " + requesting_user.getFirstname() +
                " LN: " + requesting_user.getLastname());

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

        if(requesting_user!=null)
            bitmapRemoteUser = RemoteComms.getImage(this, requesting_user.getUsername(), ".png", "/profile", options);
        if(receiving_user!=null)
            bitmapLocalUser = RemoteComms.getImage(this, receiving_user.getUsername(), ".png", "/profile", options);

        //if(is_req.toLowerCase().equals("true"))
        if(requesting)//remote is requesting Icebreak with local
        {
            populateIcebreakRequestUI();
            initIcebreakRequestHandlers();

        }else//remote is receiving requested Icebreak response
        {
            if(icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus())
            {
                //Add user to local contacts table
                LocalComms.addContact(getBaseContext(),receiving_user);
                drawAcceptanceUI();
            }
            else if(icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
            {
                drawRejectionUI();
            }
        }

        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
                IBDialog.requesting = false;
                closeActivity();//return focus to the MainActivity
            }
        });
    }

    public void showProgressBar()
    {
        if(progress==null)
            progress = new ProgressDialog(this);
        if(!progress.isShowing())
        {
            progress.setMessage("Loading...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setProgress(0);
            progress.show();
        }
    }

    public void hideProgressBar()
    {
        if(progress!=null)
            if(progress.isShowing())
                progress.dismiss();
    }

    private void populateIcebreakRequestUI()
    {
        dialog.setContentView(R.layout.popup_icebreak);

        txtIBReqPopup_name = (TextView) dialog.findViewById(R.id.ib_req_username);
        txtIBReqPopup_age = (TextView) dialog.findViewById((R.id.ib_req_user_age));
        txtIBReqPopup_gender = (TextView) dialog.findViewById((R.id.ib_req_user_gender));
        txtIBReqPopup_bioTitle = (TextView) dialog.findViewById((R.id.ib_req_user_bio_title));
        txtIBReqPopup_bio = (TextView) dialog.findViewById((R.id.ib_req_user_bio));
        txtIBReqPopup_occ = (TextView) dialog.findViewById((R.id.ib_req_user_occupation));

        imgIBReqPopup_OtherUser = (ImageView) dialog.findViewById(R.id.ib_req_user_image);

        accept = (Button) dialog.findViewById(R.id.ib_req_btn_accept);
        reject = (Button) dialog.findViewById(R.id.ib_req_btn_reject);

        //Set typefaces
        txtIBReqPopup_name.setTypeface(ttfInfinity);
        txtIBReqPopup_age.setTypeface(ttfInfinity);
        txtIBReqPopup_gender.setTypeface(ttfInfinity);
        txtIBReqPopup_bioTitle.setTypeface(ttfInfinity);
        txtIBReqPopup_bio.setTypeface(ttfInfinity);
        txtIBReqPopup_occ.setTypeface(ttfInfinity);
        accept.setTypeface(ttfInfinity);
        reject.setTypeface(ttfInfinity);

        if(imgIBReqPopup_OtherUser!=null && bitmapRemoteUser!=null)
            imgIBReqPopup_OtherUser.setImageBitmap(bitmapRemoteUser);

        String name = LocalComms.getValidatedName(requesting_user);
        txtIBReqPopup_name.setText(name);
        txtIBReqPopup_age.setText(String.valueOf(requesting_user.getAge()));
        txtIBReqPopup_gender.setText(requesting_user.getGender());
        txtIBReqPopup_bioTitle.setText("Bio: ");
        txtIBReqPopup_bio.setText(requesting_user.getBio());
        txtIBReqPopup_occ.setText("Is a " + requesting_user.getOccupation());
    }



    private void initIcebreakRequestHandlers()
    {
        accept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG,"Accept Icebreak request.");
                showProgressBar();
                status_changing = true;
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus());
                Thread tStatusUpdater = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //Update remote DB
                        if(RemoteComms.sendMessage(getBaseContext(),icebreak_msg))//TODO: update by ID
                        {
                            //Update local DB
                            LocalComms.updateMessageStatusById(getBaseContext(),icebreak_msg.getId(),icebreak_msg.getStatus());
                            //Add user to local contacts table
                            LocalComms.addContact(getBaseContext(),requesting_user);

                            Log.d(TAG,"Accept Button> Updated Icebreak request locally and remotely.");
                            status_changing = false;
                        }
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //dialog.dismiss();
                                hideProgressBar();
                                drawAcceptanceUI();
                            }
                        });
                    }
                });
                tStatusUpdater.start();
            }
        });

        reject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showProgressBar();
                status_changing = true;
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus());
                Thread tStatusUpdater = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(RemoteComms.sendMessage(getBaseContext(),icebreak_msg))//TODO: update by ID
                        {
                            LocalComms.updateMessageStatusById(getBaseContext(),icebreak_msg.getId(),icebreak_msg.getStatus());
                            Log.d(TAG,"Reject Button> Updated Icebreak request locally and remotely.");
                        }
                        hideProgressBar();
                        status_changing = false;
                        dialog.dismiss();
                    }
                });
                tStatusUpdater.start();
            }
        });
    }

    private void drawAcceptanceUI()
    {
        dialog.setContentView(R.layout.popup_accept);
        //dialog.show();

        TextView txtSuccessfulMatch = (TextView)dialog.findViewById(R.id.SuccessfulMatch);
        ImageView imgLocalUser = (ImageView)dialog.findViewById(R.id.other_pic1);
        ImageView imgRemoteUser = (ImageView)dialog.findViewById(R.id.other_pic2);
        TextView phrase = (TextView)dialog.findViewById(R.id.phrase);
        Button btnChat = (Button)dialog.findViewById(R.id.popup1_Start_Chatting);
        TextView or = (TextView)dialog.findViewById(R.id.or);
        Button btnContinue = (Button)dialog.findViewById(R.id.popup1_Keep_playing);

        imgLocalUser.setImageBitmap(bitmapLocalUser);
        imgRemoteUser.setImageBitmap(bitmapRemoteUser);

        txtSuccessfulMatch.setTypeface(ttfInfinity);
        phrase.setTypeface(ttfInfinity);
        or.setTypeface(ttfInfinity);

        btnChat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Start ChatActivity
                Log.d(TAG,"End Icebreak request.");
                if(requesting)
                {
                    dialog.dismiss();
                    Intent chatIntent = new Intent(getBaseContext(),ChatActivity.class);
                    chatIntent.putExtra("Username",icebreak_msg.getSender());
                    startActivity(chatIntent);
                }
                else
                {
                    showProgressBar();
                    status_changing = true;
                    //update status
                    icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus());
                    Thread tStatusUpdater = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Send signal to server
                            if(RemoteComms.sendMessage(getBaseContext(),icebreak_msg))//TODO: update by ID
                            {
                                LocalComms.updateMessageStatusById(getBaseContext(),icebreak_msg.getId(),icebreak_msg.getStatus());
                                Log.d(TAG,"Continue Button> Updated Icebreak request locally and remotely.");
                            }
                            hideProgressBar();
                            status_changing = false;
                            dialog.dismiss();
                            Intent chatIntent = new Intent(getBaseContext(),ChatActivity.class);
                            chatIntent.putExtra("Username",icebreak_msg.getSender());
                            startActivity(chatIntent);
                        }
                    });
                    tStatusUpdater.start();
                }
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG,"End Icebreak request.");
                if(requesting)
                {
                    dialog.dismiss();
                }
                else
                {
                    showProgressBar();
                    status_changing = true;
                    //update status
                    icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus());
                    Thread tStatusUpdater = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //Send signal to server
                            if(RemoteComms.sendMessage(getBaseContext(),icebreak_msg))//TODO: update by ID
                            {
                                LocalComms.updateMessageStatusById(getBaseContext(),icebreak_msg.getId(),icebreak_msg.getStatus());
                                Log.d(TAG,"Continue Button> Updated Icebreak request locally and remotely.");
                            }
                            hideProgressBar();
                            status_changing = false;
                            dialog.dismiss();
                        }
                    });
                    tStatusUpdater.start();
                }
            }
        });
    }

    private void drawRejectionUI()
    {
        dialog.setContentView(R.layout.popup_reject);
        //dialog.show();

        TextView txtMotivational = (TextView)dialog.findViewById(R.id.txt_motivational_message);
        ImageView imgLocalUser = (ImageView)dialog.findViewById(R.id.ib_res_local_usr_image);
        ImageView imgRemoteUser = (ImageView)dialog.findViewById(R.id.ib_res_remote_usr_image);
        TextView txtUnsuccess = (TextView)dialog.findViewById(R.id.ib_res_unsuccess);

        Button btnContinue = (Button)dialog.findViewById(R.id.ib_res_btn_continue);

        imgLocalUser.setImageBitmap(bitmapLocalUser);
        imgRemoteUser.setImageBitmap(bitmapRemoteUser);

        //Set typefaces
        txtMotivational.setTypeface(ttfInfinity);
        txtUnsuccess.setTypeface(ttfInfinity);

        btnContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG,"End Icebreak request.");
                showProgressBar();
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus());
                Thread tStatusUpdater = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(RemoteComms.sendMessage(getBaseContext(),icebreak_msg))//TODO: update by ID
                        {
                            LocalComms.updateMessageStatusById(getBaseContext(),icebreak_msg.getId(),icebreak_msg.getStatus());
                            //Don't add contact because local user was rejected.

                            Log.d(TAG,"Continue Button> Updated Icebreak request locally and remotely.");
                        }
                        hideProgressBar();
                    }
                });
                tStatusUpdater.start();
                dialog.dismiss();
            }
        });
    }

    public void closeActivity()
    {
        this.finish();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        active = false;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}