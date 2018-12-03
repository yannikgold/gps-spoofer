package de.hsflensburg.mobsec.fakegps;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.location.Location;

import android.location.LocationManager;
import android.location.LocationProvider;

import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RadioGroup;


import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MockLocationService extends Service {
    private static final String TAG = "FAKEGPS";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;
    MyLocationListener[] mLocationListeners;
    private LocationManager mLocationManager = null;
    private Handler handler = new Handler();

    private static final int PAGINATION_OVERLAP = 2;
    private List<SnappedPoint> snappedPoints = new ArrayList<>();
    private LatLng[] page = new LatLng[PAGINATION_OVERLAP];
    private double diffLatitude = 0.01;//Double.parseDouble(getString(R.string.SetLat));
    private double diffLongitude = 0.01;//Double.parseDouble(getString(R.string.SetLong));
    /**
     * The API context used for the Roads and Geocoding web service APIs.
     */
    private GeoApiContext mContext;

    private boolean firstRun = true;
    private int loop = 0;

    private List<LatLng> listPos = null;


    private int POIndex = 0;
    private SnappedPoint newLoc = null;



    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Thread wird ausgeführt!");

            Location actLoc = null;

            for (MyLocationListener locList : mLocationListeners) {
                if (locList.mLastLocation != null) {
                    actLoc = locList.mLastLocation;
                }
            }


            double newLat = actLoc.getLatitude() + diffLatitude;
            double newLng = actLoc.getLongitude() + diffLongitude;
            diffLatitude += Double.parseDouble(getString(R.string.ModLat));
            diffLongitude += Double.parseDouble(getString(R.string.ModLat));

            page[1] = new LatLng(newLat, newLng);
            loop++;

            if(firstRun) {
                firstRun = false;
                page[0] = new LatLng(newLat, newLng);
            }else{
            //if (loop==PAGINATION_OVERLAP) {
                callRoadsAPI();
                loop = 0;
                if (newLoc != null) {

                    LatLng newLL = newLoc.location;
                    double SnappedLat = newLL.lat; //54.776
                    double SnappedLng = newLL.lng; //9.442

                    //calc Diff
                    //diffLatitude =SnappedLat - actLoc.getLatitude()  ;
                    //diffLongitude =SnappedLng- actLoc.getLongitude() ;
                    // page[0] = new LatLng(SnappedLat, SnappedLng);


                    // if (actLoc != null) {
                    setMockLocation(SnappedLat, SnappedLng, 10);
                    page[0] = new LatLng(SnappedLat, SnappedLng);
                }
           // }
           // }else{
           //     setMockLocation(newLat, newLng, 10);
            }

            handler.postDelayed(runnable, 500);
        }
    };

    private void callRoadsAPI() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //snap
                    try {
                        mContext = new GeoApiContext().setApiKey(getString(R.string.google_maps_web_services_key));
                        SnappedPoint[] points = RoadsApi.snapToRoads(mContext, false, page).await();
//
                        if (points != null){
                            newLoc = points[points.length-1];
                    //        boolean passedOverlap = false;
                    //        for (SnappedPoint point : points) {
                    //            snappedPoints.add(point);
                    //            newLoc = point;
                    //        }
                        }
                       // newLoc = snappedPoints.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");

        mContext = new GeoApiContext().setApiKey(getString(R.string.google_maps_web_services_key));

        mLocationListeners = new MyLocationListener[]{
                new MyLocationListener(LocationManager.GPS_PROVIDER, getApplicationContext()),
                new MyLocationListener(LocationManager.NETWORK_PROVIDER, getApplicationContext())
        };

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        handler.post(runnable);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("StopUpdate"));
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }

        handler.removeCallbacks(runnable);
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void setMockLocation(double latitude, double longitude, float accuracy) {
        mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);

        Location newLocation = new Location(LocationManager.GPS_PROVIDER);

        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAccuracy(accuracy);
        newLocation.setTime(Calendar.getInstance().getTime().getTime());
        newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

        mLocationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());

        mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handler.removeCallbacks(runnable);
        }
    };

}
