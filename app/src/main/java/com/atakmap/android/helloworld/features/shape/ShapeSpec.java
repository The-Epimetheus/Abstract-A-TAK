package com.atakmap.android.helloworld.features.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Plugin DTO describing one freeform drawing shape (open polyline or closed
 * polygon) — everything the impl needs without an ATAK type crossing the
 * boundary. Built with the builder; immutable once built.
 */
public final class ShapeSpec {

    /** Plugin-owned mirror of the renderer's altitude interpretation. */
    public enum AltitudeMode {
        /** Renderer default. */
        DEFAULT,
        /** Altitudes are absolute (HAE metres). */
        ABSOLUTE,
        /** Altitudes are ignored; the shape drapes on the terrain. */
        CLAMP_TO_GROUND
    }

    private final String uid;
    private final String title;
    private final List<double[]> points;
    private final boolean closed;
    private final Integer strokeColorArgb;
    private final Integer fillColorArgb;
    private final Double heightMeters;
    private final Boolean movable;
    private final boolean clickable;
    private final Boolean editable;
    private final Boolean archive;
    private final AltitudeMode altitudeMode;
    private final String[] groupPath;
    private final Map<String, Boolean> metaBooleans;

    private ShapeSpec(Builder b) {
        this.uid = b.uid != null ? b.uid : UUID.randomUUID().toString();
        this.title = b.title;
        this.points = Collections.unmodifiableList(new ArrayList<>(b.points));
        this.closed = b.closed;
        this.strokeColorArgb = b.strokeColorArgb;
        this.fillColorArgb = b.fillColorArgb;
        this.heightMeters = b.heightMeters;
        this.movable = b.movable;
        this.clickable = b.clickable;
        this.editable = b.editable;
        this.archive = b.archive;
        this.altitudeMode = b.altitudeMode;
        this.groupPath = b.groupPath;
        this.metaBooleans = Collections
                .unmodifiableMap(new LinkedHashMap<>(b.metaBooleans));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String uid() { return uid; }
    public String title() { return title; }
    /** Each entry is {latitude, longitude, altitudeMeters (NaN if unset)}. */
    public List<double[]> points() { return points; }
    public boolean closed() { return closed; }
    public Integer strokeColorArgb() { return strokeColorArgb; }
    public Integer fillColorArgb() { return fillColorArgb; }
    public Double heightMeters() { return heightMeters; }
    public Boolean movable() { return movable; }
    public boolean clickable() { return clickable; }
    public Boolean editable() { return editable; }
    public Boolean archive() { return archive; }
    public AltitudeMode altitudeMode() { return altitudeMode; }
    public String[] groupPath() { return groupPath; }
    public Map<String, Boolean> metaBooleans() { return metaBooleans; }

    public static final class Builder {
        private String uid;
        private String title;
        private final List<double[]> points = new ArrayList<>();
        private boolean closed;
        private Integer strokeColorArgb;
        private Integer fillColorArgb;
        private Double heightMeters;
        private Boolean movable;
        private boolean clickable;
        private Boolean editable;
        private Boolean archive;
        private AltitudeMode altitudeMode = AltitudeMode.DEFAULT;
        private String[] groupPath;
        private final Map<String, Boolean> metaBooleans = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder uid(String uid) { this.uid = uid; return this; }
        public Builder title(String title) { this.title = title; return this; }

        /** Append a vertex (decimal degrees). */
        public Builder point(double latitude, double longitude) {
            points.add(new double[] { latitude, longitude, Double.NaN });
            return this;
        }

        /** Append a vertex with an altitude (metres; see altitudeMode). */
        public Builder point(double latitude, double longitude,
                double altitudeMeters) {
            points.add(new double[] { latitude, longitude, altitudeMeters });
            return this;
        }

        /** Close the shape into a polygon (fillable). */
        public Builder closed() { this.closed = true; return this; }

        public Builder strokeColorArgb(int argb) { this.strokeColorArgb = argb; return this; }
        public Builder fillColorArgb(int argb) { this.fillColorArgb = argb; return this; }

        /** Extrude the shape this high (metres) for 3D rendering. */
        public Builder heightMeters(double meters) { this.heightMeters = meters; return this; }

        public Builder movable(boolean movable) { this.movable = movable; return this; }
        public Builder clickable() { this.clickable = true; return this; }

        /** Whether the user may enter edit mode on it (a capability, not a mode switch). */
        public Builder editable(boolean editable) { this.editable = editable; return this; }

        /** Whether the shape's CoT is archived (persisted across restarts). */
        public Builder archive(boolean archive) { this.archive = archive; return this; }

        public Builder altitudeMode(AltitudeMode mode) { this.altitudeMode = mode; return this; }

        /** Map-group path to add into; the drawing group if unset. */
        public Builder groupPath(String... path) { this.groupPath = path; return this; }

        public Builder meta(String key, boolean value) { metaBooleans.put(key, value); return this; }

        public ShapeSpec build() {
            return new ShapeSpec(this);
        }
    }
}
