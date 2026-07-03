package com.atakmap.android.helloworld.features.camera;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's programmatic map-camera control
 * ({@code CameraController.Programmatic}). Stable across every targeted
 * version today, so this is an insurance seam (see CONTEXT.md): the interface
 * exists so a future camera-API change lands in one impl, never in business
 * logic. Interface in {@code src/main} (ATAK-free); the implementation lives
 * in {@code src/atakShared}.
 */
public interface CameraCreator extends Creator {

    /** Zoom the map camera to the given ground-sample distance (meters/pixel). */
    void zoomTo(double gsd, boolean animate);

    /** Pan the map camera to the given position (decimal degrees). */
    void panTo(double latitude, double longitude, boolean animate);
}
