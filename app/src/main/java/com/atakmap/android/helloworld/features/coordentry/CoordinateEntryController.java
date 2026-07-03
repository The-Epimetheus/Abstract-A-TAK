package com.atakmap.android.helloworld.features.coordentry;

import android.content.Context;
import android.widget.Toast;

/**
 * The coordinate-entry feature's Controller: opens ATAK's stock
 * coordinate-entry dialog through {@link CoordinateEntryCreator} and toasts
 * the committed point's host-formatted string, mirroring the original demo.
 * Deliberately shallow (ADR-0005): the demo's whole behavior is
 * "prompt, then show what came back".
 */
public class CoordinateEntryController {

    private final CoordinateEntryCreator coordinateEntryCreator;
    private final Context context;

    public CoordinateEntryController(
            CoordinateEntryCreator coordinateEntryCreator, Context context) {
        this.coordinateEntryCreator = coordinateEntryCreator;
        this.context = context;
    }

    /**
     * Show the coordinate-entry dialog pre-filled with the map center; when
     * the user commits a point, toast it exactly as the host formats it.
     * The port fires on the UI thread (Creator contract), so the toast needs
     * no extra posting.
     */
    public void promptForCoordinate() {
        coordinateEntryCreator.promptForCoordinate(
                result -> toast(result.displayString()));
    }

    private void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
