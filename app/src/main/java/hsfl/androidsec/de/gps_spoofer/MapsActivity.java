package hsfl.androidsec.de.gps_spoofer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager lm;
    PolylineOptions polyline = new PolylineOptions();
    String providerName = "GPS_PROVIDER";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        LocationManager lmMockup = (LocationManager) getApplicationContext().getSystemService(
                Context.LOCATION_SERVICE);
        if (lmMockup.getProvider(providerName) != null) {
            lmMockup.removeTestProvider(providerName);
        }
        lmMockup.addTestProvider(providerName, false, false, false, false, false,
                true, true, 0, 5);
        lmMockup.setTestProviderEnabled(providerName, true);
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Flensburg and move the camera
        /*LatLng flensburg = new LatLng(54.792636, 9.431104);
        mMap.addMarker(new MarkerOptions().position(flensburg).title("Marker in Flensburg"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(flensburg));*/

        Toast.makeText(getApplicationContext(), "Map loaded", Toast.LENGTH_SHORT).show();
        Log.d("onMapReady", "Google Map loaded");

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,0, mLocationListener);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            Log.d("onLocationChange", "Location has changed");
            LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
            mMap.setMinZoomPreference(15);
            polyline.add(newLocation);
            Marker marker = mMap.addMarker(new MarkerOptions().position(newLocation));
            mMap.addPolyline(polyline.color(Color.RED));
            Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();

            Location mockLocation = new Location(providerName);
            mockLocation.setLatitude(location.getLatitude() + 1);
            mockLocation.setLongitude(location.getLongitude() + 1);
            mockLocation.setAltitude(0);
            mockLocation.setTime(System.currentTimeMillis());
            mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            mockLocation.setAccuracy(10);
            mockLocation.setBearing(0);
            lm.setTestProviderLocation(providerName, mockLocation);
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
    };
}
