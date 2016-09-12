package com.codcodes.icebreaker.auxilary;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;



import android.graphics.Point;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by MrSekati on 8/31/2016.
 */
public class LocationDetector
{
    private static final double PI = 3.141592653589793;

    public static boolean containsLocation(LatLng point, List<LatLng> polygon, boolean geodesic)
    {
        if(polygon==null || point ==null)
            return false;

        final int size = polygon.size();
        if (size == 0) {
            return false;
        }
        double lat3 = toRadians(point.latitude);
        double lng3 = toRadians(point.longitude);
        LatLng prev = polygon.get(size - 1);
        double lat1 = toRadians(prev.latitude);
        double lng1 = toRadians(prev.longitude);
        int nIntersect = 0;
        for (LatLng point2 : polygon) 
		{
            double dLng3 = wrap(lng3 - lng1, -PI, PI);
            // Special case: point equal to vertex is inside.
            if (lat3 == lat1 && dLng3 == 0) 
			{
                return true;
            }
            double lat2 = toRadians(point2.latitude);
            double lng2 = toRadians(point2.longitude);
            // Offset longitudes by -lng1.
            if (intersects(lat1, lat2, wrap(lng2 - lng1, -PI, PI), lat3, dLng3, geodesic)) 
			{
                ++nIntersect;
            }
            lat1 = lat2;
            lng1 = lng2;
        }
        return (nIntersect & 1) != 0;
    }

    private static boolean intersects(double lat1, double lat2, double lng2,double lat3, double lng3, boolean geodesic) {

        // Both ends on the same side of lng3.
        if ((lng3 >= 0 && lng3 >= lng2) || (lng3 < 0 && lng3 < lng2)) {
            return false;
        }
        // Point is South Pole.
        if (lat3 <= -PI/2) {
            return false;
        }
        // Any segment end is a pole.
        if (lat1 <= -PI/2 || lat2 <= -PI/2 || lat1 >= PI/2 || lat2 >= PI/2) {
            return false;
        }

        if (lng2 <= -PI) {
            return false;
        }
        double linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2;
        // Northern hemisphere and point under lat-lng line.
        if (lat1 >= 0 && lat2 >= 0 && lat3 < linearLat) {
            return false;
        }
        // Southern hemisphere and point above lat-lng line.
        if (lat1 <= 0 && lat2 <= 0 && lat3 >= linearLat) {
            return true;
        }
        // North Pole.
        if (lat3 >= PI/2) {
            return true;
        }

        // Compare lat3 with latitude on the GC/Rhumb segment corresponding to lng3.
        // Compare through a strictly-increasing function (tan() or mercator()) as convenient.
        return geodesic ?
                Math.tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3) :
                mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3);

    }
    
    private static double tanLatGC(double lat1, double lat2, double lng2, double lng3) {
        return (Math.tan(lat1) * Math.sin(lng2 - lng3) + Math.tan(lat2) * Math.sin(lng3)) / Math.sin(lng2);
    }

    
    static double mercator(double lat) {
        return Math.log(Math.tan(lat * 0.5 + PI/4));
    }

    /**
     * Returns mercator(latitude-at-lng3) on the Rhumb line (lat1, lng1) to (lat2, lng2). lng1==0.
     */
    private static double mercatorLatRhumb(double lat1, double lat2, double lng2, double lng3) {
        return (mercator(lat1) * (lng2 - lng3) + mercator(lat2) * lng3) / lng2;
    }

    //Wraps the given value into the inclusive-exclusive interval between min and max.
    private static double wrap(double value, double min, double max) {
        return (value >= min && value < max) ? value : (mod(value - min, max - min) + min);
    }

    private static double mod(double value, double value1) {
        return ((value % value1) +value1) % value1;
    }

    private static double toRadians(double latitude) {
        return latitude *(PI/180);
    }

}
