package com.atakmap.android.helloworld.features.shape;

import java.util.UUID;

/**
 * Plugin DTO for a drawing ellipse: a center, a length/width footprint and a
 * rotation angle. Built with the builder; immutable once built.
 */
public final class EllipseSpec {

    private final String uid;
    private final String title;
    private final double centerLatitude;
    private final double centerLongitude;
    private final double lengthMeters;
    private final double widthMeters;
    private final double angleDegrees;
    private final Integer strokeColorArgb;
    private final Integer fillColorArgb;
    private final Boolean editable;
    private final Boolean archive;

    private EllipseSpec(Builder b) {
        this.uid = b.uid != null ? b.uid : UUID.randomUUID().toString();
        this.title = b.title;
        this.centerLatitude = b.centerLatitude;
        this.centerLongitude = b.centerLongitude;
        this.lengthMeters = b.lengthMeters;
        this.widthMeters = b.widthMeters;
        this.angleDegrees = b.angleDegrees;
        this.strokeColorArgb = b.strokeColorArgb;
        this.fillColorArgb = b.fillColorArgb;
        this.editable = b.editable;
        this.archive = b.archive;
    }

    /** An ellipse centered at the given position (decimal degrees). */
    public static Builder centeredAt(double latitude, double longitude) {
        return new Builder(latitude, longitude);
    }

    public String uid() { return uid; }
    public String title() { return title; }
    public double centerLatitude() { return centerLatitude; }
    public double centerLongitude() { return centerLongitude; }
    public double lengthMeters() { return lengthMeters; }
    public double widthMeters() { return widthMeters; }
    public double angleDegrees() { return angleDegrees; }
    public Integer strokeColorArgb() { return strokeColorArgb; }
    public Integer fillColorArgb() { return fillColorArgb; }
    public Boolean editable() { return editable; }
    public Boolean archive() { return archive; }

    public static final class Builder {
        private String uid;
        private String title;
        private final double centerLatitude;
        private final double centerLongitude;
        private double lengthMeters;
        private double widthMeters;
        private double angleDegrees;
        private Integer strokeColorArgb;
        private Integer fillColorArgb;
        private Boolean editable;
        private Boolean archive;

        private Builder(double centerLatitude, double centerLongitude) {
            this.centerLatitude = centerLatitude;
            this.centerLongitude = centerLongitude;
        }

        public Builder uid(String uid) { this.uid = uid; return this; }
        public Builder title(String title) { this.title = title; return this; }

        /** Footprint: long axis and short axis, in metres. */
        public Builder dimensionsMeters(double length, double width) {
            this.lengthMeters = length;
            this.widthMeters = width;
            return this;
        }

        /** Rotation of the long axis, degrees clockwise from north. */
        public Builder angleDegrees(double degrees) { this.angleDegrees = degrees; return this; }

        public Builder strokeColorArgb(int argb) { this.strokeColorArgb = argb; return this; }
        public Builder fillColorArgb(int argb) { this.fillColorArgb = argb; return this; }
        public Builder editable(boolean editable) { this.editable = editable; return this; }
        public Builder archive(boolean archive) { this.archive = archive; return this; }

        public EllipseSpec build() {
            return new EllipseSpec(this);
        }
    }
}
