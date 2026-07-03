package com.atakmap.android.helloworld.features.sensor;

/**
 * The sensor-FOV feature's Controller: the plot-a-sensor-field-of-view demo,
 * ATAK-free, depending only on {@link SensorFovCreator}. The Pane controller
 * forwards the tap here (every feature gets a Controller — ADR-0005).
 */
public class SensorFovController {

    /** Fixed demo uid so re-taps modify the one camera instead of stacking new ones. */
    private static final String CAMERA_UID = "sensor-fov-example-uid";

    private static final int RED = 0xFFFF0000;

    private final SensorFovCreator sensorFovCreator;

    public SensorFovController(SensorFovCreator sensorFovCreator) {
        this.sensorFovCreator = sensorFovCreator;
    }

    /**
     * First tap drops a "Sensor" camera marker at the map center with a red
     * 90°-wide, 70°-tall, 400 m FOV cone; later taps recenter the camera and
     * randomize the cone so the modification is visible (legacy demo
     * behavior, preserved exactly).
     */
    public void createOrModifySensorFov() {
        boolean modify = sensorFovCreator.fovExists(CAMERA_UID);
        // On modify the legacy demo randomized the first two metrics (range
        // stays 400 m) so each re-tap visibly reshapes the existing cone.
        int azimuth = modify ? (int) (90 * Math.random()) : 90;
        int fov = modify ? (int) (70 * Math.random()) : 70;
        sensorFovCreator.createOrUpdateSensorFov(
                SensorFovSpec.forCamera(CAMERA_UID)
                        .callsign("Sensor")
                        .markerType("b-m-p-s-p-loc")
                        .colorArgb(RED)
                        .metrics(azimuth, fov, 400)
                        .coneAlpha(90)
                        .build());
    }
}
