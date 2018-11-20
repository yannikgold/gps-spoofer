package de.hsflensburg.mobsec.fakegps;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyLocationListener implements android.location.LocationListener{
    private static final String TAG = "FAKEGPS";
    public Location mLastLocation;
    Context context;

    public MyLocationListener(String provider, Context ctx) {
        Log.e(TAG, "LocationListener " + provider);
        mLastLocation = new Location(provider);
        context = ctx;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged: " + location);
        mLastLocation.set(location);

        Intent intent = new Intent("UpdateLocation");
        intent.putExtra("Latitude",mLastLocation.getLatitude());
        intent.putExtra("Longitude",mLastLocation.getLongitude());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG, "onStatusChanged: " + provider);
    }
}
