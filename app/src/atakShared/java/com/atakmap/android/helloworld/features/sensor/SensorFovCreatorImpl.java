package com.atakmap.android.helloworld.features.sensor;

import com.atakmap.android.cot.detail.SensorDetailHandler;
import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.SensorFOV;
import com.atakmap.android.user.PlacePointTool;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

/**
 * The only place ATAK's sensor field-of-view plumbing is touched
 * ({@code SensorFOV}, {@code SensorDetailHandler}, the point tool). Source-
 * stable across all targeted versions → shared impl source set.
 *
 * <p>ATAK convention this impl leans on: {@code SensorDetailHandler.addFovToMap}
 * registers the cone as its own map item named {@code <cameraUid>-fov}, so the
 * cone is found (and restyled) through that uid, never retained.
 */
public final class SensorFovCreatorImpl implements SensorFovCreator {

    /** Reserved test-artifact namespace (see CONTEXT.md, load-time systems check). */
    private static final String TEST_UID = "com.atakmap.android.helloworld.test.SYSTEMS_CHECK_SENSOR";

    /** SensorDetailHandler names the cone map item {@code <cameraUid>-fov}. */
    private static final String FOV_SUFFIX = "-fov";

    @Override
    public String id() {
        return "SensorFovCreator";
    }

    @Override
    public boolean fovExists(String cameraUid) {
        MapView mv = MapView.getMapView();
        return mv != null
                && mv.getMapItem(cameraUid + FOV_SUFFIX) instanceof SensorFOV;
    }

    @Override
    public void createOrUpdateSensorFov(SensorFovSpec spec) {
        MapView mapView = MapView.getMapView();
        if (mapView == null)
            return;
        final GeoPointMetaData point = mapView.getCenterPoint();

        MapItem mi = mapView.getMapItem(spec.cameraUid());
        if (mi == null) {
            PlacePointTool.MarkerCreator markerCreator = new PlacePointTool.MarkerCreator(
                    point);
            markerCreator.setUid(spec.cameraUid());
            //this settings automatically pops open to CotDetails page after dropping the marker
            markerCreator.showCotDetails(false);
            //this settings determines if a CoT persists or not.
            markerCreator.setArchive(true);
            //this is the type of the marker.  Could be set to a known 2525B value or custom
            if (spec.markerType() != null)
                markerCreator.setType(spec.markerType());
            //this shows under the marker
            if (spec.callsign() != null)
                markerCreator.setCallsign(spec.callsign());
            //this also determines if the marker persists or not??
            markerCreator.setNeverPersist(false);
            mi = markerCreator.placePoint();
        }
        // blind cast, ensure this is really a marker.
        Marker camera = (Marker) mi;
        // Recenters an existing camera to the current map center; a no-op
        // right after a fresh drop (which already placed it there).
        camera.setPoint(point);

        // ATAK's cone color API wants the channels as 0..1 floats, split out
        // of the ARGB int here so the seam only carries the int.
        float r = ((0x00FF0000 & spec.colorArgb()) >> 16) / 256f;
        float g = ((0x0000FF00 & spec.colorArgb()) >> 8) / 256f;
        float b = ((0x000000FF & spec.colorArgb()) >> 0) / 256f;

        mi = mapView.getMapItem(camera.getUID() + FOV_SUFFIX);
        if (mi instanceof SensorFOV) {
            SensorFOV sFov = (SensorFOV) mi;
            sFov.setColor(spec.colorArgb()); // currently broken
            sFov.setColor(r, g, b);
            sFov.setMetrics(spec.azimuthDeg(), spec.fovDeg(),
                    spec.rangeMeters());
        } else { // use this case
            SensorDetailHandler.addFovToMap(camera, spec.azimuthDeg(),
                    spec.fovDeg(), spec.rangeMeters(), new float[] {
                            r, g, b, spec.coneAlpha()
                    }, true);
        }
    }

    /**
     * Runs the real cone path under the reserved test namespace: a probe
     * camera at (0,0) (kept out of the overlay list, not archived), the cone
     * added through {@code addFovToMap}, then both removed in a finally. FULL
     * only when the cone actually appeared under the {@code -fov} uid
     * convention; PARTIAL when the call resolved but the convention did not
     * hold (nothing found to restyle or tear down).
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "sensor FOV path threw", () -> {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            Marker probe = null;
            try {
                probe = new Marker(
                        GeoPointMetaData.wrap(new GeoPoint(0, 0)), TEST_UID);
                probe.setType("b-m-p-s-p-loc");
                probe.setMetaBoolean("addToObjList", false);
                probe.setMetaBoolean("archive", false);
                mv.getRootGroup().addItem(probe);
                SensorDetailHandler.addFovToMap(probe, 90, 70, 400,
                        new float[] {
                                1f, 0f, 0f, 90
                        }, true);
                if (!(mv.getMapItem(
                        TEST_UID + FOV_SUFFIX) instanceof SensorFOV))
                    return SelfCheckResult.partial(id(),
                            "addFovToMap resolved but no cone appeared under the -fov uid convention");
                return SelfCheckResult.full(id(),
                        "added + removed a test sensor FOV cone (not persisted)");
            } finally {
                MapItem cone = mv.getMapItem(TEST_UID + FOV_SUFFIX);
                if (cone != null && cone.getGroup() != null)
                    cone.removeFromGroup();
                if (probe != null && probe.getGroup() != null)
                    probe.removeFromGroup();
            }
        });
    }
}
