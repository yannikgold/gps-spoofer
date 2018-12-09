package de.hsflensburg.mobsec.fakegps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.maps.GeoApiContext;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FAKEGPS";
    private TextView tvLatitude, tvLongitude;
    private TextView tvLatIs, tvLngIs;
    private GeoApiContext mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MockLocationService service = new MockLocationService();
        final Intent intent = new Intent(this, MockLocationService.class);
        final Context ctx = this;

        findViewById(R.id.LocationSet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.removeExtra("mode");
                intent.putExtra("mode", "start");
                startService(intent);
                Toast.makeText(ctx, "Thread gestartet", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.LocationStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(intent);

                Intent stopIntent = new Intent("StopUpdate");
                LocalBroadcastManager.getInstance(ctx).sendBroadcast(stopIntent);

                Toast.makeText(ctx, "Thread gestoppt", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.runSimulation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.removeExtra("mode");
                intent.putExtra("mode", "simulate");
                startService(intent);
                Toast.makeText(ctx, "Simulation gestartet", Toast.LENGTH_SHORT).show();
            }
        });

        tvLatitude = findViewById(R.id.tvLat);
        tvLongitude = findViewById(R.id.tvLong);
        tvLngIs = findViewById(R.id.tvLatIs);
        tvLatIs = findViewById(R.id.tvLngIs);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("UpdateLocation"));

        mContext = new GeoApiContext().setApiKey(getString(R.string.google_maps_web_services_key));

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyLocation", "onReceive");
            tvLatitude.setText("Latitude: " + Double.toString(intent.getDoubleExtra("Latitude", 0.0d)));
            tvLongitude.setText("Longitude: " + Double.toString(intent.getDoubleExtra("Longitude", 0.0d)));
            tvLatIs.setText("Latitude: " + Double.toString(intent.getDoubleExtra("Latitude", 0.0d)));
            tvLngIs.setText("Longitude: " + Double.toString(intent.getDoubleExtra("Longitude", 0.0d)));
        }
    };
}
