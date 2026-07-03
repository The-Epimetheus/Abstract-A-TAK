package com.atakmap.android.helloworld.features.coordentry;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's coordinate plumbing: the stock coordinate-entry
 * dialog ({@code CoordinateEntryCapability}) and the coordinate-format
 * conversion utilities ({@code CoordinateFormatUtilities} /
 * {@code CoordinateFormat}). Both drag host types — and the dialog even a
 * host string resource ({@code com.atakmap.app.R.string.rb_coord_title}) —
 * that must not leak into {@code src/main}. Interface here; implementation
 * in {@code src/atakShared} (both APIs are source-stable across every
 * targeted version today, so this doubles as an insurance seam — see
 * CONTEXT.md).
 */
public interface CoordinateEntryCreator extends Creator {

    /**
     * Show ATAK's stock coordinate-entry dialog (the same multi-tab
     * MGRS/DD/DM/DMS entry the R+B tool uses), titled with the host's stock
     * coordinate-dialog title and pre-filled with the current map center
     * (with elevation). When the user commits a point, the port fires once
     * with the result; dismissing the dialog fires nothing.
     *
     * <p>The port is always invoked on the UI thread — the dialog's own
     * callback carries no such guarantee, so the seam posts to the map view
     * first (as the legacy demo did) before touching the port.
     *
     * <p>No-op if the map view is not available yet.
     */
    void promptForCoordinate(CoordinateEntryPort port);

    /**
     * Convert an MGRS grid reference to a WGS-84 coordinate through ATAK's
     * {@code CoordinateFormatUtilities}.
     *
     * <p>Consumer-in-waiting: the speech-marker demo
     * ({@code createSpeechMarker} in the legacy receiver) builds its marker
     * point from spoken MGRS fragments through exactly this conversion; when
     * that cluster migrates, its Controller calls this instead of naming
     * {@code CoordinateFormat.MGRS} itself.
     *
     * @return {@code {latitude, longitude}} in decimal degrees, or
     *         {@code null} when the parts do not form a valid MGRS reference
     *         (the seam absorbs ATAK's {@code IllegalArgumentException} into
     *         this contract).
     */
    double[] mgrsToLatLon(MgrsRef ref);
}
