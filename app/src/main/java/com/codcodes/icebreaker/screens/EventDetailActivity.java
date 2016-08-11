package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.ProgressBar;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.ImageUtils;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.Restful;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.UserListRecyclerViewAdapter;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class EventDetailActivity extends AppCompatActivity implements IOnListFragmentInteractionListener
{
    private static final boolean DEBUG = true;
    private final String TAG = "ICEBREAK";

    private int Eventid;
    private ArrayList<User> users;
    private ArrayList<String> Name;
    private ArrayList<String> Catchphrase;
    private ArrayList<String> userIcon;
    private int AccessCode;
    private IOnListFragmentInteractionListener mListener;
    //private ListView lv;
    private RecyclerView usersAtEventList;
    private ViewFlipper vf;
    private TextView eventDetails;
    private ProgressDialog progress;
    private static boolean CHUNKED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Name = new ArrayList<String>();
        Catchphrase= new ArrayList<String>();
        userIcon = new ArrayList<String>();
        mListener = (IOnListFragmentInteractionListener) this;

        final String username = SharedPreference.getUsername(getApplicationContext());

        Bundle extras = getIntent().getExtras();
        final Activity act =this;

        if(extras != null)
        {
            String evtName = extras.getString("Event Name");
            TextView eventName = (TextView)findViewById(R.id.event_name);
            eventName.setText(evtName);

            Eventid = extras.getInt("Event ID");
            AccessCode = extras.getInt("Access ID");
            System.out.println(AccessCode);

            TextView eventDescription = (TextView)findViewById(R.id.event_description);
            eventDescription.setText(extras.getString("Event Description"));

            String imagePath = Environment.getExternalStorageDirectory().getPath().toString()
                    + extras.getString("Image ID");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imagePath);
            Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);
            ImageView eventImage = (ImageView) findViewById((R.id.event_image));

            eventImage.setImageBitmap(circularbitmap);
            bitmap.recycle();
        }

        eventDetails = (TextView)findViewById(R.id.Event_Heading);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        eventDetails.setTypeface(heading);
        //lv= (ListView) findViewById(R.id.contactList);
        usersAtEventList = (RecyclerView) findViewById(R.id.users_at_event_list);
        final EditText accessCode = (EditText) findViewById(R.id.AccessCode);

        accessCode.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionID, KeyEvent event)
            {
                if (actionID== EditorInfo.IME_ACTION_DONE)
                {
                    if(matchAccessCode(Integer.parseInt(accessCode.getText().toString())))
                    {
                        showProgressBar();
                        //updateProfile(Eventid,username);
                        listPeople(act);
                    }
                    else
                    {
                        accessCode.setError("Invalid Access Code Entered");
                    }
                }
                return false;
            }
        });
    }

    public void showProgressBar()
    {
        progress=new ProgressDialog(this);
        progress.setMessage("Loading List");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();
    }

    public void updateProfile(final int eventID,final String username)
    {
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();

                try
                {
                    Socket soc = new Socket(InetAddress.getByName("icebreak.azurewebsites.net"), 80);
                    System.out.println("Connection established");
                    PrintWriter out = new PrintWriter(soc.getOutputStream());
                    System.out.println("Sending request");

                    String data = URLEncoder.encode("event_ID", "UTF-8") + "=" + URLEncoder.encode(Integer.toString(eventID), "UTF-8");

                    out.print("POST /IBUserRequestService.svc/userUpdate/"+username+" HTTP/1.1\r\n"
                            + "Host: icebreak.azurewebsites.net\r\n"
                            + "Content-Type: text/plain; charset=utf-8\r\n"
                            + "Content-Length: " + data.length() + "\r\n\r\n"
                            + data);
                    out.flush();

                    BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                    String resp;
                    boolean found = false;
                    while((resp = in.readLine())!=null)
                    {
                        if (DEBUG) System.out.println(resp);
                        Log.d("ICEBREAK",resp);
                        if(resp.contains("HTTP/1.1 200 OK"))
                        {
                            Log.d("ICEBREAK","Found HTTP attr");
                            found = true;
                            break;
                        }
                    }

                    out.close();
                    in.close();
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "No internet access", Toast.LENGTH_LONG).show();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("com.codcodes.icebreaker.Back",true);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    public boolean matchAccessCode(int code)
    {
        if(code == AccessCode)
        {
            return true;
        }
        return false;
    }

    public void listPeople(final Activity context)
    {
        Thread tContactsLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                if(Eventid > 0)
                {
                    try
                    {
                        String contactsJson = Restful.sendGetRequest("getUsersAtEvent/" + Eventid);
                        final ArrayList<User> contacts = new ArrayList<>();
                        JSON.<User>getJsonableObjectsFromJson(contactsJson, contacts, User.class);
                        System.err.println("Contacts at event: " + Eventid+ " " + contacts.size() + " people");

                        final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                        Bitmap circularbitmap = null;
                        Bitmap bitmap = null;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                        //Attempt to load images into memory and set the list adapter
                        for (User u : contacts)
                        {
                            //Look for user profile image
                            if (!new File(Environment.getExternalStorageDirectory().getPath()
                                    + "/Icebreak/profile/" + u.getUsername() + ".png").exists()) {
                                //if (imageDownload(u.getUsername() + ".png", "/profile")) {
                                if (Restful.imageDownloader(u.getUsername(), ".png", "/profile", context))
                                {
                                    bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/" + u.getUsername() + ".png", options);
                                    //Bitmap bitmap = ImageUtils.getInstant().compressBitmapImage(holder.getView().getResources(),R.drawable.blue);
                                    circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                } else //user has no profile yet - attempt to load default profile image
                                {
                                    if (!new File(Environment.getExternalStorageDirectory().getPath().toString()
                                            + "/Icebreak/profile/default.png").exists())
                                    {
                                        //Attempt to download default profile image
                                        if (Restful.imageDownloader("default", ".png", "/profile", context))
                                        {
                                            /*bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                                    + "/Icebreak/profile/profile_default.png", getActivity());*/
                                            options = new BitmapFactory.Options();
                                            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                            bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                                    + "/Icebreak/profile/default.png", options);
                                            circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                        } else //Couldn't download default profile image
                                        {
                                            Toast.makeText(getApplicationContext(), "Could not download default profile images, please check your internet connection.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else//default profile image exists
                                    {
                                        /*bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/profile_default.png",getActivity());*/
                                        options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
                                        bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath().toString()
                                                + "/Icebreak/profile/default.png", options);
                                        circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                                    }
                                }
                            } else//user profile image exists
                            {
                                bitmap = ImageUtils.getInstant().compressBitmapImage(Environment.getExternalStorageDirectory().getPath().toString()
                                        + "/Icebreak/profile/" + u.getUsername() + ".png", context);
                                circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, R.dimen.dp_size_300);
                            }
                            if (bitmap == null || circularbitmap == null) {
                                System.err.println("Bitmap is null");
                            } else {
                                bitmaps.add(circularbitmap);
                                bitmap.recycle();
                            }
                        }
                        //Update UI

                         runOnUiThread(new Runnable()
                         {
                             @Override
                             public void run()
                             {
                                 if (usersAtEventList != null)
                                 {
                                     usersAtEventList.setLayoutManager(new LinearLayoutManager(context));
                                     usersAtEventList.setAdapter(new UserListRecyclerViewAdapter(contacts, bitmaps, mListener));
                                     getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

                                     vf = (ViewFlipper) findViewById(R.id.viewFlipper);
                                     eventDetails.setText("List Of People");
                                     vf.showNext();
                                     Log.d(TAG, "Set users at event list");
                                     progress.hide();
                                 }
                             }
                         });
                    } catch (IOException e) {
                        //TODO: Error Logging
                        e.printStackTrace();
                    } catch (java.lang.InstantiationException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    } catch (IllegalAccessException e)
                    {
                        //TODO: Error Logging
                        e.printStackTrace();
                    }
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Invalid Event ID",Toast.LENGTH_SHORT).show();
                }
            }
        });
        tContactsLoader.start();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra("com.codcodes.icebreaker.Back",true);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

    @Override
    public void onListFragmentInteraction(User user)
    {
        Intent intent = new Intent(this,OtherUserProfileActivity.class);
        intent.putExtra("Firstname",user.getFirstname());
        intent.putExtra("Lastname",user.getLastname());
        intent.putExtra("Username",user.getUsername());
        intent.putExtra("Age",user.getAge());
        intent.putExtra("Gender",user.getGender());
        intent.putExtra("Occupation",user.getOccupation());
        intent.putExtra("Bio",user.getBio());

        startActivity(intent);
    }
}
