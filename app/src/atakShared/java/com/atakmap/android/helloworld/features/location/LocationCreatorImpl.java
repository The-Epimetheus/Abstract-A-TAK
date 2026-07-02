package com.atakmap.android.helloworld.features.location;

import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.compat.MockLocationApplier;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;

/**
 * The only place the mock-GPS metadata store is touched. The version-diverging
 * writes ({@code MapData.putX} &le;5.2 vs {@code MetaDataHolder2.setMetaX} 5.3+)
 * live in the banded internal {@link MockLocationApplier}
 * (src/bands/atakPre53 / src/bands/atak53plus); this impl adds the stable half — the
 * GPS-received broadcast — and stays in {@code src/atakShared}.
 */
public final class LocationCreatorImpl implements LocationCreator {

    @Override
    public String id() {
        return "LocationCreator";
    }

    @Override
    public long simulateSelfLocation(double latitude, double longitude) {
        MapView mv = MapView.getMapView();
        if (mv == null || mv.getSelfMarker() == null)
            return -1;

        long mockTime = MockLocationApplier.apply(mv,
                new GeoPoint(latitude, longitude));

        Intent gpsReceived = new Intent();
        gpsReceived.setAction("com.atakmap.android.map.WR_GPS_RECEIVED");
        AtakBroadcast.getInstance().sendBroadcast(gpsReceived);
        return mockTime;
    }

    /**
     * Feeding a mock fix for real would move the self marker — irreversible from
     * the user's point of view — so this degrades: it executes the BANDED metadata
     * API (write + read + remove under a reserved probe key) without sending the
     * GPS broadcast. A wrong band binding still fails here at load.
     */
    @Override
    public SelfCheckResult selfCheck() {
        try {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            String api = MockLocationApplier.probeMetadataApi(mv);
            return SelfCheckResult.partial(id(),
                    api + " exercised; GPS broadcast not sent (would move the self marker)");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "banded metadata API threw", t);
        }
    }
}
