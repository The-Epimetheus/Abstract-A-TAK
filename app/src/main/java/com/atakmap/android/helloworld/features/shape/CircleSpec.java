package com.atakmap.android.helloworld.features.shape;

import java.util.UUID;

/**
 * Plugin DTO for a drawing circle: a center and a radius. Built with the
 * builder; immutable once built.
 */
public final class CircleSpec {

    private final String uid;
    private final String title;
    private final double centerLatitude;
    private final double centerLongitude;
    private final double radiusMeters;
    private final Integer strokeColorArgb;
    private final Integer fillColorArgb;
    private final Boolean editable;
    private final Boolean archive;

    private CircleSpec(Builder b) {
        this.uid = b.uid != null ? b.uid : UUID.randomUUID().toString();
        this.title = b.title;
        this.centerLatitude = b.centerLatitude;
        this.centerLongitude = b.centerLongitude;
        this.radiusMeters = b.radiusMeters;
        this.strokeColorArgb = b.strokeColorArgb;
        this.fillColorArgb = b.fillColorArgb;
        this.editable = b.editable;
        this.archive = b.archive;
    }

    /** A circle of {@code radiusMeters} centered at the given position. */
    public static Builder centeredAt(double latitude, double longitude,
            double radiusMeters) {
        return new Builder(latitude, longitude, radiusMeters);
    }

    public String uid() { return uid; }
    public String title() { return title; }
    public double centerLatitude() { return centerLatitude; }
    public double centerLongitude() { return centerLongitude; }
    public double radiusMeters() { return radiusMeters; }
    public Integer strokeColorArgb() { return strokeColorArgb; }
    public Integer fillColorArgb() { return fillColorArgb; }
    public Boolean editable() { return editable; }
    public Boolean archive() { return archive; }

    public static final class Builder {
        private String uid;
        private String title;
        private final double centerLatitude;
        private final double centerLongitude;
        private final double radiusMeters;
        private Integer strokeColorArgb;
        private Integer fillColorArgb;
        private Boolean editable;
        private Boolean archive;

        private Builder(double centerLatitude, double centerLongitude,
                double radiusMeters) {
            this.centerLatitude = centerLatitude;
            this.centerLongitude = centerLongitude;
            this.radiusMeters = radiusMeters;
        }

        public Builder uid(String uid) { this.uid = uid; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder strokeColorArgb(int argb) { this.strokeColorArgb = argb; return this; }
        public Builder fillColorArgb(int argb) { this.fillColorArgb = argb; return this; }
        public Builder editable(boolean editable) { this.editable = editable; return this; }
        public Builder archive(boolean archive) { this.archive = archive; return this; }

        public CircleSpec build() {
            return new CircleSpec(this);
        }
    }
}
