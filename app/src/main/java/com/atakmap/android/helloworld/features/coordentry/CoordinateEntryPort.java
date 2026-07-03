package com.atakmap.android.helloworld.features.coordentry;

/**
 * Callback port for the coordinate-entry dialog: the plugin-owned listener a
 * Controller hands to {@link CoordinateEntryCreator#promptForCoordinate}. The
 * impl implements ATAK's real {@code CoordinateEntryCapability.ResultCallback}
 * and adapts each committed point to this call, on the UI thread.
 */
public interface CoordinateEntryPort {

    /** The user committed a point in the dialog. Invoked on the UI thread. */
    void onCoordinateEntered(CoordinateEntryResult result);
}
