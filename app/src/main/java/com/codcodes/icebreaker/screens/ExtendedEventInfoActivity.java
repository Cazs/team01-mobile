package com.codcodes.icebreaker.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.model.Event;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ExtendedEventInfoActivity extends AppCompatActivity
{
    private Event event = null;
    private Typeface ttfAilerons;
    private TextView title;
    private ProgressDialog progressDlg;
    private double lat = 0, lng = 0;
    private final String TAG = "IB/XEventInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extended_event_info);

        ttfAilerons = Typeface.createFromAsset(this.getAssets(),"Ailerons-Typeface.otf");
        title = (TextView)ExtendedEventInfoActivity.this.findViewById(R.id.main_heading);
        title.setTextSize(25);
        title.setTypeface(ttfAilerons);

        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            String slat = extras.getString("Location_lat");
            String slng = extras.getString("Location_lng");
            event = extras.getParcelable("Event");

            title.setText(event.getTitle());

            if(slat!=null)
                if(!slat.isEmpty())
                    lat = Double.valueOf(slat);

            if(slng!=null)
                if(!slng.isEmpty())
                    lng = Double.valueOf(slng);
        }else this.finish();

        final RelativeLayout imgGpsLoc = (RelativeLayout)findViewById(R.id.imgGpsLoc);

        progressDlg = LocalComms.showProgressDialog(ExtendedEventInfoActivity.this,"Fetching image...");
        Thread tImageLoader = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //TODO: have Event organisers set zoom level etc.
                    final Drawable bg = RemoteComms.getGoogleMapsBitmap(lat, lng, 18, 400, 1000, event.getBoundary());
                    //final BitmapDrawable background = new BitmapDrawable(b);
                    ExtendedEventInfoActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            imgGpsLoc.setBackground(bg);
                            LocalComms.hideProgressBar(progressDlg);
                        }
                    });
                }
                catch (IOException e)
                {
                    Log.wtf(TAG, e.getMessage(), e);
                }
            }
        });
        tImageLoader.start();
    }



    /*public static Bitmap getGoogleMapThumbnail(double lati, double longi){
        String URL = "http://maps.google.com/maps/api/staticmap?center=" +lati + "," + longi + "&zoom=15&size=200x200&sensor=false";
        Bitmap bmp = null;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet(URL);

        InputStream in = null;
        try {
            in = httpclient.execute(request).getEntity().getContent();
            bmp = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bmp;
    }*/

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent i = new Intent(this,EventDetailActivity.class);
        i.putExtra("Event",event);
        startActivity(i);
    }
}
