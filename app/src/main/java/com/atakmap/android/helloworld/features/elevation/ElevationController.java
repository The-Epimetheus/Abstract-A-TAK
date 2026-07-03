package com.atakmap.android.helloworld.features.elevation;

import android.content.Context;
import android.widget.Toast;

/**
 * The elevation feature's Controller: queries terrain + surface at the map
 * center through {@link ElevationCreator} and surfaces both values as toasts,
 * mirroring the original demo.
 */
public class ElevationController {

    private final ElevationCreator elevationCreator;
    private final Context context;

    public ElevationController(ElevationCreator elevationCreator,
            Context context) {
        this.elevationCreator = elevationCreator;
        this.context = context;
    }

    /**
     * Toast the terrain and surface elevations at the map center; quiet if
     * the map has no center yet.
     */
    public void showElevationAtCenter() {
        ElevationSample sample = elevationCreator.sampleAtMapCenter();
        if (sample == null)
            return;
        toast("Terrain: " + sample.terrainMeters());
        toast("Surface: " + sample.surfaceMeters());
    }

    private void toast(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
