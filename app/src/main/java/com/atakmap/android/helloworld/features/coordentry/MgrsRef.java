package com.atakmap.android.helloworld.features.coordentry;

/**
 * Plugin DTO for one MGRS grid reference, split the way ATAK's converter
 * consumes it: grid-zone designator, 100&nbsp;km square identifier, easting,
 * northing — four strings, in that order. Keeping them as named parts (not a
 * bare {@code String[]}) is the point: the legacy bug surface here is
 * argument order.
 */
public final class MgrsRef {

    private final String gridZone;
    private final String squareId;
    private final String easting;
    private final String northing;

    private MgrsRef(String gridZone, String squareId, String easting,
            String northing) {
        this.gridZone = gridZone;
        this.squareId = squareId;
        this.easting = easting;
        this.northing = northing;
    }

    /**
     * @param gridZone  grid-zone designator: UTM zone number + latitude band,
     *                  e.g. {@code "18S"} (a caller assembling it from spoken
     *                  fragments concatenates numeric zone + band letter)
     * @param squareId  100&nbsp;km square identifier, e.g. {@code "UJ"}
     * @param easting   easting digits within the square, e.g. {@code "23371"}
     * @param northing  northing digits within the square, e.g. {@code "06519"}
     */
    public static MgrsRef of(String gridZone, String squareId, String easting,
            String northing) {
        return new MgrsRef(gridZone, squareId, easting, northing);
    }

    public String gridZone() {
        return gridZone;
    }

    public String squareId() {
        return squareId;
    }

    public String easting() {
        return easting;
    }

    public String northing() {
        return northing;
    }
}
