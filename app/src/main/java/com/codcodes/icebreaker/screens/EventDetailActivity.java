package com.codcodes.icebreaker.screens;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.ImageConverter;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.LocationDetector;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.SharedPreference;
import com.codcodes.icebreaker.auxilary.TimelineAdapter;
import com.codcodes.icebreaker.auxilary.ViewHeightAnimator;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IJsonable;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.codcodes.icebreaker.model.User;
import com.codcodes.icebreaker.tabs.EventsFragment;
import com.codcodes.icebreaker.tabs.UserContactsFragment;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;

public class EventDetailActivity extends AppCompatActivity implements IOnListFragmentInteractionListener
{
    private final String TAG = "IB/EventDetailActivity";

    private LatLng me;
    private Event selected_event;
    private boolean is_loading_timeline=true;
    private boolean timeline_in_fg = false;
    private ArrayList<AbstractMap.SimpleEntry<String, String>> image_to_audio_map;
    private RecyclerView rview;
    private IOnListFragmentInteractionListener mListener;

    private LocationDetector locationChecker;

    private Dialog dlgGpsHack;
    private TextView eventDetails;
    private ProgressDialog progress;

    private static final int RC_BARCODE_CAPTURE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        ImageView dbg_anim_logo = (ImageView)findViewById(R.id.imgLogo);

        if(dbg_anim_logo!=null)
        {
            dbg_anim_logo.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View view)
                {
                    dlgGpsHack = new Dialog(EventDetailActivity.this);
                    if(dlgGpsHack==null)
                        return false;

                    dlgGpsHack.setContentView(R.layout.gps_hack);
                    if(!dlgGpsHack.isShowing())
                        dlgGpsHack.show();

                    RadioButton rbtnAud = (RadioButton)dlgGpsHack.findViewById(R.id.rbtn_auditorium);
                    RadioButton rbtnStud = (RadioButton)dlgGpsHack.findViewById(R.id.rbtn_student_center);
                    RadioButton rbtnPond = (RadioButton)dlgGpsHack.findViewById(R.id.rbtn_pond);
                    RadioButton rbtnLib = (RadioButton)dlgGpsHack.findViewById(R.id.rbtn_library);

                    if (EventsFragment.debug_locations==null)
                        EventsFragment.debug_locations=new ArrayList<>();
                    if(EventsFragment.debug_locations.isEmpty())
                        EventsFragment.populateOverrideLocations();

                    rbtnAud.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mockLocation(EventsFragment.debug_locations.get(0).getValue().latitude,EventsFragment.debug_locations.get(0).getValue().longitude);
                        }
                    });

                    rbtnStud.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mockLocation(EventsFragment.debug_locations.get(1).getValue().latitude,EventsFragment.debug_locations.get(0).getValue().longitude);
                        }
                    });

                    rbtnPond.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mockLocation(EventsFragment.debug_locations.get(2).getValue().latitude,EventsFragment.debug_locations.get(0).getValue().longitude);
                        }
                    });

                    rbtnLib.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mockLocation(EventsFragment.debug_locations.get(3).getValue().latitude,EventsFragment.debug_locations.get(0).getValue().longitude);
                        }
                    });

                    return true;
                }
            });
        }
        Bundle extras = getIntent().getExtras();

        locationChecker = new LocationDetector();

        if(extras != null)
        {
            selected_event = extras.getParcelable("Event");
            TextView eventName = (TextView)findViewById(R.id.event_name);
            TextView eventDescription = (TextView)findViewById(R.id.event_description);

            if(selected_event==null||eventName==null||eventDescription==null)
                finish();

            eventName.setText(selected_event.getTitle());
            eventDescription.setText(selected_event.getDescription());
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ALPHA_8;
            Thread tIconLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Bitmap bitmap = LocalComms.getImage(EventDetailActivity.this, "event_icons-" + selected_event.getId(), ".png", "/events", options);
                        //if (bitmap == null)
                        //    bitmap = RemoteComms.getImage(EventDetailActivity.this, "event_icons-" + selected_event.getId(), ".png", "/events", options);

                        if (bitmap != null)
                        {
                            final ImageView eventImage = (ImageView) findViewById((R.id.event_image));
                            final Bitmap circularbitmap = ImageConverter.getRoundedCornerBitMap(bitmap, 100);
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    eventImage.setImageBitmap(circularbitmap);
                                }
                            });
                            bitmap.recycle();
                        }
                    }
                    catch (IOException e)
                    {
                        LocalComms.logException(e);
                    }
                }
            });
            tIconLoader.start();
        }else this.finish();

        rview = (RecyclerView)findViewById(R.id.imageTimeline);

        if (rview instanceof RecyclerView)
        {
            rview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }

        eventDetails = (TextView)findViewById(R.id.main_heading);
        Typeface heading = Typeface.createFromAsset(getAssets(),"Ailerons-Typeface.otf");
        eventDetails.setTypeface(heading);
        eventDetails.setTextSize(40);

        TextView txt_start_date = (TextView)findViewById(R.id.event_start_date);
        TextView txt_end_date = (TextView)findViewById(R.id.event_end_date);
        TextView txt_address = (TextView)findViewById(R.id.event_address);

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm");
        String sdate = sdf.format(new Date(selected_event.getDate()*1000L));
        String edate = sdf.format(new Date(selected_event.getEndDate()*1000L));

        txt_start_date.setText(sdate);
        txt_end_date.setText(edate);
        txt_address.setText(selected_event.getAddress());

        final EditText accessCode = (EditText) findViewById(R.id.AccessCode);

        accessCode.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionID, KeyEvent event)
            {
                if (actionID== EditorInfo.IME_ACTION_DONE || actionID== EditorInfo.IME_ACTION_NEXT)
                {
                    if(selected_event!=null)
                    {
                        if(selected_event.getEndDate()>=System.currentTimeMillis()/1000)
                        {
                            int code = 0;
                            try
                            {
                                code = Integer.parseInt(accessCode.getText().toString());
                                validateEventLogin(code);
                            } catch (NumberFormatException e)
                            {
                                LocalComms.logException(e);
                                Toast.makeText(EventDetailActivity.this, "Code is not a valid number!", Toast.LENGTH_LONG).show();
                            }
                        }else toastHandler("This event has already passed.").obtainMessage().sendToTarget();
                    }else toastHandler("This event is invalid.").obtainMessage().sendToTarget();
                }
                return false;
            }
        });

        //bitmaps = new ArrayList<>();
        image_to_audio_map = new ArrayList<>();
        Thread tImageDownloader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    /*String ev_id = WritersAndReaders.readAttributeFromConfig(Config.EVENT_ID.getValue());
                    if (ev_id != null)
                    {
                        if (!ev_id.isEmpty())
                        {
                            if (Long.parseLong(ev_id) > 0)
                            {*/
                                if(selected_event.getId()>0)//Selected event is valid
                                {
                                    is_loading_timeline = true;
                                    String img_paths = RemoteComms.sendGetRequest("getImageIDsAtEvent/" + selected_event.getId());
                                    //Remove inverted commas
                                    img_paths = img_paths.charAt(0) == '"' ? img_paths.substring(1) : img_paths;
                                    img_paths = img_paths.charAt(img_paths.length() - 1) == '"' ? img_paths.substring(0, img_paths.length() - 1) : img_paths;
                                    String[] img_path_arr = img_paths.split(";");
                                    for (String path : img_path_arr)
                                    {
                                        //path = path.replaceAll("\\|", ";");
                                        if (path != null)
                                        {
                                            if (path.contains("."))
                                            {
                                                String filename = path.split("\\.")[0];
                                                if(filename.contains("|"))
                                                {
                                                    String dir = filename.substring(0, filename.lastIndexOf('|'));
                                                    filename = filename.substring(filename.lastIndexOf('|') + 1);

                                                    //BitmapFactory.Options options = new BitmapFactory.Options();
                                                    //options.inPreferredConfig = Bitmap.Config.ALPHA_8;

                                                    //Bitmap bitmap = LocalComms.getImage(getApplicationContext(), filename, path.split("\\.")[1], dir, options);
                                                    //bitmaps.add(bitmap);
                                                    String audio_path = filename + ".wav";
                                                    String img_path = filename + ".png";

                                                    image_to_audio_map.add(new AbstractMap.SimpleEntry<>(img_path, audio_path));

                                                    /*byte[] audio = LocalComms.getFile(getApplicationContext(), filename, ".wav", dir);
                                                    if (audio != null)
                                                        Log.v(TAG, "Audio size for '" + filename + ".wav': " + audio.length);
                                                    else Log.v(TAG, "Audio file '" + filename + ".wav' does not exist on server/locally");*/
                                                }else Log.wtf(TAG, "Invalid file path.");
                                            } else
                                            {
                                                Log.wtf(TAG, "Image path does not have an extension.");
                                                toastHandler("Image path does not have an extension.").obtainMessage().sendToTarget();
                                            }
                                        } else
                                        {
                                            Log.d(TAG, "Invalid path received from server.");
                                            toastHandler("Invalid path received from server.").obtainMessage().sendToTarget();
                                        }
                                    }
                                }else
                                {
                                    Log.d(TAG, "Invalid Icebreak event selected.");
                                    toastHandler("Invalid Icebreak event selected.").obtainMessage().sendToTarget();
                                }
                                is_loading_timeline = false;
                                //TODO: Think about calling setAdapter here
                            /*}else
                            {
                                Log.d(TAG, "You're not signed in to an Icebreak event.");
                                toastHandler("You're not signed in to an Icebreak event.").obtainMessage().sendToTarget();
                            }
                        }else
                        {
                            Log.d(TAG, "You're not signed in to an Icebreak event.");
                            toastHandler("You're not signed in to an Icebreak event.").obtainMessage().sendToTarget();
                        }
                    }else
                    {
                        Log.d(TAG, "You're not signed in to an Icebreak event.");
                        toastHandler("You're not signed in to an Icebreak event.").obtainMessage().sendToTarget();
                    }*/
                }catch (NumberFormatException e)
                {
                    LocalComms.logException(e);
                } catch (IOException e)
                {
                    LocalComms.logException(e);
                }
            }
        });
        tImageDownloader.start();
        Thread tTimelineLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (is_loading_timeline){Log.v(TAG,"Is loading Story.");}
                EventDetailActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        rview.setAdapter(new TimelineAdapter(EventDetailActivity.this,getSupportFragmentManager(), image_to_audio_map,  mListener));
                        timeline_in_fg=true;
                        //btnShowTimeline.setClickable(true);
                        //btnShowTimeline.setText("Hide Event Story");
                    }
                });
            }
        });
        tTimelineLoader.start();
    }

    public void showTimeline(View view)
    {
        final LinearLayout bottom_bar = (LinearLayout)findViewById(R.id.dragView);
        //final Button btnShowTimeline = (Button) findViewById(R.id.btnShowTimeline);
        //ViewGroup.LayoutParams params = bottom_bar.getLayoutParams();
        final int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        //DisplayMetrics metrics = getResources().getDisplayMetrics();

        if(timeline_in_fg)//hide timeline
        {
            ViewHeightAnimator heightAnim = new ViewHeightAnimator(bottom_bar, 1,height);
            heightAnim.setDuration(200);
            bottom_bar.startAnimation(heightAnim);
            timeline_in_fg=false;
            //btnShowTimeline.setText("Events Story");
        }else//display timeline
        {
            //btnShowTimeline.setText("Story Loading...");
            //btnShowTimeline.setClickable(false);
            Thread tTimelineLoader = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    while (is_loading_timeline){Log.v(TAG,"Is loading Story.");}
                    EventDetailActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            rview.setAdapter(new TimelineAdapter(EventDetailActivity.this, getSupportFragmentManager(), image_to_audio_map,  mListener));
                            ViewHeightAnimator heightAnim = new ViewHeightAnimator(bottom_bar, height, 5);
                            heightAnim.setDuration(200);
                            bottom_bar.startAnimation(heightAnim);
                            timeline_in_fg=true;
                            //btnShowTimeline.setClickable(true);
                            //btnShowTimeline.setText("Hide Event Story");
                        }
                    });
                }
            });
            tTimelineLoader.start();
        }
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (this instanceof IOnListFragmentInteractionListener)
        {
            mListener = (IOnListFragmentInteractionListener) this;
        }
        else
        {
            throw new RuntimeException(this.toString()
                    + " must implement IOnListFragmentInteractionListener");
        }
    }

    @Override
    public void onListFragmentInteraction(IJsonable item)
    {
        Log.d(TAG,"Got JSonable object.");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mListener = null;
    }

    public void mockLocation(double lat, double lng)
    {
        String msg="Going to ["+lat+","+lng+"]";
        Toast.makeText(EventDetailActivity.this,msg,Toast.LENGTH_LONG).show();

        Location mockLocation = new Location(MainActivity.mocLocationProvider); // a string
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lng);
        mockLocation.setTime(System.currentTimeMillis());

        //onLocationChanged(mockLocation);

        if(dlgGpsHack!=null)
            if(dlgGpsHack.isShowing())
                dlgGpsHack.hide();
    }

    public void viewPosition(View view)
    {
        Intent i = new Intent(this,EventTimelineActivity.class);
        i.putExtra("Location_lat",String.valueOf(MainActivity.mLastKnownLoc.getLatitude()));
        i.putExtra("Location_lng",String.valueOf(MainActivity.mLastKnownLoc.getLongitude()));
        i.putExtra("Event",selected_event);
        startActivity(i);
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
        me = new LatLng(MainActivity.mLastKnownLoc.getLatitude(),
                            MainActivity.mLastKnownLoc.getLongitude());
        if(me!=null)
        {
            if (matchAccessCode(code))
            {
                if (locationChecker.containsLocation(me, selected_event.getBoundary(), true))
                {
                    Log.d(TAG,"Valid code and location.");
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

                                    if (selected_event == null)
                                        return;
                                    if (selected_event.getId() > 0)
                                    {
                                        user.setEvent(selected_event);
                                        String resp = RemoteComms.postData("userUpdate/" + user.getUsername(), user.toString());

                                        Message message;

                                        if (resp.contains("200"))
                                        {
                                            String ev_title = selected_event.getTitle();

                                            WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),
                                                    String.valueOf(selected_event.getId()));

                                            message = toastHandler("Signed in to event \"" + ev_title + "\"").obtainMessage();
                                            message.sendToTarget();

                                            Log.d(TAG, "Signed in to event \"" + ev_title + "\".");

                                            Intent i = new Intent(EventDetailActivity.this,MainActivity.class);
                                            i.putExtra("Fragment", UserContactsFragment.class.getName());
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(i);
                                        } else
                                        {
                                            /*WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),
                                                    String.valueOf(0));*/
                                            message = toastHandler("Could not login to event, server response: " + resp).obtainMessage();
                                            message.sendToTarget();
                                            Log.d(TAG, resp);
                                        }
                                    } else
                                    {
                                        /*WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),
                                                String.valueOf(0));*/
                                        Message message = toastHandler("Event is null.").obtainMessage();
                                        message.sendToTarget();
                                        Log.wtf(TAG, "Event is null for some reason.");
                                    }
                                } else
                                {
                                    /*WritersAndReaders.writeAttributeToConfig(Config.EVENT_ID.getValue(),
                                            String.valueOf(0));*/
                                    Log.wtf(TAG, "User object is null.");
                                    Message message = toastHandler("Could not sign in to event, User object is null.").obtainMessage();
                                    message.sendToTarget();
                                }
                            }catch (SocketTimeoutException e)
                            {
                                Message message = toastHandler("We are having trouble connecting to the server, please check your internet connection.").obtainMessage();
                                message.sendToTarget();
                                Log.d(TAG, e.getMessage(), e);
                            }
                            catch (UnknownHostException e)
                            {
                                Message message = toastHandler("No Internet access.").obtainMessage();
                                message.sendToTarget();
                                Log.d(TAG, e.getMessage(), e);
                            } catch (IOException e)
                            {
                                Log.wtf(TAG, e.getMessage(), e);
                            } finally
                            {
                                LocalComms.hideProgressBar(progress);
                            }
                        }
                    });
                    tEventDataLoader.start();
                    //TODO: Go to UserContactsFragment
                } else
                {
                    Toast.makeText(this, "Your location reading says that you're not at this event. \nPlease check that your GPS is on and you have an Internet connection.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "User is not actually at the Event.");
                }
            } else
            {
                Toast.makeText(this, "Invalid access code. Please try again.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Invalid Access Code Entered");
            }
        }
        else Log.d(TAG,"Location is null.");
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
            if(dist<=4)
                return true;
        }
        return false;
    }

    public boolean matchAccessCode(int code)
    {
        if(code == selected_event.getAccessCode())
            return true;
        return false;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("com.codcodes.icebreaker.Back",true);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        this.finish();
    }
}
