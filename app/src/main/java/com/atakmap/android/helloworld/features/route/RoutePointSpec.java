package com.atakmap.android.helloworld.features.route;

/**
 * Plugin DTO for one route point crossing the seam: a waypoint (checkpoint the
 * navigator announces) or a control point (shaping vertex), in decimal degrees.
 */
public final class RoutePointSpec {

    public final double latitude;
    public final double longitude;
    public final boolean controlPoint;

    private RoutePointSpec(double latitude, double longitude,
            boolean controlPoint) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.controlPoint = controlPoint;
    }

    public static RoutePointSpec wayPoint(double latitude, double longitude) {
        return new RoutePointSpec(latitude, longitude, false);
    }

    public static RoutePointSpec controlPoint(double latitude, double longitude) {
        return new RoutePointSpec(latitude, longitude, true);
    }
}
