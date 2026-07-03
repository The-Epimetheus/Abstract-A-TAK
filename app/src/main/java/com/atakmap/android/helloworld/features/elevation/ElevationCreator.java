package com.atakmap.android.helloworld.features.elevation;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's elevation query engine ({@code ElevationManager}
 * plus its terrain/surface model filters). Source-stable across every
 * targeted version today, so this is an insurance seam (see CONTEXT.md).
 * Interface in {@code src/main} (ATAK-free); the implementation lives in
 * {@code src/atakShared}.
 */
public interface ElevationCreator extends Creator {

    /**
     * Query the terrain (bare-earth) and surface elevation models at the
     * current map center.
     *
     * @return the sample — values may be {@code NaN} where nothing covers the
     *         point — or {@code null} if the map has no usable center yet.
     */
    ElevationSample sampleAtMapCenter();
}
