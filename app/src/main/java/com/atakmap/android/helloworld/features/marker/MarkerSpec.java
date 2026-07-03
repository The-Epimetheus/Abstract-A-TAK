package com.atakmap.android.helloworld.features.marker;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Plugin DTO describing one marker to place — everything the impl needs to
 * reproduce the demo placements without an ATAK type crossing the boundary.
 * Built with the builder; immutable once built.
 */
public final class MarkerSpec {

    private final boolean atMapCenter;
    private final double latitude;
    private final double longitude;
    private final String uid;
    private final String type;
    private final String callsign;
    private final String title;
    private final boolean dropAsIfByUser;
    private final boolean showCotDetails;
    private final boolean neverPersist;
    private final Boolean archive;
    private final boolean fullRotation;
    private final boolean rotateWithHeading;
    private final Long autoStaleMillis;
    private final boolean dispatchAsCot;
    private final Double trackHeading;
    private final Double trackSpeed;
    private final Integer colorArgb;
    private final String iconsetPath;
    private final String iconUri;
    private final boolean alwaysShowText;
    private final boolean clickable;
    private final String menu;
    private final String[] groupPath;
    private final boolean persistAndAnnounce;
    private final boolean persist;
    private final boolean showLabel;
    private final Map<String, Boolean> metaBooleans;
    private final Map<String, String> metaStrings;
    private final Map<String, Long> metaLongs;
    private final Map<String, Double> metaDoubles;

    private MarkerSpec(Builder b) {
        this.atMapCenter = b.atMapCenter;
        this.latitude = b.latitude;
        this.longitude = b.longitude;
        this.uid = b.uid != null ? b.uid : UUID.randomUUID().toString();
        this.type = b.type;
        this.callsign = b.callsign;
        this.title = b.title;
        this.dropAsIfByUser = b.dropAsIfByUser;
        this.showCotDetails = b.showCotDetails;
        this.neverPersist = b.neverPersist;
        this.archive = b.archive;
        this.fullRotation = b.fullRotation;
        this.rotateWithHeading = b.rotateWithHeading;
        this.autoStaleMillis = b.autoStaleMillis;
        this.dispatchAsCot = b.dispatchAsCot;
        this.trackHeading = b.trackHeading;
        this.trackSpeed = b.trackSpeed;
        this.colorArgb = b.colorArgb;
        this.iconsetPath = b.iconsetPath;
        this.iconUri = b.iconUri;
        this.alwaysShowText = b.alwaysShowText;
        this.clickable = b.clickable;
        this.menu = b.menu;
        this.groupPath = b.groupPath;
        this.persistAndAnnounce = b.persistAndAnnounce;
        this.persist = b.persist;
        this.showLabel = b.showLabel;
        this.metaBooleans = Collections
                .unmodifiableMap(new LinkedHashMap<>(b.metaBooleans));
        this.metaStrings = Collections
                .unmodifiableMap(new LinkedHashMap<>(b.metaStrings));
        this.metaLongs = Collections
                .unmodifiableMap(new LinkedHashMap<>(b.metaLongs));
        this.metaDoubles = Collections
                .unmodifiableMap(new LinkedHashMap<>(b.metaDoubles));
    }

    /** Place at the map center (the impl resolves it, elevation included). */
    public static Builder atMapCenter() {
        return new Builder(true, 0, 0);
    }

    /** Place at an explicit position (decimal degrees). */
    public static Builder at(double latitude, double longitude) {
        return new Builder(false, latitude, longitude);
    }

    public boolean isAtMapCenter() { return atMapCenter; }
    public double latitude() { return latitude; }
    public double longitude() { return longitude; }
    public String uid() { return uid; }
    public String type() { return type; }
    public String callsign() { return callsign; }
    public String title() { return title; }
    public boolean dropAsIfByUser() { return dropAsIfByUser; }
    public boolean showCotDetails() { return showCotDetails; }
    public boolean neverPersist() { return neverPersist; }
    public Boolean archive() { return archive; }
    public boolean fullRotation() { return fullRotation; }
    public boolean rotateWithHeading() { return rotateWithHeading; }
    public Long autoStaleMillis() { return autoStaleMillis; }
    public boolean dispatchAsCot() { return dispatchAsCot; }
    public Double trackHeading() { return trackHeading; }
    public Double trackSpeed() { return trackSpeed; }
    public Integer colorArgb() { return colorArgb; }
    public String iconsetPath() { return iconsetPath; }
    public String iconUri() { return iconUri; }
    public boolean alwaysShowText() { return alwaysShowText; }
    public boolean clickable() { return clickable; }
    public String menu() { return menu; }
    public String[] groupPath() { return groupPath; }
    public boolean persistAndAnnounce() { return persistAndAnnounce; }
    public boolean persist() { return persist; }
    public boolean showLabel() { return showLabel; }
    public Map<String, Boolean> metaBooleans() { return metaBooleans; }
    public Map<String, String> metaStrings() { return metaStrings; }
    public Map<String, Long> metaLongs() { return metaLongs; }
    public Map<String, Double> metaDoubles() { return metaDoubles; }

    public static final class Builder {
        private final boolean atMapCenter;
        private final double latitude;
        private final double longitude;
        private String uid;
        private String type;
        private String callsign;
        private String title;
        private boolean dropAsIfByUser;
        private boolean showCotDetails;
        private boolean neverPersist;
        private Boolean archive;
        private boolean fullRotation;
        private boolean rotateWithHeading;
        private Long autoStaleMillis;
        private boolean dispatchAsCot;
        private Double trackHeading;
        private Double trackSpeed;
        private Integer colorArgb;
        private String iconsetPath;
        private String iconUri;
        private boolean alwaysShowText;
        private boolean clickable;
        private String menu;
        private String[] groupPath;
        private boolean persistAndAnnounce;
        private boolean persist;
        private boolean showLabel;
        private final Map<String, Boolean> metaBooleans = new LinkedHashMap<>();
        private final Map<String, String> metaStrings = new LinkedHashMap<>();
        private final Map<String, Long> metaLongs = new LinkedHashMap<>();
        private final Map<String, Double> metaDoubles = new LinkedHashMap<>();

        private Builder(boolean atMapCenter, double latitude,
                double longitude) {
            this.atMapCenter = atMapCenter;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Builder uid(String uid) { this.uid = uid; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder callsign(String callsign) { this.callsign = callsign; return this; }
        public Builder title(String title) { this.title = title; return this; }

        /**
         * Place it the way a user drop would (ATAK's point tool semantics:
         * details page toggle, persistence handling) instead of a raw
         * programmatic add.
         */
        public Builder dropAsIfByUser() { this.dropAsIfByUser = true; return this; }

        public Builder showCotDetails(boolean show) { this.showCotDetails = show; return this; }
        public Builder neverPersist(boolean never) { this.neverPersist = never; return this; }

        /** CoT is created but marked (not) persisting; point-tool drops only. */
        public Builder archive(boolean archive) { this.archive = archive; return this; }

        /** Allow full rotation of the marker icon (heading, no arrow). */
        public Builder fullRotation() { this.fullRotation = true; return this; }

        /** Rotate the icon with its heading (keeps the arrow style). */
        public Builder rotateWithHeading() { this.rotateWithHeading = true; return this; }

        /** Stale the marker out automatically this long after placement. */
        public Builder autoStaleAfterMillis(long millis) { this.autoStaleMillis = millis; return this; }

        /** After placing, dispatch the marker externally as a CoT event. */
        public Builder dispatchAsCot() { this.dispatchAsCot = true; return this; }

        public Builder track(double heading, double speed) {
            this.trackHeading = heading;
            this.trackSpeed = speed;
            return this;
        }

        public Builder colorArgb(int argb) { this.colorArgb = argb; return this; }
        public Builder iconsetPath(String path) { this.iconsetPath = path; return this; }

        /** Icon image URI (e.g. {@code base64://…} or {@code asset://…}). */
        public Builder iconUri(String uri) { this.iconUri = uri; return this; }

        public Builder alwaysShowText() { this.alwaysShowText = true; return this; }
        public Builder clickable() { this.clickable = true; return this; }

        /**
         * Custom radial menu: either an inlined definition (see
         * MenuCreator.loadRadialMenu) or a path to a menu XML ATAK can
         * resolve itself (e.g. {@code "menus/a-n.xml"}).
         */
        public Builder menu(String menu) { this.menu = menu; return this; }

        /** Map-group path to add into (e.g. "Cursor on Target", "Friendly"); root if unset. */
        public Builder groupPath(String... path) { this.groupPath = path; return this; }

        /** Persist the marker and announce its placement to ATAK. */
        public Builder persistAndAnnounce() { this.persistAndAnnounce = true; return this; }

        /** Persist the marker quietly (no placement announcement). */
        public Builder persist() { this.persist = true; return this; }

        /** Always render the marker's label. */
        public Builder showLabel() { this.showLabel = true; return this; }

        public Builder meta(String key, boolean value) { metaBooleans.put(key, value); return this; }
        public Builder meta(String key, String value) { metaStrings.put(key, value); return this; }
        public Builder meta(String key, long value) { metaLongs.put(key, value); return this; }
        public Builder meta(String key, double value) { metaDoubles.put(key, value); return this; }

        public MarkerSpec build() {
            return new MarkerSpec(this);
        }
    }
}
