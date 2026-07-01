package com.atakmap.android.helloworld.compat;

import android.os.SystemClock;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.time.CoordinatedTime;

/**
 * Compatibility-band impl for ATAK >= 5.3, where {@code MapView.getMapData()} returns
 * {@code MetaDataHolder2} (extends MetaDataHolder) with a {@code setMetaX} API — the
 * Bundle-style {@code putX}/{@code MapData} of <=5.2 was removed.
 *
 * <p>Lives in the {@code src/atak53plus} source set, added ONLY to the
 * atak530..atak580 flavors. Identical fully-qualified name to the {@code src/atakPre53}
 * twin, so exactly one is compiled per APK. (No parcelable equivalent in the new API,
 * so the point is stored as lat/lon metadata.)
 */
public final class MockLocationApplier {

    private MockLocationApplier() {}

    public static long apply(MapView view, GeoPoint gp) {
        long t = SystemClock.elapsedRealtime();
        view.getMapData().setMetaDouble("mockLocationSpeed", 20); // m/s
        view.getMapData().setMetaDouble("mockLocationAccuracy", 5.0); // meters
        view.getMapData().setMetaString("locationSourcePrefix", "mock");
        view.getMapData().setMetaBoolean("mockLocationAvailable", true);
        view.getMapData().setMetaString("mockLocationSource", "Hello World Plugin");
        view.getMapData().setMetaString("mockLocationSourceColor", "#FFAFFF00");
        view.getMapData().setMetaBoolean("mockLocationCallsignValid", true);
        view.getMapData().setMetaDouble("mockLocationLat", gp.getLatitude());
        view.getMapData().setMetaDouble("mockLocationLon", gp.getLongitude());
        view.getMapData().setMetaLong("mockLocationTime", t);
        view.getMapData().setMetaLong("mockGPSTime", new CoordinatedTime().getMilliseconds());
        view.getMapData().setMetaInteger("mockFixQuality", 2);
        return t;
    }
}
