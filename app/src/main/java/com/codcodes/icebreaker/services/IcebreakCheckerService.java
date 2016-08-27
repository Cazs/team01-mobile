package com.codcodes.icebreaker.services;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.INTERVALS;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.model.UserContract;
import com.codcodes.icebreaker.model.UserHelper;
import com.codcodes.icebreaker.screens.IBDialog;
import com.codcodes.icebreaker.screens.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Casper on 2016/08/16.
 */
public class IcebreakCheckerService extends IntentService
{
    //private Context context = null;
    private static Message icebreak_msg = new Message();
    private static User requesting_user = new User();
    private static User receiving_user = new User();

    private final String TAG = "IB/ListenerService";
    //private Handler mHandler;

    public IcebreakCheckerService()
    {
        super("IcebreakCheckerService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //mHandler = new Handler();//Bind to main/UI thread
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if(intent!=null)// && mHandler!=null
        {
            try
            {
                //Check for Icebreaks indefinitely
                while (true)
                {
                    Log.d(TAG,"Checking for local inbound and outbound Icebreaks.");
                    ArrayList<Message> messages = LocalComms.getInboundMessages(this,
                            SharedPreference.getUsername(this).toString());

                    ArrayList<Message> out_messages = LocalComms.getOutboundMessages(this,
                            SharedPreference.getUsername(this).toString());

                    //If there are Icebreaks
                    if (messages.size() > 0)
                    {
                        Log.d(TAG, "Found Icebreak/s.");

                        //Get first Icebreak
                        icebreak_msg = messages.get(0);

                        System.err.println(messages.size());

                        receiving_user = LocalComms.getContact(this,icebreak_msg.getReceiver());
                        if (receiving_user == null)//attempt to download user details
                            receiving_user = RemoteComms.getUser(this,icebreak_msg.getReceiver());

                        requesting_user = LocalComms.getContact(this,icebreak_msg.getSender());
                        if (requesting_user == null)//attempt to download user details
                            requesting_user = RemoteComms.getUser(this,icebreak_msg.getSender());

                        Log.d(TAG+"/IBC", "IBDialog active: " + IBDialog.active);

                        //always wait for pending message status changes to complete
                        if (!IBDialog.active && !IBDialog.status_changing)
                        {
                            //Show Icebreak Dialog
                            Intent dlgIntent = new Intent(getApplicationContext(), IBDialog.class);
                            dlgIntent.putExtra("Message", icebreak_msg);
                            dlgIntent.putExtra("Receiver", receiving_user);
                            dlgIntent.putExtra("Sender", requesting_user);
                            IBDialog.requesting = true;

                            dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(dlgIntent);
                        }
                    }
                    else Log.d(TAG,"<No local inbound Icebreaks>");

                    //Check for cases where local user is sender
                    if(out_messages.size()>0)
                    {
                        for(Message m: out_messages)
                        {
                            //TODO: send messages to server if they haven't been sent
                            Log.d(TAG+"/OBC>", "IBDialog active: " + IBDialog.active);
                            //always wait for pending message status changes to complete
                            if (!IBDialog.active && !IBDialog.status_changing)
                            {
                                //If local user has been accepted or rejected, show appropriate dialog
                                if (m.getStatus() == MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus() ||
                                        m.getStatus() == MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus())
                                {
                                    receiving_user = LocalComms.getContact(this,m.getReceiver());
                                    if (receiving_user == null)//attempt to download user details
                                        receiving_user = RemoteComms.getUser(this,m.getReceiver());

                                    requesting_user = LocalComms.getContact(this,m.getSender());
                                    if (requesting_user == null)//attempt to download user details
                                        requesting_user = RemoteComms.getUser(this,m.getSender());
                                    //Show dialog
                                    Intent dlgIntent = new Intent(getApplicationContext(), IBDialog.class);
                                    dlgIntent.putExtra("Message", m);
                                    dlgIntent.putExtra("Receiver", receiving_user);
                                    dlgIntent.putExtra("Sender", requesting_user);
                                    IBDialog.requesting = false;

                                    dlgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(dlgIntent);
                                }
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG,"<No local outbound Icebreaks>");
                    }
                        /*mHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {

                            }
                        });*/
                    //sleep a bit
                    Thread.sleep(INTERVALS.IB_CHECK_DELAY.getValue());
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
            }
        }
        else
            Log.d(TAG,"intent==null:" + (intent==null));//"handler==null:" + (mHandler==null) +
    }


    /*private void accept() throws IOException
    {
        //Do UI things
        hideDialog();
        dlg_visible = false;
        drawPostAcceptanceUI();

        dialog.show();
        dlg_visible =true;

        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_ACCEPTED.getStatus());
                if (RemoteComms.sendMessage(context, icebreak_msg))
                {

                    Log.d(TAG, "Updated remote status");
                }
                else
                    Log.d(TAG, "Could not send delivery status to server.");
            }
        });
        t.start();
    }

    private void reject() throws IOException
    {
        //Send signal to sender to update delivery status
        Thread tReject = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                icebreak_msg.setStatus(MESSAGE_STATUSES.ICEBREAK_REJECTED.getStatus());
                if(RemoteComms.sendMessage(getBaseContext(),icebreak_msg))
                {
                    MessagePollHelper dbHelper = new MessagePollHelper(getBaseContext());//getBaseContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    ContentValues kv_pairs = new ContentValues();
                    //kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_ID, icebreak_msg.getId());
                    kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER, icebreak_msg.getSender());
                    kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER, icebreak_msg.getReceiver());
                    kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS, icebreak_msg.getStatus());
                    //kv_pairs.put(MessagePollContract.MessageEntry.COL_MESSAGE_TIME, icebreak_msg.getTime());

                    String where = MessagePollContract.MessageEntry.COL_MESSAGE_ID +
                            " = ?";
                    String[] where_args = {icebreak_msg.getId()};
                    db.update(MessagePollContract.MessageEntry.TABLE_NAME, kv_pairs,where,where_args);
                    db.close();
                    Log.d(TAG, "Successfully updated message status on remote and local DB");
                }
                else
                    Log.d(TAG, "Could NOT Successfully updated message status on server.");

                hideDialog();
            }
        });
        tReject.start();
    }

    private void drawPostAcceptanceUI()
    {
        dialog.setContentView(R.layout.popup_accepted);

        TextView txtSuccessfulMatch = (TextView)dialog.findViewById(R.id.SuccessfulMatch);
        ImageView imgLocalUser = (ImageView)dialog.findViewById(R.id.other_pic1);
        ImageView imgRemoteUser = (ImageView)dialog.findViewById(R.id.other_pic2);
        TextView phrase = (TextView)dialog.findViewById(R.id.phrase);
        Button btnChat = (Button)dialog.findViewById(R.id.popup1_Start_Chatting);
        TextView or = (TextView)dialog.findViewById(R.id.or);
        Button btnPlay = (Button)dialog.findViewById(R.id.popup1_Keep_playing);

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
                //Add user to local contacts table
                /*UserHelper dbHelper = new UserHelper(getBaseContext());//getBaseContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                dbHelper.onCreate(db);

                ContentValues kv_pairs = new ContentValues();

                kv_pairs.put(UserContract.UserEntry.COL_USER_USERNAME, requesting_user.getUsername());
                kv_pairs.put(UserContract.UserEntry.COL_USER_FNAME, otherUser.getFirstname());
                kv_pairs.put(UserContract.UserEntry.COL_USER_LNAME, otherUser.getLastname());
                kv_pairs.put(UserContract.UserEntry.COL_USER_AGE, otherUser.getAge());
                kv_pairs.put(UserContract.UserEntry.COL_USER_BIO, otherUser.getBio());
                kv_pairs.put(UserContract.UserEntry.COL_USER_CATCHPHRASE, otherUser.getCatchphrase());
                kv_pairs.put(UserContract.UserEntry.COL_USER_OCCUPATION, otherUser.getOccupation());
                kv_pairs.put(UserContract.UserEntry.COL_USER_GENDER, otherUser.getGender());

                long newRowId = -1;
                if(!userExistsInDB(getBaseContext(),strUserRemote))
                    newRowId = db.insert(UserContract.UserEntry.TABLE_NAME, null, kv_pairs);
                else
                    Log.d(TAG,"User exists in local DB");
                System.err.println("New contact ==> "+ newRowId);
                /*Toast.makeText(getBaseContext(),"Could not add user to contacts list: " + e.getMessage(),Toast.LENGTH_LONG).show();
                e.printStackTrace();
                Log.d(TAG,e.getMessage());*
                //Start ChatActivity
                hideDialog();
                /*Intent chatIntent = new Intent(getBaseContext(),ChatActivity.class);
                chatIntent.putExtra("Username",strUserRemote);
                startActivity(chatIntent);*
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                hideDialog();
            }
        });
    }

    public void checkForIcebreaks() throws IOException
    {
        //cview_set = false;
        if(dialog==null)
        {
            System.err.println("Dialog is currently NULL.");
            return;
        }
        if(dlg_visible)
        {
            Log.d(TAG,"Dialog is currently displayed.");
            return;
        }

        //TODO: Take a closer look at the SERV_RECEIVED part
        if(icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus() || icebreak_msg.getStatus()==MESSAGE_STATUSES.ICEBREAK_SERV_RECEIVED.getStatus())
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setContentView(R.layout.pop_up_one);
                    cview_set = true;
                }
            });

            //Wait for content view to be set
            while (!cview_set) {}//Uuurgh

            //TODO: make vars global
            final TextView txtPopupname = (TextView) dialog.findViewById(R.id.popup1_profile_name);

            final TextView txtPopupage = (TextView) dialog.findViewById((R.id.popup1_profile_age));

            final TextView txtPopupgender = (TextView) dialog.findViewById((R.id.popup1_profile_gender));

            final TextView txtPopupbioTitle = (TextView) dialog.findViewById((R.id.popup1_profile_bio_title));

            final TextView txtPopupbio = (TextView) dialog.findViewById((R.id.popup1_profile_bio));

            final ImageView imgOtherUser = (ImageView) dialog.findViewById(R.id.other_pic);

            final Button accept = (Button) dialog.findViewById(R.id.popup1_Accept);
            final Button reject = (Button) dialog.findViewById(R.id.popup1_Reject);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtPopupname.setTypeface(ttfInfinity);
                    txtPopupage.setTypeface(ttfInfinity);
                    txtPopupgender.setTypeface(ttfInfinity);
                    txtPopupbioTitle.setText("Bio:");
                    txtPopupbioTitle.setTypeface(ttfInfinity);
                    txtPopupbio.setTypeface(ttfInfinity);
                    imgOtherUser.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/Icebreak/profile/default.png"));
                    reject.setTypeface(ttfAilerons);
                    accept.setTypeface(ttfAilerons);

                    if (icebreak_msg != null && requesting_user != null)
                    {
                        if (txtPopupbio != null)
                            txtPopupbio.setText(requesting_user.getBio());
                        if (txtPopupgender != null)
                            txtPopupgender.setText(requesting_user.getGender());
                        if (txtPopupage != null)
                            txtPopupage.setText(String.valueOf(requesting_user.getAge()));
                        if (txtPopupname != null) {
                            if (requesting_user.getFirstname() != null && requesting_user.getLastname() != null) {
                                if (requesting_user.getFirstname().toLowerCase().equals('x') || requesting_user.getLastname().toLowerCase().equals('x'))
                                    txtPopupname.setText("Anonymous");
                                else
                                    txtPopupname.setText(requesting_user.getFirstname() + " " + requesting_user.getLastname());
                            } else txtPopupname.setText("Anonymous");
                        }
                        if (imgOtherUser != null && bitmapRemoteUser != null)
                            imgOtherUser.setImageBitmap(bitmapRemoteUser);

                        dialog.show();//Show dialog
                        dlg_visible = true;
                    } else Log.d(TAG, "NULL remote user or icebreak_msg");
                }
            });

            while(accept==null){}//Aarg
            accept.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try {
                        accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            while(reject==null){}//Aarg
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        reject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else
            Log.d(TAG,"Message status: " + icebreak_msg.getStatus());
    }*/
}
