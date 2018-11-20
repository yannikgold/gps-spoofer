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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FAKEGPS";
    private TextView tvLatitude, tvLongitude;

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

        tvLatitude = findViewById(R.id.tvLat);
        tvLongitude = findViewById(R.id.tvLong);

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("UpdateLocation"));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyLocation", "onReceive");
            tvLatitude.setText("Latitude: " + Double.toString(intent.getDoubleExtra("Latitude", 0.0d)));
            tvLongitude.setText("Longitude: " + Double.toString(intent.getDoubleExtra("Longitude", 0.0d)));
        }
    };
}
