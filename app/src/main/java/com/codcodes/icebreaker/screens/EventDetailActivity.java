package com.codcodes.icebreaker.screens;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.LocationDetector;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class EventDetailActivity extends AppCompatActivity
{
    private final String TAG = "IB/EventDetailActivity";

    private long Eventid;
    private String eventLoc;
    private int eventRadius;

    private ArrayList<LatLng> polygon;
    private LatLng me;
    private LocationDetector locationChecker;

    private int AccessCode;
    private int event_Radius;

    private RecyclerView usersAtEventList;
    private TextView eventDetails;
    private ProgressDialog progress;

    private LocationDetector locationDetector;

    private static final int RC_BARCODE_CAPTURE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //cationDetector = new LocationDetector();
       // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();

        polygon = new ArrayList<>();
        polygon.add(new LatLng(-26.182944, 27.997387));
        polygon.add(new LatLng(-26.183185, 27.996846));
        polygon.add(new LatLng(-26.183816, 27.996964));
        polygon.add(new LatLng(-26.184235, 27.997307));
        polygon.add(new LatLng(-26.184312, 27.997704));
        polygon.add(new LatLng(-26.184201, 27.997913));
        polygon.add(new LatLng(-26.184008, 27.998251));
        polygon.add(new LatLng(-26.183815, 27.998380));
        polygon.add(new LatLng(-26.183517, 27.998471));
        me = new LatLng(-26.182944,27.997387); //-26.183297, 27.995006
        locationChecker = new LocationDetector();

        /*
        //If there's a cached eventID use that
        String strEvId = SharedPreference.getEventId(this);
        if(strEvId!=null)
        {
            if(!strEvId.isEmpty())
            {
                if(Long.valueOf(strEvId)>0)
                {
                    Eventid = Long.valueOf(SharedPreference.getEventId(this));
                    showProgressBar();
                    //updateProfile(Eventid,username);
                    listPeople(act);
                }
            }
        }*/

        if(extras != null)
        {
            String evtName = extras.getString("Event Name");
            TextView eventName = (TextView)findViewById(R.id.event_name);
            eventName.setText(evtName);

            Eventid = extras.getLong("Event ID");
            AccessCode = extras.getInt("Access Code");
            event_Radius = extras.getInt("Event Radius");

            eventLoc = extras.getString("Event Location");
            eventRadius = extras.getInt("Event Radius");

            //location = (Location) extras.get("Access Location");
            //String[] part = eventLoc.split(":");

            //location.setLatitude(Double.valueOf(part[0]));
            //location.setLongitude(Double.valueOf(part[1]));


            TextView eventDescription = (TextView)findViewById(R.id.event_description);
            eventDescription.setText(extras.getString("Event Description"));

            String imagePath = Environment.getExternalStorageDirectory().getPath().toString()
                    + extras.getString("Image ID");

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imagePath);
            if(bitmap!=null)
            {
                Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);
                ImageView eventImage = (ImageView) findViewById((R.id.event_image));
                eventImage.setImageBitmap(circularbitmap);
                bitmap.recycle();
            }
        }else this.finish();

        eventDetails = (TextView)findViewById(R.id.Event_Heading);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        eventDetails.setTypeface(heading);
        usersAtEventList = (RecyclerView) findViewById(R.id.users_at_event_list);
        final EditText accessCode = (EditText) findViewById(R.id.AccessCode);

        accessCode.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionID, KeyEvent event)
            {
                if (actionID== EditorInfo.IME_ACTION_DONE)
                {
                    //Event e = RemoteComms.getEvent(Eventid);
                    validateEventLogin(Integer.parseInt(accessCode.getText().toString()));
                }
                return false;
            }
        });
        //Location loc;
       /* if((loc = locationDetector.getLocation()) != null)
        {

            Log.d("Testing", String.valueOf(loc.getLongitude()) + " : " + String.valueOf(loc.getLatitude() ));
        }*/

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


    private void validateEventLogin(int code)
    {
        if(matchAccessCode(code))
        {
            if(locationChecker.containsLocation(me,polygon,true))
            {
                progress = LocalComms.showProgressDialog(EventDetailActivity.this, "Signing in to event...");
                Thread tEventDataLoader = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            User user = LocalComms.getContact(EventDetailActivity.this, SharedPreference.getUsername(EventDetailActivity.this));
                            if (user == null)
                                user = RemoteComms.getUser(EventDetailActivity.this, SharedPreference.getUsername(EventDetailActivity.this));
                            if (user != null)
                            {
                                user.setUsername(SharedPreference.getUsername(EventDetailActivity.this));//for some reason the username is not being set by preceding methods
                                MainActivity.event_id = Eventid;
                                MainActivity.event = RemoteComms.getEvent(Eventid);

                                if (MainActivity.event != null && MainActivity.event_id>0)
                                {
                                    user.setEvent(MainActivity.event);
                                    String resp = RemoteComms.postData("userUpdate/" + user.getUsername(), user.toString());

                                    Message message;

                                    if (resp.contains("200"))
                                    {
                                        String ev_title = MainActivity.event.getTitle();

                                        SharedPreference.setEventId(EventDetailActivity.this, MainActivity.event_id);
                                        message = toastHandler("Signed in to event \"" + ev_title + "\"").obtainMessage();
                                        message.sendToTarget();

                                        if (MainActivity.users_at_event == null)
                                            MainActivity.users_at_event = new ArrayList<>();
                                        String contactsJson = RemoteComms.sendGetRequest("getUsersAtEvent/" + Eventid);
                                        JSON.<User>getJsonableObjectsFromJson(contactsJson, MainActivity.users_at_event, User.class);
                                        Log.d(TAG, "Signed in to event \"" + ev_title + "\".");

                                        EventDetailActivity.this.finish();
                                    } else
                                    {
                                        message = toastHandler("Could not login to event, server response: " + resp).obtainMessage();
                                        message.sendToTarget();
                                        Log.d(TAG, resp);
                                    }
                                } else Log.wtf(TAG, "Event is null for some reason.");
                            } else
                            {
                                Log.wtf(TAG, "User object is null.");
                                Message message = toastHandler("Could not sign in to event, User object is null.").obtainMessage();
                                message.sendToTarget();
                            }
                        } catch (IllegalAccessException e)
                        {
                            //TODO: better logging
                            Log.wtf(TAG, e.getMessage(), e);
                        } catch (InstantiationException e)
                        {
                            //TODO: better logging
                            Log.wtf(TAG, e.getMessage(), e);
                        } catch (UnknownHostException e)
                        {
                            Message message = toastHandler("No Internet Access..").obtainMessage();
                            message.sendToTarget();
                            Log.d(TAG, e.getMessage(), e);
                        } catch (IOException e)
                        {
                            //TODO: better logging
                            Log.wtf(TAG, e.getMessage(), e);
                        } finally
                        {
                            LocalComms.hideProgressBar(progress);
                        }
                    }
                });
                tEventDataLoader.start();
                //TODO: Go to UserContactsFragment
            }
            else
            {
                Toast.makeText(this,"Your location reading says that you're not at this event. \nPlease check that your GPS is on and you have an Internet connection.", Toast.LENGTH_LONG).show();
                Log.d(TAG,"User is not actually at the Event.");
            }
        }
        else
        {
            Toast.makeText(this,"Invalid access code. Please try again.", Toast.LENGTH_LONG).show();
            Log.d(TAG,"Invalid Access Code Entered");
        }
    }

    public void startQRscanner(View v)
    {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RC_BARCODE_CAPTURE)
        {
            if (resultCode == CommonStatusCodes.SUCCESS)
            {
                if (data != null)
                {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    validateEventLogin(Integer.valueOf(barcode.displayValue));
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                }
                else
                {
                    Toast.makeText(this,"No barcode captured.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else
            {
                Toast.makeText(this,String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)), Toast.LENGTH_LONG).show();
                Log.d(TAG, String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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
    public boolean inLocation(Location loc1, Location loc2 ,int radius)
    {
        if(loc1 != null || loc2 != null)
        {
            double earthRadius = 6371;
            double dLat = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
            double dLng = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(loc1.getLatitude())) * Math.cos(Math.toRadians(loc2.getLatitude())) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            float dist = (float) (earthRadius * c);
            Log.d("Testing","Distance : " + String.valueOf(dist));
            if(dist<=4)
            {
                return true;
            }
        }

        return false;
    }

    public boolean matchAccessCode(int code)
    {
        if(code == AccessCode)
        {
            return true;
        }

        return false;
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
}
