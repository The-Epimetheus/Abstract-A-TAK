package com.atakmap.android.helloworld.features.camera;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.map.CameraController;

/**
 * The only place ATAK's {@code CameraController.Programmatic} is touched.
 * Source-stable across all targeted versions → shared impl source set.
 */
public final class CameraCreatorImpl implements CameraCreator {

    @Override
    public String id() {
        return "CameraCreator";
    }

    @Override
    public void zoomTo(double gsd, boolean animate) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        CameraController.Programmatic.zoomTo(mv.getRenderer3(), gsd, animate);
    }

    @Override
    public void panTo(double latitude, double longitude, boolean animate) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        CameraController.Programmatic.panTo(mv.getRenderer3(),
                new GeoPoint(latitude, longitude), animate);
    }

    /**
     * Pans the camera to where it already is: the real dispatch path runs
     * (FULL) with no visible movement — invisible to the user, nothing to
     * tear down.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "camera control path threw", () -> {
            MapView mv = MapView.getMapView();
            if (mv == null || mv.getRenderer3() == null)
                return SelfCheckResult.skipped(id(), "map renderer not ready");
            GeoPoint here = mv.getCenterPoint().get();
            CameraController.Programmatic.panTo(mv.getRenderer3(), here, false);
            return SelfCheckResult.full(id(),
                    "panned to the current center (no visible change)");
        });
    }
}
