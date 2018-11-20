package hsfl.androidsec.de.gps_spoofer;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class MockLocationProvider {
    String providerName;
    Context ctx;

    public MockLocationProvider(String name, Context ctx) {
        Log.d("MockLocationProvider", "MockLocationProvider initiated");
        this.providerName = name;
        this.ctx = ctx;

        LocationManager lmMockup = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lmMockup.addTestProvider(providerName, false, false, false, false, false,
                true, true, 0, 5);
        lmMockup.setTestProviderEnabled(providerName, true);
    }

    public void pushLocation(double lat, double lon) {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);

        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat + 1);
        mockLocation.setLongitude(lon + 1);
        mockLocation.setAltitude(0);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        mockLocation.setAccuracy(500);
        lm.setTestProviderLocation(providerName, mockLocation);
    }

    public void shutdown() {
        LocationManager lm = (LocationManager) ctx.getSystemService(
                Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }
}