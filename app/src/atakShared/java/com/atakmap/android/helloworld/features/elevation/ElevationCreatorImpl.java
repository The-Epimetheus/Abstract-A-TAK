package com.atakmap.android.helloworld.features.elevation;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;
import com.atakmap.map.elevation.ElevationData;
import com.atakmap.map.elevation.ElevationManager;

/**
 * The only place {@code ElevationManager} is touched. Source-stable across
 * all targeted versions → shared impl source set.
 */
public final class ElevationCreatorImpl implements ElevationCreator {

    @Override
    public String id() {
        return "ElevationCreator";
    }

    @Override
    public ElevationSample sampleAtMapCenter() {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return null;
        GeoPoint point = mv.getCenterPoint().get();
        if (point == null)
            return null;
        return sampleAt(point.getLatitude(), point.getLongitude());
    }

    private static ElevationSample sampleAt(double lat, double lon) {
        // Same query, two model filters: MODEL_TERRAIN is bare earth,
        // MODEL_SURFACE includes buildings/canopy where a DSM is loaded.
        ElevationManager.QueryParameters dtmFilter = new ElevationManager.QueryParameters();
        dtmFilter.elevationModel = ElevationData.MODEL_TERRAIN;
        ElevationManager.QueryParameters dsmFilter = new ElevationManager.QueryParameters();
        dsmFilter.elevationModel = ElevationData.MODEL_SURFACE;

        double terrain = ElevationManager.getElevation(lat, lon, dtmFilter,
                new GeoPointMetaData());
        double surface = ElevationManager.getElevation(lat, lon, dsmFilter,
                new GeoPointMetaData());
        return new ElevationSample(terrain, surface);
    }

    /**
     * FULL: runs a real terrain + surface query at the map center. The probe
     * verifies the query path, not coverage — {@code NaN} results are legal
     * on a host with no elevation data loaded.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "elevation query threw", () -> {
            ElevationSample sample = sampleAtMapCenter();
            if (sample == null)
                return SelfCheckResult.skipped(id(),
                        "MapView/map center not ready");
            return SelfCheckResult.full(id(),
                    "queried terrain + surface at map center"
                            + " (NaN is legal without elevation coverage)");
        });
    }
}
