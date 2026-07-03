package com.atakmap.android.helloworld.features.sensor;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's sensor field-of-view plumbing ({@code SensorFOV},
 * {@code SensorDetailHandler}, the point tool that drops the camera marker).
 * One deep method does the whole camera-plus-cone dance so the ATAK objects
 * never cross the seam; the {@link SensorFovSpec} carries everything the impl
 * needs. Interface in {@code src/main}; the implementation lives in
 * {@code src/atakShared}.
 */
public interface SensorFovCreator extends Creator {

    /**
     * Whether a FOV cone already hangs off the camera with this uid. ATAK's
     * {@code SensorDetailHandler} names the cone map item
     * {@code <cameraUid>-fov}; this checks that the item exists and really is
     * a sensor FOV.
     */
    boolean fovExists(String cameraUid);

    /**
     * Create — or restyle — the sensor camera and its FOV cone as described by
     * {@code spec}. Seam contract (the legacy demo's exact dance):
     * <ul>
     *   <li>camera marker absent → drop it at the map center through the point
     *       tool (archived, no CoT-details popup);</li>
     *   <li>camera marker present → recenter it to the current map center;</li>
     *   <li>cone absent → add it with the spec's metrics, color and alpha;</li>
     *   <li>cone present → recolor it and apply the spec's metrics.</li>
     * </ul>
     * No-op when the map is not ready.
     */
    void createOrUpdateSensorFov(SensorFovSpec spec);
}
