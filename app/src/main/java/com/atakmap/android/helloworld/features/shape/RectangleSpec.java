package com.atakmap.android.helloworld.features.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Plugin DTO for a drawing rectangle: exactly four corners, walked in order.
 * Built with the builder; immutable once built.
 */
public final class RectangleSpec {

    private final String uid;
    private final String title;
    private final List<double[]> corners;
    private final Integer strokeColorArgb;
    private final Integer fillColorArgb;
    private final boolean basicStyle;
    private final Boolean editable;
    private final Boolean archive;
    private final boolean dispatchAsCot;

    private RectangleSpec(Builder b) {
        if (b.corners.size() != 4)
            throw new IllegalStateException(
                    "a rectangle needs exactly 4 corners, got "
                            + b.corners.size());
        this.uid = b.uid != null ? b.uid : UUID.randomUUID().toString();
        this.title = b.title;
        this.corners = Collections.unmodifiableList(new ArrayList<>(b.corners));
        this.strokeColorArgb = b.strokeColorArgb;
        this.fillColorArgb = b.fillColorArgb;
        this.basicStyle = b.basicStyle;
        this.editable = b.editable;
        this.archive = b.archive;
        this.dispatchAsCot = b.dispatchAsCot;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String uid() { return uid; }
    public String title() { return title; }
    /** Four {latitude, longitude} corners, in walk order. */
    public List<double[]> corners() { return corners; }
    public Integer strokeColorArgb() { return strokeColorArgb; }
    public Integer fillColorArgb() { return fillColorArgb; }
    public boolean basicStyle() { return basicStyle; }
    public Boolean editable() { return editable; }
    public Boolean archive() { return archive; }
    public boolean dispatchAsCot() { return dispatchAsCot; }

    public static final class Builder {
        private String uid;
        private String title;
        private final List<double[]> corners = new ArrayList<>();
        private Integer strokeColorArgb;
        private Integer fillColorArgb;
        private boolean basicStyle;
        private Boolean editable;
        private Boolean archive;
        private boolean dispatchAsCot;

        private Builder() {
        }

        public Builder uid(String uid) { this.uid = uid; return this; }
        public Builder title(String title) { this.title = title; return this; }

        /** Append one corner (decimal degrees); call exactly four times. */
        public Builder corner(double latitude, double longitude) {
            corners.add(new double[] { latitude, longitude });
            return this;
        }

        public Builder strokeColorArgb(int argb) { this.strokeColorArgb = argb; return this; }
        public Builder fillColorArgb(int argb) { this.fillColorArgb = argb; return this; }

        /** Strip ATAK's default style flags (plain outline, no decorations). */
        public Builder basicStyle() { this.basicStyle = true; return this; }

        public Builder editable(boolean editable) { this.editable = editable; return this; }
        public Builder archive(boolean archive) { this.archive = archive; return this; }

        /** After placing, dispatch the rectangle externally as a CoT event. */
        public Builder dispatchAsCot() { this.dispatchAsCot = true; return this; }

        public RectangleSpec build() {
            return new RectangleSpec(this);
        }
    }
}
