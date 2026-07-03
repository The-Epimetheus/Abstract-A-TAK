package com.atakmap.android.helloworld.features.sensor;

/**
 * Plugin DTO describing one sensor camera + FOV cone — everything the impl
 * needs to reproduce the demo without an ATAK type crossing the boundary.
 * Built with the builder; immutable once built.
 */
public final class SensorFovSpec {

    private final String cameraUid;
    private final String callsign;
    private final String markerType;
    private final int colorArgb;
    private final int azimuthDeg;
    private final int fovDeg;
    private final int rangeMeters;
    private final float coneAlpha;

    private SensorFovSpec(Builder b) {
        this.cameraUid = b.cameraUid;
        this.callsign = b.callsign;
        this.markerType = b.markerType;
        this.colorArgb = b.colorArgb;
        this.azimuthDeg = b.azimuthDeg;
        this.fovDeg = b.fovDeg;
        this.rangeMeters = b.rangeMeters;
        this.coneAlpha = b.coneAlpha;
    }

    /** Start a spec for the camera marker with this uid. */
    public static Builder forCamera(String cameraUid) {
        return new Builder(cameraUid);
    }

    public String cameraUid() { return cameraUid; }
    public String callsign() { return callsign; }
    public String markerType() { return markerType; }
    public int colorArgb() { return colorArgb; }
    public int azimuthDeg() { return azimuthDeg; }
    public int fovDeg() { return fovDeg; }
    public int rangeMeters() { return rangeMeters; }
    public float coneAlpha() { return coneAlpha; }

    public static final class Builder {
        private final String cameraUid;
        private String callsign;
        private String markerType;
        private int colorArgb;
        private int azimuthDeg;
        private int fovDeg;
        private int rangeMeters;
        private float coneAlpha;

        private Builder(String cameraUid) {
            this.cameraUid = cameraUid;
        }

        /** Shown under the camera marker. */
        public Builder callsign(String callsign) {
            this.callsign = callsign;
            return this;
        }

        /** CoT type of the camera marker (2525B or custom). */
        public Builder markerType(String markerType) {
            this.markerType = markerType;
            return this;
        }

        /** Cone color; the impl splits it into the RGB floats ATAK wants. */
        public Builder colorArgb(int argb) {
            this.colorArgb = argb;
            return this;
        }

        /** Cone direction, width and reach. */
        public Builder metrics(int azimuthDeg, int fovDeg, int rangeMeters) {
            this.azimuthDeg = azimuthDeg;
            this.fovDeg = fovDeg;
            this.rangeMeters = rangeMeters;
            return this;
        }

        /**
         * Fourth channel of the cone's color array. The legacy demo passed 90
         * here even though the other channels are 0..1 floats — preserved
         * verbatim; the renderer tolerates it.
         */
        public Builder coneAlpha(float coneAlpha) {
            this.coneAlpha = coneAlpha;
            return this;
        }

        public SensorFovSpec build() {
            return new SensorFovSpec(this);
        }
    }
}
