package com.atakmap.android.helloworld.features.location;

import android.util.Log;

/**
 * The location feature's Controller. Owns the mock-GPS demo behavior: runs
 * the simulated fix off the UI thread (a fix stamps metadata and fires a
 * broadcast — not tap-handler work) and logs the applied timestamp. The
 * version-diverged metadata store is already behind {@link LocationCreator};
 * this class is the uniform tap → Controller → Creator hop (ADR-0005).
 */
public class LocationController {

    private static final String TAG = "LocationController";

    private final LocationCreator locationCreator;

    public LocationController(LocationCreator locationCreator) {
        this.locationCreator = locationCreator;
    }

    /**
     * Feed one simulated self-position fix into ATAK's mock-GPS pipeline,
     * off the UI thread. Logs the stamped mock timestamp on success.
     */
    public void simulateGpsFix() {
        new Thread(this::runSim).start();
    }

    private synchronized void runSim() {
        long mockLocationTime = locationCreator
                .simulateSelfLocation(-44.0, 22.0); // decimal degrees
        if (mockLocationTime >= 0) {
            Log.d(TAG, "simulated gps fix applied, last seen time: "
                    + mockLocationTime);
        }
    }
}
