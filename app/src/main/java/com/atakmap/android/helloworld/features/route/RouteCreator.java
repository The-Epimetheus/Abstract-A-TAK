package com.atakmap.android.helloworld.features.route;

import java.util.List;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over ATAK's route subsystem: route construction/persistence, the
 * route list, and navigation-event registration. The route APIs used here are
 * byte-stable across every supported version, so the impl lives in
 * {@code src/atakShared} — the seam still earns its keep by keeping
 * {@code Route}/{@code RouteNavigator} (and their history of additive change)
 * out of {@code src/main}, and by making route behavior testable through one
 * interface.
 */
public interface RouteCreator extends Creator {

    /**
     * Create a route from {@code points}, add it to the map's route group, and
     * persist it.
     *
     * @return a handle to the live route the impl retains.
     */
    RouteHandle createRoute(String title, int argbColor, String pointPrefix,
            List<RoutePointSpec> points);

    /**
     * Insert points into the route behind {@code handle} at {@code index}.
     *
     * @return false if the handle is unknown (nothing inserted).
     */
    boolean insertPoints(RouteHandle handle, int index,
            List<RoutePointSpec> points);

    /**
     * Create an empty flying-method route, add it to the route group, and open
     * ATAK's route-details editor on it.
     */
    void createFlyingRouteWithDetails(String title, int argbColor,
            String pointPrefix);

    /** Titles of every complete route currently on the map. */
    List<String> completeRouteTitles();

    /**
     * Register {@code port} for navigation events (started/stopped, off-route,
     * arrivals, ...). At most one port at a time; replaces any previous one.
     */
    void registerNavigationPort(RouteNavPort port);

    /** Unregister the port registered by {@link #registerNavigationPort}. */
    void unregisterNavigationPort();
}
