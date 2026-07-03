package com.atakmap.android.helloworld.features.shape;

/**
 * Plugin DTO for an accuracy ellipse attached to a marker: the ellipse tracks
 * the marker as it moves and is removed with it. Built with the builder;
 * immutable once built.
 */
public final class AccuracyEllipseSpec {

    private final String markerUid;
    private final String name;
    private final double heightMeters;
    private final double widthMeters;
    private final Integer fillColorArgb;
    private final Integer strokeColorArgb;
    private final Double strokeWeight;

    private AccuracyEllipseSpec(Builder b) {
        this.markerUid = b.markerUid;
        this.name = b.name;
        this.heightMeters = b.heightMeters;
        this.widthMeters = b.widthMeters;
        this.fillColorArgb = b.fillColorArgb;
        this.strokeColorArgb = b.strokeColorArgb;
        this.strokeWeight = b.strokeWeight;
    }

    /** An ellipse following the marker with this uid, for the marker's life. */
    public static Builder followingMarker(String markerUid) {
        return new Builder(markerUid);
    }

    public String markerUid() { return markerUid; }
    public String name() { return name; }
    public double heightMeters() { return heightMeters; }
    public double widthMeters() { return widthMeters; }
    public Integer fillColorArgb() { return fillColorArgb; }
    public Integer strokeColorArgb() { return strokeColorArgb; }
    public Double strokeWeight() { return strokeWeight; }

    public static final class Builder {
        private final String markerUid;
        private String name;
        private double heightMeters = 20;
        private double widthMeters = 20;
        private Integer fillColorArgb;
        private Integer strokeColorArgb;
        private Double strokeWeight;

        private Builder(String markerUid) {
            this.markerUid = markerUid;
        }

        /** Display name of the ellipse (its "shape name"). */
        public Builder name(String name) { this.name = name; return this; }

        public Builder dimensionsMeters(double height, double width) {
            this.heightMeters = height;
            this.widthMeters = width;
            return this;
        }

        public Builder fillColorArgb(int argb) { this.fillColorArgb = argb; return this; }
        public Builder strokeColorArgb(int argb) { this.strokeColorArgb = argb; return this; }
        public Builder strokeWeight(double weight) { this.strokeWeight = weight; return this; }

        public AccuracyEllipseSpec build() {
            return new AccuracyEllipseSpec(this);
        }
    }
}
