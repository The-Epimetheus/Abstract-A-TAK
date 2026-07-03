package com.atakmap.android.helloworld.features.coordentry;

/**
 * Plugin DTO for one committed coordinate-entry result crossing the seam:
 * the point in decimal degrees plus the host-formatted display string, so
 * {@code src/main} never names {@code GeoPointMetaData}.
 */
public final class CoordinateEntryResult {

    private final String paneId;
    private final double latitude;
    private final double longitude;
    private final String displayString;
    private final String suggestedAffiliation;

    public CoordinateEntryResult(String paneId, double latitude,
            double longitude, String displayString,
            String suggestedAffiliation) {
        this.paneId = paneId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayString = displayString;
        this.suggestedAffiliation = suggestedAffiliation;
    }

    /**
     * Which entry tab produced the point (ATAK reports it as a free-form
     * string, e.g. the MGRS tab). May be {@code null}.
     */
    public String paneId() {
        return paneId;
    }

    /** Latitude in decimal degrees (WGS-84). */
    public double latitude() {
        return latitude;
    }

    /** Longitude in decimal degrees (WGS-84). */
    public double longitude() {
        return longitude;
    }

    /**
     * The point formatted by the host ({@code GeoPointMetaData.toString()})
     * — exactly the string the legacy demo toasted, including any elevation
     * and source metadata the host appends.
     */
    public String displayString() {
        return displayString;
    }

    /**
     * Affiliation the dialog suggests for a marker dropped at the point
     * (host-defined string; may be {@code null}). The demo ignores it — kept
     * on the DTO because it is part of the dialog's real contract.
     */
    public String suggestedAffiliation() {
        return suggestedAffiliation;
    }
}
