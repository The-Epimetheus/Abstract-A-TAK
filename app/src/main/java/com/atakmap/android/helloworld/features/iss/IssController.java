package com.atakmap.android.helloworld.features.iss;

import android.util.JsonReader;
import android.util.Log;

import com.atakmap.android.helloworld.abstraction.Disposable;
import com.atakmap.android.helloworld.features.marker.MarkerCreator;
import com.atakmap.android.helloworld.features.marker.MarkerSpec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The ISS-tracking demo's Controller: a toggleable {@link Timer} polls
 * open-notify's ISS position feed every 3 seconds and plots (or moves) the
 * ISS marker through {@link MarkerCreator}. ATAK-free — the HTTP fetch and
 * JSON parse are plain java/android; only the marker work crosses the seam.
 *
 * <p>Holds a live Timer, so it implements {@link Disposable}; the Pane
 * controller cascades {@link #dispose()} on plugin unload (a leaked Timer
 * would keep polling — and moving the marker — into the next hot-load).
 */
public class IssController implements Disposable {

    private static final String TAG = "IssController";

    /** Fixed uid so each poll updates the one ISS marker instead of stacking new ones. */
    static final String ISS_UID = "iss-unique-identifier";

    /** Meta key where the marker remembers when it last moved (elapsed-realtime ms). */
    private static final String LAST_UPDATE_KEY = "iss.lastUpdateTime";

    private final MarkerCreator markerCreator;

    private Timer issTimer = null;
    private boolean tracking = false;

    public IssController(MarkerCreator markerCreator) {
        this.markerCreator = markerCreator;
    }

    /** Whether the poll timer is running — drives the button's selected state. */
    public synchronized boolean isTracking() {
        return tracking;
    }

    /**
     * Toggle ISS tracking. Turning it on plots immediately and then every
     * 3 seconds; turning it off cancels the schedule (the marker stays on the
     * map, exactly as the legacy demo left it).
     *
     * @return true if tracking is now ON — mirror this into the button's
     *         selected state.
     */
    public synchronized boolean toggleIssTracking() {
        boolean wasTracking = tracking;
        if (issTimer != null) {
            issTimer.cancel();
            issTimer.purge();
            issTimer = null;
        }
        // The legacy receiver created a fresh Timer on every tap — including
        // the "stop" tap, leaving an idle Timer behind. Kept verbatim; the
        // idle Timer is reaped by the same cancel/purge path in dispose().
        issTimer = new Timer();
        tracking = !wasTracking;
        if (!wasTracking) {
            issTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    plotISSLocation();
                }
            }, 0, 3000);
        }
        return tracking;
    }

    /** Idempotent Timer teardown; the Pane controller cascades this on unload. */
    @Override
    public synchronized void dispose() {
        if (issTimer != null) {
            issTimer.cancel();
            issTimer.purge();
            issTimer = null;
        }
        tracking = false;
    }

    /**
     * One poll: fetch the ISS position (cleartext HTTP — see the pane wiring
     * comment about Android 9+), parse it, and move the ISS marker, placing
     * it first if it isn't on the map yet. Runs on the Timer thread, which
     * keeps the network I/O off the UI thread — same as the legacy demo.
     */
    private void plotISSLocation() {
        double lat = Double.NaN, lon = Double.NaN;
        try {
            final InputStream input = new URL(
                    "http://api.open-notify.org/iss-now.json").openStream();
            // The legacy code read the body with ATAK's
            // FileSystemUtils.copyStreamToString; readFully() keeps the same
            // contract (read fully as UTF-8, close the stream) in plain
            // java.io so this Controller stays ATAK-free.
            final String returnJson = readFully(input);

            Log.d(TAG, "return json: " + returnJson);

            JsonReader jr = new JsonReader(new StringReader(returnJson));
            jr.beginObject();
            while (jr.hasNext()) {
                String name = jr.nextName();
                switch (name) {
                    case "iss_position":
                        jr.beginObject();
                        while (jr.hasNext()) {
                            String n = jr.nextName();
                            switch (n) {
                                case "latitude":
                                    lat = jr.nextDouble();
                                    break;
                                case "longitude":
                                    lon = jr.nextDouble();
                                    break;
                                case "message":
                                    jr.skipValue();
                                    break;
                            }
                        }
                        jr.endObject();
                        break;
                    case "timestamp":
                        jr.skipValue();
                        break;
                    case "message":
                        jr.skipValue();
                        break;
                }
            }
            jr.endObject();
        } catch (Exception e) {
            Log.e(TAG, "error", e);
        }
        if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
            // Move the existing marker, deriving its track from the previous
            // point (the impl owns the geo math); a non-Marker item under the
            // uid still counts as "exists" and is left alone — the legacy
            // demo's exact quirk.
            if (!markerCreator.moveMarkerWithTrack(ISS_UID, lat, lon,
                    LAST_UPDATE_KEY)) {
                markerCreator.placeMarker(MarkerSpec.at(lat, lon)
                        .uid(ISS_UID)
                        .callsign("International Space Station")
                        .type("a-f-P-T")
                        .dropAsIfByUser()
                        .showCotDetails(false)
                        .neverPersist(true)
                        // don't forget to turn on the arrow so that we know
                        // where the ISS is going
                        .rotateWithHeading()
                        //.meta("editable", false)
                        .meta("movable", false)
                        .meta("how", "m-g")
                        .build());
            }
        }
    }

    /** Read the whole stream as UTF-8 and close it, whatever happens. */
    private static String readFully(InputStream in) throws IOException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1)
                bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }
}
