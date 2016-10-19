package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.services.IcebreakService;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by Casper on 2016/08/17.
 */
public class IBDialog extends Activity
{
    public static final int NULL = -1;
    public static final int INCOMING_REQUEST = 0;
    public static final int RESP_ACCEPTED = 1;
    public static final int RESP_REJECTED = 2;
    public static int request_code = NULL;

    private Dialog dialog;
    private ProgressDialog progress;
    private Bitmap bitmapReceivingUser,bitmapRequestingUser;

    private static final String TAG = "IB/IBDialog";
    private static Message icebreak_msg = null;
    private static User requesting_user = null;
    private static User receiving_user = null;


    private TextView txtIBReqPopup_name,txtIBReqPopup_age,txtIBReqPopup_gender,
            txtIBReqPopup_bioTitle,txtIBReqPopup_bio, txtIBReqPopup_occ;
    private ImageView imgIBReqPopup_OtherUser;
    private Button accept,reject;
    private RadioButton optionA,optionB,optionC;

    private Typeface ttfInfinity, ttfAilerons;

    private boolean active = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        Intent dlgIntent = getIntent();
        icebreak_msg = dlgIntent.getParcelableExtra("Message");
        receiving_user = dlgIntent.getParcelableExtra("Receiver");
        requesting_user = dlgIntent.getParcelableExtra("Sender");
        String s = dlgIntent.getStringExtra("Request_Code");

        //if(IcebreakService.active)
        //if(SharedPreference.isDialogActive(this))
        String stat = null;
        try
        {
            stat = WritersAndReaders.readAttributeFromConfig(Config.DLG_ACTIVE.getValue());
            if(stat!=null)
                active = stat.toLowerCase().equals("true");
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.d(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
        if(active)
        {
            Log.d(TAG, "Dialog is already showing!");
            this.finish();
            return;
        }

        if(icebreak_msg==null||receiving_user==null||requesting_user==null || s==null)
        {
            Log.d(TAG,"Some compulsory objects are null.");
            closeActivity();
        }

        if(s.toLowerCase().equals("null"))
            closeActivity();

        request_code = Integer.valueOf(s);

        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if(vibrator.hasVibrator())
            vibrator.vibrate(5000);

        if(request_code==INCOMING_REQUEST)//IceBreak request
        {
            if(icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus())//double check
            {
                try
                {
                    populateIcebreakRequestUI();
                    initIcebreakRequestHandlers();
                } catch (IOException e)
                {
                    if(e.getMessage()!=null)
                        Log.wtf(TAG,e.getMessage(),e);
                    else
                        e.printStackTrace();
                }
            }

        }else if(request_code==RESP_ACCEPTED||request_code==RESP_REJECTED)//IceBreak response
        {
            if(icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus())//double check
            {
                //Add user to local contacts table
                //LocalComms.addContact(getBaseContext(),receiving_user);
                drawAcceptanceUI();
            }
            else if(icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
            {
                drawRejectionUI();
            }
        }

        setDlgStatus(Config.DLG_ACTIVE_TRUE.getValue());

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialogInterface)
            {
                if(bitmapReceivingUser!=null)
                    bitmapReceivingUser.recycle();
                if(bitmapRequestingUser!=null)
                    bitmapRequestingUser.recycle();

                LocalComms.hideProgressBar(progress);
                IBDialog.request_code = NULL;
                closeActivity();//return focus to the MainActivity
            }
        });
    }

    private void setDlgStatus(String val)
    {
        try
        {
            WritersAndReaders.writeAttributeToConfig(Config.DLG_ACTIVE.getValue(),val);
        } catch (IOException e)
        {
            if(e.getMessage()!=null)
                Log.wtf(TAG,e.getMessage(),e);
            else
                e.printStackTrace();
        }
    }

    public void loadImages(final ImageView[] images)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;

        Thread tImageLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if(requesting_user!=null)
                {
                    try
                    {
                        //try to load local image for sender
                        if(bitmapRequestingUser!=null)
                            bitmapRequestingUser.recycle();
                        bitmapRequestingUser = LocalComms.getImage(IBDialog.this, requesting_user.getUsername(), ".png", "/profile", options);
                        //if (bitmapRequestingUser == null)//try get image from server if no local image
                        //    bitmapRequestingUser = RemoteComms.getImage(IBDialog.this, requesting_user.getUsername(), ".png", "/profile", options);
                    }
                    catch (IOException e)
                    {
                        if(e.getMessage()!=null)
                            Log.wtf(TAG,e.getMessage(),e);
                        else
                            e.printStackTrace();
                    }

                    //set image
                    Runnable r = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(images[0]!=null)
                                images[0].setImageBitmap(bitmapRequestingUser);
                        }
                    };
                    runOnUiThread(r);
                }else Log.d(TAG,"Requesting user is null");

                if(receiving_user!=null)
                {
                    if(bitmapReceivingUser!=null)
                        bitmapReceivingUser.recycle();
                    try
                    {
                        //try to load local image for receiver
                        bitmapReceivingUser = LocalComms.getImage(IBDialog.this, receiving_user.getUsername(), ".png", "/profile", options);
                        //if (bitmapReceivingUser == null)//try get image from server if no local image
                        //    bitmapReceivingUser = RemoteComms.getImage(IBDialog.this, receiving_user.getUsername(), ".png", "/profile", options);

                        //set image
                        Runnable r = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (images[1] != null)
                                    images[1].setImageBitmap(bitmapReceivingUser);
                            }
                        };
                        runOnUiThread(r);
                    }
                    catch (IOException e)
                    {
                        if(e.getMessage()!=null)
                            Log.wtf(TAG,e.getMessage(),e);
                        else
                            e.printStackTrace();
                    }
                }else Log.d(TAG,"Receiving user is null");
            }
        });
        tImageLoader.start();
    }

    private void populateIcebreakRequestUI() throws IOException
    {
        if(!active)
        {
            if(dialog==null)
                dialog = new Dialog(this);

            dialog.setContentView(R.layout.popup_icebreak);
            setDlgStatus(Config.DLG_ACTIVE_TRUE.getValue());
            dialog.show();
        }//else return;//else it's showing - do nothing to it.

        txtIBReqPopup_name = (TextView) dialog.findViewById(R.id.ib_req_username);
        txtIBReqPopup_age = (TextView) dialog.findViewById(R.id.ib_req_user_age);
        txtIBReqPopup_gender = (TextView) dialog.findViewById(R.id.ib_req_user_gender);
        txtIBReqPopup_bioTitle = (TextView) dialog.findViewById(R.id.ib_req_user_bio_title);
        txtIBReqPopup_bio = (TextView) dialog.findViewById(R.id.ib_req_user_bio);
        txtIBReqPopup_occ = (TextView) dialog.findViewById(R.id.ib_req_user_occupation);

        imgIBReqPopup_OtherUser = (ImageView) dialog.findViewById(R.id.ib_req_user_image);

        loadImages(new ImageView[]{imgIBReqPopup_OtherUser,null});

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

        /*if(imgIBReqPopup_OtherUser!=null && bitmapRequestingUser!=null)
            imgIBReqPopup_OtherUser.setImageBitmap(bitmapRequestingUser);*/

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
        if(accept==null||reject==null)
            return;

        accept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
                if(dialog!=null)
                    dialog.hide();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        drawAcceptanceUI();
                    }
                });
            }
        });

        reject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                progress = LocalComms.showProgressDialog(IBDialog.this,"Rejecting request...");
                IcebreakService.status_changing = true;
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus());
                Thread tStatusUpdater = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            //Send signal to server
                            if (RemoteComms.sendMessage(getBaseContext(), icebreak_msg))//TODO: update by ID
                            {
                                Log.d(TAG, "Reject Button> Updated IceBreak request remotely.");
                                LocalComms.updateMessageStatusById(getBaseContext(), icebreak_msg.getId(), icebreak_msg.getStatus());
                                Log.d(TAG, "Reject Button> Updated IceBreak request locally.");
                            }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    LocalComms.hideProgressBar(progress);
                                    dialog.dismiss();
                                }
                            });
                            IcebreakService.status_changing = false;
                        }
                        catch (SocketTimeoutException e)
                        {
                            Log.d(TAG,"Connection Timeout.",e);
                            Toast.makeText(IBDialog.this,"Connection Time Out.",Toast.LENGTH_LONG).show();
                        }
                        catch (IOException e)
                        {
                            Log.d(TAG,"IOException: " + e.getMessage(),e);
                            Toast.makeText(IBDialog.this,"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
                tStatusUpdater.start();
            }
        });
    }

    private void drawAcceptanceUI()
    {
        if(!active)
        {
            if(dialog==null)
                dialog = new Dialog(this);
            if(request_code==RESP_ACCEPTED)
            {
                dialog.setContentView(R.layout.popup_accepted2);
                dialog.show();
                setDlgStatus(Config.DLG_ACTIVE_TRUE.getValue());

                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                Display dis = wm.getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                dis.getMetrics(metrics);
                dialog.getWindow().setLayout((int) (metrics.widthPixels * 0.90), metrics.widthPixels);
            }
            else if(request_code==INCOMING_REQUEST)
            {
                dialog.setContentView(R.layout.popup_accepted);
                dialog.show();
                setDlgStatus(Config.DLG_ACTIVE_TRUE.getValue());

            }else dialog.dismiss();
        }//else return;//else it's showing - do nothing to it.

        TextView txtSuccessfulMatch = (TextView)dialog.findViewById(R.id.SuccessfulMatch);
        ImageView imgLocalUser = (ImageView)dialog.findViewById(R.id.other_pic1);
        ImageView imgRemoteUser = (ImageView)dialog.findViewById(R.id.other_pic2);
        TextView phrase = (TextView)dialog.findViewById(R.id.phrase);

        if(request_code==INCOMING_REQUEST)
        {
            optionA = (RadioButton) dialog.findViewById(R.id.popup_accepted_radbtn1);
            optionB = (RadioButton) dialog.findViewById(R.id.popup_accepted_radbtn2);
            optionC = (RadioButton) dialog.findViewById(R.id.popup_accepted_radbtn3);
            TextView or = (TextView) dialog.findViewById(R.id.or);
            TextView or2 = (TextView) dialog.findViewById(R.id.or2);
            or.setTypeface(ttfInfinity);
            or2.setTypeface(ttfInfinity);
        }

        Button btnContinue = (Button)dialog.findViewById(R.id.popup1_Keep_playing);

        loadImages(new ImageView[]{imgLocalUser,imgRemoteUser});
        //imgLocalUser.setImageBitmap(bitmapReceivingUser);
        //imgRemoteUser.setImageBitmap(bitmapRequestingUser);

        txtSuccessfulMatch.setTypeface(ttfInfinity);
        phrase.setTypeface(ttfInfinity);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    long ev_id = 0;
                    String temp = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                    if (temp != null)
                        if (!temp.isEmpty() && !temp.equals("null"))
                            ev_id = Long.parseLong(temp);
                    Event event = null;
                    if (ev_id > 0)
                        event = RemoteComms.getEvent(ev_id);
                    if (event != null)
                    {
                        final String[] places = event.getMeetingPlaces();
                        if (places.length > 2)
                        {
                            IBDialog.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    optionA.setText(places[0]);
                                    optionB.setText(places[1]);
                                    optionC.setText(places[2]);
                                }
                            });
                        }
                    }
                }
                catch (IOException e)
                {
                    if(e.getMessage()!=null)
                        Log.d(TAG,e.getMessage());
                    else
                        e.printStackTrace();
                }
            }
        });
        if(request_code==INCOMING_REQUEST)
        {
            t.start();

            optionA.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    optionA.setChecked(true);
                    optionB.setChecked(false);
                    optionC.setChecked(false);
                }
            });
            optionB.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    optionA.setChecked(false);
                    optionB.setChecked(true);
                    optionC.setChecked(false);
                }
            });
            optionC.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    optionA.setChecked(false);
                    optionB.setChecked(false);
                    optionC.setChecked(true);
                }
            });
        }
        else
        {
            if(receiving_user!=null && icebreak_msg!=null)
            {
                String s = "Congrats, " + LocalComms.getValidatedName(receiving_user) + " would like to meet you at "
                        + icebreak_msg.getMessage();
                phrase.setText(s);
            }
        }

        btnContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG,"btnContinue> End IceBreak request.");
                if(request_code==INCOMING_REQUEST)//request to local user
                {
                    if(!optionA.isChecked() && !optionB.isChecked() && !optionC.isChecked())
                    {
                        Log.d(TAG, "No meeting place chosen.");
                        Toast.makeText(IBDialog.this,"You must choose one meeting place.",Toast.LENGTH_LONG).show();
                        return;
                    }

                    progress = LocalComms.showProgressDialog(IBDialog.this, "Accepting request...");
                    IcebreakService.status_changing = true;
                    if(icebreak_msg==null)
                    {
                        Log.d(TAG, "An unexpected error has occurred> icebreak_msg=null");
                        Toast.makeText(IBDialog.this,"An unexpected error has occurred.",Toast.LENGTH_LONG).show();
                        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
                        dialog.dismiss();
                    }

                    icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus());
                    Thread tStatusUpdater = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                String place = "ICEBREAK";
                                if(optionA.isChecked())
                                    place=optionA.getText().toString();
                                if(optionB.isChecked())
                                    place=optionB.getText().toString();
                                if(optionC.isChecked())
                                    place=optionC.getText().toString();
                                icebreak_msg.setMessage(place);
                                //Update remote DB
                                if (RemoteComms.sendMessage(getBaseContext(), icebreak_msg))//TODO: update by ID
                                {
                                    Log.d(TAG, "Continue Button> Updated IceBreak request remotely[accepted]");
                                    //Update local DB
                                    LocalComms.updateMessageStatusById(getBaseContext(), icebreak_msg.getId(), icebreak_msg.getStatus());
                                    Log.d(TAG, "Continue Button> Updated IceBreak request locally[rejected]");
                                    //Add user to local contacts table
                                    try
                                    {
                                        if (requesting_user == null)
                                            requesting_user = RemoteComms.getUser(IBDialog.this, icebreak_msg.getSender());

                                        //Save new contact to local storage
                                        if (requesting_user != null)
                                            LocalComms.addContact(getBaseContext(), requesting_user);
                                        //TODO: Add to networks remote table
                                    } catch (IOException e)
                                    {
                                        if(e.getMessage()!=null)
                                            Log.wtf(TAG, "Couldn't get requesting user: " + e.getMessage(), e);
                                        else
                                            e.printStackTrace();
                                    }

                                    IcebreakService.status_changing = false;
                                }
                            }
                            catch (SocketTimeoutException e)
                            {
                                Log.d(TAG,"Connection Timeout.",e);
                                Toast.makeText(IBDialog.this,"Connection Time Out.",Toast.LENGTH_LONG).show();
                            }
                            catch (IOException e)
                            {
                                Log.d(TAG,"IOException: " + e.getMessage(),e);
                                Toast.makeText(IBDialog.this,"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
                                    LocalComms.hideProgressBar(progress);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    tStatusUpdater.start();
                }
                else if(request_code==RESP_ACCEPTED)//initiator has been accepted
                {
                    if(progress==null)
                        progress = LocalComms.showProgressDialog(IBDialog.this,"Loading...");
                    IcebreakService.status_changing = true;
                    //update status
                    icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus());
                    Thread tStatusUpdater = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                //Send signal to server
                                if (RemoteComms.sendMessage(getBaseContext(), icebreak_msg))//TODO: update by ID
                                {
                                    Log.d(TAG, "Continue Button> Updated IceBreak request remotely.");
                                    LocalComms.updateMessageStatusById(getBaseContext(), icebreak_msg.getId(), icebreak_msg.getStatus());
                                    Log.d(TAG, "Continue Button> Updated IceBreak request locally.");
                                }
                            }
                            catch (SocketTimeoutException e)
                            {
                                Log.d(TAG,"Connection Timeout.",e);
                                Toast.makeText(IBDialog.this,"Connection Time Out.",Toast.LENGTH_LONG).show();
                            }
                            catch (IOException e)
                            {
                                Log.d(TAG,"IOException: " + e.getMessage(),e);
                                Toast.makeText(IBDialog.this,"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                            IcebreakService.status_changing = false;
                            setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    LocalComms.hideProgressBar(progress);
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                    tStatusUpdater.start();
                }
            }
        });
    }

    private void drawRejectionUI()
    {
        if(!active)
        {
            if(dialog==null)
                dialog = new Dialog(this);

            dialog.setContentView(R.layout.popup_rejected);
            dialog.show();
            setDlgStatus(Config.DLG_ACTIVE_TRUE.getValue());
        }//else return;//else it's showing - do nothing to it.

        TextView txtUnsuccess = (TextView)dialog.findViewById(R.id.ib_res_unsuccess);
        TextView txtMotivational = (TextView)dialog.findViewById(R.id.txt_motivational_message);
        ImageView imgLocalUser = (ImageView)dialog.findViewById(R.id.ib_res_local_usr_image);
        ImageView imgRemoteUser = (ImageView)dialog.findViewById(R.id.ib_res_remote_usr_image);

        //Set typefaces
        txtMotivational.setTypeface(ttfInfinity);
        txtUnsuccess.setTypeface(ttfInfinity);

        Button btnContinue = (Button)dialog.findViewById(R.id.ib_res_btn_continue);

        /*if(imgLocalUser!=null && bitmapRequestingUser!=null)
            imgLocalUser.setImageBitmap(bitmapRequestingUser);
        else Log.d(TAG,"Requesting user bitmap NULL.");

        if(imgRemoteUser!=null && bitmapReceivingUser!=null)
            imgRemoteUser.setImageBitmap(bitmapReceivingUser);
        else Log.d(TAG,"Receiving user bitmap NULL.");*/
        loadImages(new ImageView[]{imgLocalUser,imgRemoteUser});

        btnContinue.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG,"End Icebreak request.");
                progress = LocalComms.showProgressDialog(IBDialog.this, "Loading...");
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_DONE.getStatus());
                Thread tStatusUpdater = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (RemoteComms.sendMessage(getBaseContext(), icebreak_msg))//TODO: update by ID
                            {
                                Log.d(TAG, "Continue Button> Updated IceBreak request remotely.");
                                LocalComms.updateMessageStatusById(getBaseContext(), icebreak_msg.getId(), icebreak_msg.getStatus());
                                //Don't add contact because local user was rejected.
                                Log.d(TAG, "Continue Button> Updated IceBreak request locally.");
                            }

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    dialog.dismiss();
                                }
                            });
                        }
                        catch (SocketTimeoutException e)
                        {
                            Toast.makeText(IBDialog.this,"Connection Time Out.",Toast.LENGTH_LONG).show();
                            Log.wtf(TAG, "Timeout",e);
                            //TODO: Better logging
                        }
                        catch (IOException e)
                        {
                            Toast.makeText(IBDialog.this,"Error: " + e.getMessage(),Toast.LENGTH_LONG).show();
                            Log.wtf(TAG, e.getMessage(),e);
                            //TODO: Better logging
                        }
                    }
                });
                tStatusUpdater.start();
            }
        });
    }

    public void closeActivity()
    {
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
        this.finish();
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //setDlgStatus(Config.DLG_ACTIVE_TRUE.getValue());
    }

    @Override
    public void onStop()
    {
        super.onStop();
        //setDlgStatus(Config.DLG_ACTIVE_FALSE.getValue());
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}