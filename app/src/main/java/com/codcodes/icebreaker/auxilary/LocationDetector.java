package com.codcodes.icebreaker.auxilary;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.location.LocationServices;

/**
 * Created by MrSekati on 8/15/2016.
 */
public class LocationDetector implements LocationListener{
    private Context myContext;
    private LocationManager locationManager;
    private Location location;

    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_UPDATES = 1000 * 60 * 30;

    public LocationDetector(Context myContext) {
        this.myContext = myContext;
    }

    public boolean isGPSEnabbled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public Location getLocation() {
        locationManager = (LocationManager) myContext.getSystemService(myContext.LOCATION_SERVICE);
        if(!isGPSEnabbled())
        {
           // showEnableDilog();
          //  Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
          //  myContext.startActivity(intent);
            Log.d("Testing","Disabled");
            return null;
        }
        else
        {
            try{
                if(location == null)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if(locationManager != null)
                    {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                    return location;

                }
            }catch (SecurityException e)
            {
                //dialogGPS(myContext); // lets the user know there is a problem with the gps
            }

        }
        return null;
    }

    private void showEnableDilog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(myContext);
        alertBuilder.setMessage("Location Disabled Would You like to Enable it?");
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton("Enable Location", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                myContext.startActivity(intent);
            }
        });
        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog arlert = alertBuilder.create();
        arlert.show();
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
