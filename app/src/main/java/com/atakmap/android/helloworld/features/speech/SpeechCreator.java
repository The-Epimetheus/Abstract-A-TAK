package com.atakmap.android.helloworld.features.speech;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over the speech demo's ATAK-touching dispatch targets. The
 * speech action classes ({@code speechtotext/*} — {@code SpeechNavigator},
 * {@code SpeechPointDropper}, …) are unmigrated debt: each demands a raw ATAK
 * {@code MapView} at construction and performs its whole job in the
 * constructor (verbs disguised as objects). Until they migrate, this seam
 * owns constructing them — plus the two actions that need ATAK types the
 * Controller cannot name (the self marker's uid for the compass broadcast,
 * {@code QuickPicReceiver.QUICK_PIC} for the camera) and the MGRS conversion
 * ({@code CoordinateFormatUtilities}) the speech marker depends on — so
 * {@link SpeechController} stays ATAK-free. Interface in {@code src/main};
 * the implementation lives in {@code src/atakShared}.
 */
public interface SpeechCreator extends Creator {

    /**
     * Draw a route to the spoken destination and start navigating it
     * ({@code quickNav} is the spoken "quick" variant flag).
     */
    void navigateTo(String destination, boolean quickNav);

    /** Plot a marker at the spoken destination. */
    void plotPoint(String destination);

    /** Start a bloodhound track to a marker, route, or address. */
    void bloodhound(String destination);

    /** Open the 9-Line window on the named target. */
    void openNineLine(String destination);

    /**
     * Ask ATAK to open the compass on the self marker. The legacy receiver
     * marked this case "DOESNT WORK" — the broadcast is sent but the host
     * never opens the compass; preserved verbatim for fidelity.
     */
    void openCompassOnSelf();

    /** Toggle the brightness slider / apply the spoken brightness. */
    void adjustBrightness(String destination);

    /** Delete the named shape, marker, or route. */
    void deleteItem(String destination);

    /** Open the named marker's detail menu. */
    void openDetails(String destination);

    /** Draw an R&B line between the two spoken map items. */
    void linkItems(String destination);

    /** Launch ATAK's quick-pic camera. */
    void launchQuickPic();

    /**
     * Convert an MGRS coordinate to latitude/longitude.
     *
     * @param gridZone the grid-zone designator: numeric grid + alpha grid
     *            (e.g. {@code "18T"})
     * @param squareId the 100&nbsp;km square id (e.g. {@code "WL"})
     * @param easting the easting digits
     * @param northing the northing digits
     * @return {@code {latitude, longitude}} in decimal degrees
     * @throws IllegalArgumentException if the components do not form a valid
     *             MGRS coordinate — the legacy contract, kept so the caller
     *             can toast its "error getting the MGRS point" message
     */
    double[] mgrsToLatLon(String gridZone, String squareId, String easting,
            String northing);
}
