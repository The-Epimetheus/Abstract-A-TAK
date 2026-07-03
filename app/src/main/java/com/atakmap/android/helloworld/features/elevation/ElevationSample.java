package com.atakmap.android.helloworld.features.elevation;

/**
 * One elevation query result: the terrain (bare-earth model) and surface
 * (includes buildings/canopy) elevations at a point, in meters. Either value
 * may be {@code NaN} where no loaded elevation source covers the point.
 */
public final class ElevationSample {

    private final double terrainMeters;
    private final double surfaceMeters;

    public ElevationSample(double terrainMeters, double surfaceMeters) {
        this.terrainMeters = terrainMeters;
        this.surfaceMeters = surfaceMeters;
    }

    public double terrainMeters() {
        return terrainMeters;
    }

    public double surfaceMeters() {
        return surfaceMeters;
    }
}
