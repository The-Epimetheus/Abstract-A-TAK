package com.atakmap.android.helloworld.features.route;

/**
 * Callback port for route-navigation events: the plugin-owned listener a
 * Controller implements to receive them. The impl implements ATAK's real
 * {@code RouteNavigatorListener} / {@code RouteNavigationManagerEventListener}
 * pair and adapts each event to these primitive-typed calls.
 */
public interface RouteNavPort {

    void onNavigationStarted();

    void onNavigationStopped();

    void onGpsStatusChanged(boolean available);

    void onLocationChanged(double oldLat, double oldLon, double newLat,
            double newLon);

    void onObjectiveChanged();

    void onOffRoute();

    void onReturnedToRoute();

    void onTriggerEntered();

    void onArrivedAtPoint();

    void onDepartedPoint();
}
