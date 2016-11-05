package com.codcodes.icebreaker.screens;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.codcodes.icebreaker.R;
import com.codcodes.icebreaker.auxilary.Config;
import com.codcodes.icebreaker.auxilary.JSON;
import com.codcodes.icebreaker.auxilary.LocalComms;
import com.codcodes.icebreaker.auxilary.RemoteComms;
import com.codcodes.icebreaker.auxilary.TimelineAdapter;
import com.codcodes.icebreaker.auxilary.UserListRecyclerViewAdapter;
import com.codcodes.icebreaker.auxilary.ViewHeightAnimator;
import com.codcodes.icebreaker.auxilary.WritersAndReaders;
import com.codcodes.icebreaker.model.Event;
import com.codcodes.icebreaker.model.IJsonable;
import com.codcodes.icebreaker.model.IOnListFragmentInteractionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;

public class EventTimelineActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private double lat = 0, lng = 0;
    private Event event = null;
    //private ArrayList<Bitmap> bitmaps;

    private static final String TAG = "IB/EventTmlnActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_timeline);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();

        if(extras != null)
        {
            String slat = extras.getString("Location_lat");
            String slng = extras.getString("Location_lng");
            event = extras.getParcelable("Event");

            if(slat!=null)
                if(!slat.isEmpty())
                    lat = Double.valueOf(slat);

            if(slng!=null)
                if(!slng.isEmpty())
                    lng = Double.valueOf(slng);
        }else this.finish();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.setIndoorEnabled(true);
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
        }*/
        LatLng usr_loc = new LatLng(lat,lng);

        CircleOptions ev_loc_opts = new CircleOptions();
        ev_loc_opts.center(event.getOrigin());
        ev_loc_opts.clickable(false);
        ev_loc_opts.strokeColor(Color.BLACK);
        ev_loc_opts.fillColor(Color.BLACK);
        ev_loc_opts.radius(5);
        mMap.addCircle(ev_loc_opts);

        CircleOptions usr_loc_opts = new CircleOptions();
        usr_loc_opts.center(usr_loc);
        usr_loc_opts.clickable(false);
        usr_loc_opts.strokeColor(Color.CYAN);
        usr_loc_opts.fillColor(Color.CYAN);
        usr_loc_opts.radius(5);
        mMap.addCircle(usr_loc_opts);

        PolylineOptions geo_fence_opts = new PolylineOptions();
        geo_fence_opts.color(Color.DKGRAY);
        geo_fence_opts.clickable(false);
        geo_fence_opts.addAll(event.getBoundary());
        mMap.addPolyline(geo_fence_opts);

        PolylineOptions usr_ev_opts = new PolylineOptions();
        usr_ev_opts.color(Color.parseColor("#079afc"));
        usr_ev_opts.clickable(false);
        usr_ev_opts.add(event.getOrigin());
        usr_ev_opts.add(usr_loc);
        mMap.addPolyline(usr_ev_opts);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(event.getOrigin(), 16.0f));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(-26.181520, 27.996154)));
    }
}
