package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ContactListSwitches;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.MESSAGE_STATUSES;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.ContactsContract;
import com.codcodes.icebreaker.model.ContactsHelper;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.Message;
import com.codcodes.icebreaker.model.MessagePollContract;
import com.codcodes.icebreaker.model.MessagePollHelper;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.model.UserContract;
import com.codcodes.icebreaker.model.UserHelper;
import com.codcodes.icebreaker.services.MessagePollService;
import com.codcodes.icebreaker.tabs.EventsFragment;
import com.codcodes.icebreaker.tabs.ProfileFragment;
import com.codcodes.icebreaker.tabs.UserContactsFragment;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Timer;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements IOnListFragmentInteractionListener
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private Button accept, reject;
    private TextView txtPopupbio, txtPopupbioTitle, txtPopupgender, txtPopupage, txtPopupname;
    private LinearLayout actionBar;
    private ViewPager mViewPager;
    private Typeface ttfInfinity, ttfAilerons;
    private Dialog dialog;

    public static String rootDir = Environment.getExternalStorageDirectory().getPath();
    public static boolean IB_DIALOG = false;
    public static boolean appInFG = false;
    public static final  int IB_CHECK_DELAY = 5000;//5 sec
    public static ContactListSwitches val_switch = ContactListSwitches.SHOW_USERS_AT_EVENT;

    private static final Message icebreak_msg = new Message();
    private static User requesting_user = new User();
    private static final User receiving_user = new User();
    private static final String TAG = "IB/MainActivity";
    private static boolean cview_set = false;
    private static boolean dlg_visible = false;

    private Bitmap bitmapLocalUser,bitmapRemoteUser;
    private Activity context;
    private boolean remote_user_set = false;

    private int[] imageResId =
            {
                    R.drawable.ic_location_on_white_24dp,
                    R.drawable.ic_chat_bubble_white_24dp,
                    R.drawable.ic_person_white_24dp
            };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        appInFG = true;

        ttfInfinity = Typeface.createFromAsset(getAssets(), "Infinity.ttf");
        ttfAilerons = Typeface.createFromAsset(getAssets(), "Ailerons-Typeface.otf");

        dialog = new Dialog(this);
        Restful.validateStoragePermissions(this);

        Thread tDataloader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //Get local/receiving user
                    setLocalUser(SharedPreference.getUsername(context).toString());
                    //checkForIcebreaks();

                    /*
                     * Keep updating the icebreak_msg so the checkForIcebreaks() method can
                     * Update the UI based on that
                     */
                    while(appInFG)
                    {
                        MessagePollHelper dbHelper = new MessagePollHelper(getBaseContext());
                        SQLiteDatabase db = dbHelper.getReadableDatabase();
                        dbHelper.onCreate(db);

                        String localUser = SharedPreference.getUsername(getBaseContext());
                        String query = "SELECT * FROM " + MessagePollContract.MessageEntry.TABLE_NAME + " WHERE "
                                + MessagePollContract.MessageEntry.COL_MESSAGE + " = ? AND "
                                + MessagePollContract.MessageEntry.COL_MESSAGE_STATUS + " = ? AND "
                                + MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER + " = ?";

                        Cursor c = db.rawQuery(query, new String[]{"ICEBREAK",
                                String.valueOf(MESSAGE_STATUSES.ICEBREAK_DELIVERED.getStatus()), localUser});

                        if (c.getCount() > 0)//If there are Icebreak requests
                        {
                            c.moveToFirst();
                            String msg_id = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_ID));
                            String send = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_SENDER));
                            String msg = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE));
                            String recv = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_RECEIVER));
                            String time = c.getString(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_TIME));
                            int status = c.getInt(c.getColumnIndex(MessagePollContract.MessageEntry.COL_MESSAGE_STATUS));

                            icebreak_msg.setId(msg_id);
                            icebreak_msg.setSender(send);
                            icebreak_msg.setMessage(msg);
                            icebreak_msg.setReceiver(recv);
                            icebreak_msg.setTime(time);
                            icebreak_msg.setStatus(status);

                            db.close();//Close DB

                            //Get remote/requesting user
                            if(requesting_user==null)
                            {
                                requesting_user = Restful.getUser(send);
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                bitmapRemoteUser = Restful.getImage(context, requesting_user.getUsername(), ".png", "/profile", options);
                                Log.d(TAG,"Requesting user was null, I fixed this.");
                            }
                            else
                            {
                                boolean need_to_sync = false;
                                if(requesting_user.getUsername()==null)
                                    need_to_sync = true;
                                else
                                    if (!requesting_user.getUsername().equals(send))//Update Images and requesting_user only when necessary
                                        need_to_sync = true;
                                if(need_to_sync)
                                {
                                    requesting_user = Restful.getUser(send);
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                    bitmapRemoteUser = Restful.getImage(context, requesting_user.getUsername(), ".png", "/profile", options);
                                    Log.d(TAG, "Requesting user's username did not match the one we had, I fixed this.");
                                }
                                else
                                    Log.d(TAG, "Remote user is up to date.");
                            }
                        }
                        Thread.sleep(IB_CHECK_DELAY);
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });
        tDataloader.start();

        //Start Icebreak searching service
        Thread tIBloader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                while (appInFG) {
                    try {
                        checkForIcebreaks();
                        Thread.sleep(IB_CHECK_DELAY);
                    } catch (IOException e) {//TODO: Fix error handling.
                        e.printStackTrace();
                    }catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
        tIBloader.start();

        //Start message polling service
        Intent inMsg = new Intent(this, MessagePollService.class);
        inMsg.putExtra("Username", SharedPreference.getUsername(this));
        this.startService(inMsg);
        //Load UI components
        actionBar = (LinearLayout)findViewById(R.id.actionBar);
        mViewPager = (ViewPager) findViewById(R.id.container);
        TabLayout tablayout = (TabLayout) findViewById(R.id.tab_layout);
        TextView headingTextView = (TextView) findViewById(R.id.main_heading);
        ttfInfinity = Typeface.createFromAsset(this.getAssets(),"Ailerons-Typeface.otf");
        final FloatingActionButton fabSwitch = (FloatingActionButton)findViewById(R.id.fabSwitch);

        //Setup UI components
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager(),MainActivity.this));
        tablayout.setupWithViewPager(mViewPager);// Set up the ViewPager with the sections adapter.
        tablayout.getTabAt(0).setIcon(imageResId[0]);
        tablayout.getTabAt(1).setIcon(imageResId[1]);
        tablayout.getTabAt(2).setIcon(imageResId[2]);
        headingTextView.setTypeface(ttfInfinity);
        fabSwitch.hide();

        fabSwitch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(val_switch==ContactListSwitches.SHOW_USER_CONTACTS)
                    val_switch=ContactListSwitches.SHOW_USERS_AT_EVENT;
                else
                    val_switch=ContactListSwitches.SHOW_USER_CONTACTS;
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if(position == 1)
                {
                    fabSwitch.show();
                }else
                {
                    fabSwitch.hide();
                }
            }

            @Override
            public void onPageSelected(int position)
            {

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }

    private void setLocalUser(String username) throws IOException
    {
        UserHelper u_dbHelper = new UserHelper(getBaseContext());
        SQLiteDatabase db;
        String query;
        String[] args;
        if(!username.isEmpty()) {
             db = u_dbHelper.getReadableDatabase();
            u_dbHelper.onCreate(db);
            query = "SELECT * FROM " + UserContract.UserEntry.TABLE_NAME +
                    " WHERE " + UserContract.UserEntry.COL_USER_USERNAME + " = ?";
            args = new String[]{username};//which will be local user
            Cursor c = db.rawQuery(query, args);
            if (c.getCount() > 0) {
                c.moveToFirst();
                String fname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_FNAME));
                String lname = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_LNAME));
                int age = c.getInt(c.getColumnIndex(UserContract.UserEntry.COL_USER_AGE));
                String bio = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_BIO));
                String ctch = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_CATCHPHRASE));
                String occp = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_OCCUPATION));
                String gndr = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_GENDER));
                String usr = c.getString(c.getColumnIndex(UserContract.UserEntry.COL_USER_USERNAME));
                receiving_user.setFirstname(fname);
                receiving_user.setLastname(lname);
                receiving_user.setAge(age);
                receiving_user.setBio(bio);
                receiving_user.setCatchphrase(ctch);
                receiving_user.setOccupation(occp);
                receiving_user.setGender(gndr);
                receiving_user.setUsername(usr);
                Log.d(TAG,"Set global local user object.");
                db.close();
            } else
            {
                //Insert user data from server
                db = u_dbHelper.getWritableDatabase();
                u_dbHelper.onCreate(db);
                User local = Restful.getUser(username);

                ContentValues kv_pairs = new ContentValues();
                kv_pairs.put(UserContract.UserEntry.COL_USER_USERNAME, local.getUsername());
                kv_pairs.put(UserContract.UserEntry.COL_USER_AGE, local.getAge());
                kv_pairs.put(UserContract.UserEntry.COL_USER_BIO, local.getBio());
                kv_pairs.put(UserContract.UserEntry.COL_USER_CATCHPHRASE, local.getCatchphrase());
                kv_pairs.put(UserContract.UserEntry.COL_USER_FNAME, local.getFirstname());
                kv_pairs.put(UserContract.UserEntry.COL_USER_GENDER, local.getGender());
                kv_pairs.put(UserContract.UserEntry.COL_USER_OCCUPATION, local.getOccupation());
                kv_pairs.put(UserContract.UserEntry.COL_USER_LNAME, local.getLastname());

                long newRowId = db.insert(MessagePollContract.MessageEntry.TABLE_NAME, null, kv_pairs);
                Log.d(TAG, "Inserted into Users table: new row=" + newRowId);
                db.close();
            }
            //Load local profile bitmap
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            bitmapLocalUser = Restful.getImage(context, username, ".png", "/profile", options);

        }
        else Log.d(TAG,"Empty username");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                if(Restful.sendMessage(getBaseContext(),icebreak_msg))
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
        dialog.setContentView(R.layout.pop_up_two);

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
                Log.d(TAG,e.getMessage());*/
                //Start ChatActivity
                hideDialog();
                /*Intent chatIntent = new Intent(getBaseContext(),ChatActivity.class);
                chatIntent.putExtra("Username",strUserRemote);
                startActivity(chatIntent);*/
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

    public void hideDialog()
    {
        if(dialog!=null)
            //if(dialog.isShowing()) {
                //dialog.dismiss();
                //dialog.cancel();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        });
            //}
        //dialog = new Dialog(this);
        cview_set = false;
        dlg_visible = false;
    }

    private void accept() throws IOException
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
                if (Restful.sendMessage(context, icebreak_msg))
                {

                    Log.d(TAG, "Updated remote status");
                }
                else
                    Log.d(TAG, "Could not send delivery status to server.");
            }
        });
        t.start();
    }

    private boolean userExistsInDB(Context ctxt, String username)
    {
        String query ="SELECT * FROM " + UserContract.UserEntry.TABLE_NAME + " WHERE "
                + UserContract.UserEntry.COL_USER_USERNAME +" = ?";

        UserHelper dbHelper = new UserHelper(ctxt);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        dbHelper.onCreate(db);

        Cursor c =  db.rawQuery(query, new String[] {username});
        int rowCount=c.getCount();
        db.close();
        if(rowCount>0)
            return true;
        else
            return  false;
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
    }

    @Override
    public void onListFragmentInteraction(User item)
    {

        /*Dialog userProfileScreen = new Dialog(this);
        userProfileScreen.setContentView(R.layout.content_other_user_profile);

        TextView username = (TextView)userProfileScreen.findViewById(R.id.other_user_name);
        ImageView profile_image = (ImageView)userProfileScreen.findViewById(R.id.other_user_profile_image);
        TextView level = (TextView)userProfileScreen.findViewById(R.id.other_user_level);
        TextView age = (TextView)userProfileScreen.findViewById(R.id.other_user_age);
        TextView gender = (TextView)userProfileScreen.findViewById(R.id.other_user_gender);
        TextView occupation = (TextView)userProfileScreen.findViewById(R.id.other_user_occupation);
        TextView phrase = (TextView)userProfileScreen.findViewById(R.id.other_user_phrase);
        TextView bio = (TextView)userProfileScreen.findViewById(R.id.other_user_bio);

        username.setText(item.getUsername());
        /*bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/profile_default.png",getActivity());*
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        if(new File(Environment.getExternalStorageDirectory().getPath().toString()
                + "/Icebreak/profile/"+item.getUsername()+".png").exists())
        {
            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                    + "/Icebreak/profile/"+item.getUsername()+".png", options);
        }
        else
        {
            if(new File(Environment.getExternalStorageDirectory().getPath().toString()
                    + "/Icebreak/profile/profile_default.png").exists())
            {
                bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                        + "/Icebreak/profile/profile_default.png", options);
            }
        }
        if(bitmap!=null)
        {
            Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
            profile_image.setImageBitmap(circularbitmap);
        }
        else
        {
            Toast.makeText(this,"Could not get profile for selected user, nor could we find the default image.",Toast.LENGTH_LONG).show();
        }

        //level.setText(item.getLevel());
        //age.setText(item.getAge());
        gender.setText(item.getGender());
        occupation.setText(item.getOccupation());
        phrase.setText(item.getCatchphrase());
        bio.setText(item.getBio());

        userProfileScreen.show();
        */
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra("Username",item.getUsername());
        startActivity(intent);
    }

    public class FragmentAdapter extends FragmentPagerAdapter
    {
        final int PAGE_COUNT = 3;
        private Context context;

        public FragmentAdapter(FragmentManager fm,Context context)
        {
            super(fm);
            this.context=context;
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    //actionBar.setVisibility(View.VISIBLE);
                    return EventsFragment.newInstance(context,getIntent().getExtras());
                case 1:
                    //actionBar.setVisibility(View.INVISIBLE);
                    return UserContactsFragment.newInstance(context, getIntent().getExtras());
                case 2:
                    //actionBar.setVisibility(View.INVISIBLE);
                    return ProfileFragment.newInstance(context);
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        public CharSequence getPageTitle(int position)
        {
            return null;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        hideDialog();
        this.appInFG = false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.appInFG = true;
    }

    public boolean isInForeground()
    {
        return this.appInFG;
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}