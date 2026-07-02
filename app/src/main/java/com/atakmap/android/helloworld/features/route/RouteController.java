package com.atakmap.android.helloworld.features.route;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * The route feature's Controller — the ATAK-free half of the Humble-Object
 * split, and the plugin's first worked example of one. The Humble shell
 * ({@code HelloWorldDropDownReceiver}) forwards button taps here as primitives;
 * this class holds the actual behavior (waypoint math, hook state, event
 * toasts) and depends only on {@link RouteCreator} and Android. Testable
 * through this interface with no ATAK on the classpath (ADR-0004 still runs it
 * against the real host).
 */
public class RouteController implements RouteNavPort {

    private static final String TAG = "RouteController";

    private final RouteCreator routeCreator;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private RouteHandle demoRoute;
    private boolean navigationHooked;

    public RouteController(RouteCreator routeCreator, Context context) {
        this.routeCreator = routeCreator;
        this.context = context;
    }

    /**
     * Create the demo route: five waypoints marching north from the map center
     * in 0.0001° steps.
     */
    public void createDemoRoute(double centerLat, double centerLon) {
        Log.d(TAG, "creating a quick route");
        List<RoutePointSpec> points = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            points.add(RoutePointSpec.wayPoint(centerLat + (i * .0001),
                    centerLon));
        }
        demoRoute = routeCreator.createRoute("My Route", Color.WHITE, "CP",
                points);
        Log.d(TAG, "route created: " + demoRoute);
    }

    /**
     * Extend the demo route with fifteen points (alternating way/control) at
     * index 2, mirroring the original demo's zig-zag pattern.
     *
     * @return false if {@link #createDemoRoute} has not run yet this session.
     */
    public boolean extendDemoRoute(double centerLat, double centerLon) {
        if (demoRoute == null)
            return false;
        List<RoutePointSpec> points = new ArrayList<>();
        for (int i = 1; i < 16; ++i) {
            if (i % 2 == 0) {
                points.add(RoutePointSpec.wayPoint(
                        centerLat - (i * .0001), centerLon + (i * .0001)));
            } else {
                points.add(RoutePointSpec.controlPoint(
                        centerLat + (i * .0002), centerLon + (i * .0002)));
            }
        }
        return routeCreator.insertPoints(demoRoute, 2, points);
    }

    /** Create an empty flying route and open ATAK's route editor on it. */
    public void createFlyingRoute() {
        routeCreator.createFlyingRouteWithDetails("my flying route", Color.RED,
                "cp");
    }

    /** Titles of every complete route on the map, for the picker dialog. */
    public List<String> completeRouteTitles() {
        return routeCreator.completeRouteTitles();
    }

    public boolean isNavigationHooked() {
        return navigationHooked;
    }

    /**
     * Hook or unhook route-navigation events; while hooked, each event surfaces
     * as a toast (this class is its own {@link RouteNavPort}).
     *
     * @return the new hooked state.
     */
    public boolean toggleNavigationEvents() {
        if (navigationHooked) {
            routeCreator.unregisterNavigationPort();
            navigationHooked = false;
        } else {
            routeCreator.registerNavigationPort(this);
            navigationHooked = true;
        }
        return navigationHooked;
    }

    /* ------------ RouteNavPort: navigation events, surfaced as toasts ------------ */

    @Override
    public void onNavigationStarted() {
        toast("Navigation started");
    }

    @Override
    public void onNavigationStopped() {
        toast("Navigation stopped");
    }

    @Override
    public void onGpsStatusChanged(boolean available) {
        // quiet — matches the original demo behavior
    }

    @Override
    public void onLocationChanged(double oldLat, double oldLon, double newLat,
            double newLon) {
        if (oldLat != newLat || oldLon != newLon)
            toast("Location changed");
    }

    @Override
    public void onObjectiveChanged() {
        toast("Navigation Objective Changed");
    }

    @Override
    public void onOffRoute() {
        toast("Off Route");
    }

    @Override
    public void onReturnedToRoute() {
        toast("Back on Route.");
    }

    @Override
    public void onTriggerEntered() {
        toast("Entered trigger");
    }

    @Override
    public void onArrivedAtPoint() {
        toast("Arrived at point");
    }

    @Override
    public void onDepartedPoint() {
        toast("Departed point");
    }

    private void toast(final String msg) {
        mainHandler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT)
                .show());
    }
}
