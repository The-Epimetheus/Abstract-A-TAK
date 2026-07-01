package com.atakmap.android.helloworld.compat;

import android.os.SystemClock;

import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.time.CoordinatedTime;

/**
 * Compatibility-band impl for ATAK <= 5.2, where {@code MapView.getMapData()} returns
 * {@code MapData} with a Bundle-style {@code putX} API. Byte-confirmed: getMapData()
 * returns MapData in 4.10/5.0/5.1/5.2 and MetaDataHolder2 (setMetaX) in 5.3+.
 *
 * <p>Lives in the {@code src/atakPre53} source set, which the build adds ONLY to the
 * atak410/atak500/atak510/atak520 flavors. The 5.3+ twin lives in {@code src/atak53plus}
 * with the identical fully-qualified name — exactly one is compiled into any APK. This
 * is the per-version override mechanism handling a real class removal.
 */
public final class MockLocationApplier {

    private MockLocationApplier() {}

    public static long apply(MapView view, GeoPoint gp) {
        long t = SystemClock.elapsedRealtime();
        view.getMapData().putDouble("mockLocationSpeed", 20); // m/s
        view.getMapData().putFloat("mockLocationAccuracy", 5f); // meters
        view.getMapData().putString("locationSourcePrefix", "mock");
        view.getMapData().putBoolean("mockLocationAvailable", true);
        view.getMapData().putString("mockLocationSource", "Hello World Plugin");
        view.getMapData().putString("mockLocationSourceColor", "#FFAFFF00");
        view.getMapData().putBoolean("mockLocationCallsignValid", true);
        view.getMapData().putParcelable("mockLocation", gp);
        view.getMapData().putLong("mockLocationTime", t);
        view.getMapData().putLong("mockGPSTime", new CoordinatedTime().getMilliseconds());
        view.getMapData().putInt("mockFixQuality", 2);
        return t;
    }
}
