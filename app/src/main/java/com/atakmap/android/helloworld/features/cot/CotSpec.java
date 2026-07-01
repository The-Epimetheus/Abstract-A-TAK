package com.atakmap.android.helloworld.features.cot;

/**
 * Plugin DTO describing a Cursor-on-Target event by value, so business logic can
 * ask for a CoT event without touching any ATAK type. The {@link CotCreator} impl
 * maps this to ATAK's {@code CotEvent}/{@code CotPoint}/{@code CotDetail}.
 *
 * <p>ATAK-free (only {@code java.*}); lives in {@code src/main}. Immutable value
 * type — built via {@link Builder} (plain Java 8, no records).
 */
public final class CotSpec {

    private final String uid;
    private final String type;      // CoT type, e.g. "a-f-G" (friendly ground)
    private final String callsign;  // may be null
    private final double lat;
    private final double lon;
    private final double hae;        // height above ellipsoid, meters (or CotPoint.UNKNOWN)
    private final double ce;         // circular error, meters
    private final double le;         // linear error, meters
    private final String how;        // e.g. "h-g-i-g-o" (human)
    private final int staleMinutes;  // event validity window from now

    private CotSpec(Builder b) {
        this.uid = b.uid;
        this.type = b.type;
        this.callsign = b.callsign;
        this.lat = b.lat;
        this.lon = b.lon;
        this.hae = b.hae;
        this.ce = b.ce;
        this.le = b.le;
        this.how = b.how;
        this.staleMinutes = b.staleMinutes;
    }

    public String uid() { return uid; }
    public String type() { return type; }
    public String callsign() { return callsign; }
    public double lat() { return lat; }
    public double lon() { return lon; }
    public double hae() { return hae; }
    public double ce() { return ce; }
    public double le() { return le; }
    public String how() { return how; }
    public int staleMinutes() { return staleMinutes; }

    public static Builder builder(String uid, String type, double lat, double lon) {
        return new Builder(uid, type, lat, lon);
    }

    public static final class Builder {
        private final String uid;
        private final String type;
        private final double lat;
        private final double lon;
        private String callsign = null;
        private double hae = 9999999.0; // CotPoint.UNKNOWN sentinel; impl maps it
        private double ce = 9999999.0;
        private double le = 9999999.0;
        private String how = "h-g-i-g-o";
        private int staleMinutes = 5;

        private Builder(String uid, String type, double lat, double lon) {
            this.uid = uid;
            this.type = type;
            this.lat = lat;
            this.lon = lon;
        }

        public Builder callsign(String v) { this.callsign = v; return this; }
        public Builder hae(double v) { this.hae = v; return this; }
        public Builder ce(double v) { this.ce = v; return this; }
        public Builder le(double v) { this.le = v; return this; }
        public Builder how(String v) { this.how = v; return this; }
        public Builder staleMinutes(int v) { this.staleMinutes = v; return this; }

        public CotSpec build() { return new CotSpec(Builder.this); }
    }
}
