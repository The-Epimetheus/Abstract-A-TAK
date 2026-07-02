package com.atakmap.android.helloworld.features.route;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.routes.Route;
import com.atakmap.android.routes.RouteMapReceiver;
import com.atakmap.android.routes.RouteNavigationManager;
import com.atakmap.android.routes.RouteNavigator;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoCalculations;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The only place {@code Route}/{@code RouteMapReceiver}/{@code RouteNavigator}
 * are touched. The route APIs used here are byte-stable across every supported
 * version, so this lives in {@code src/atakShared}. Keeps the
 * {@code RouteHandle -> Route} registry and adapts the two ATAK navigation
 * listener interfaces into the single plugin-owned {@link RouteNavPort}.
 */
public final class RouteCreatorImpl implements RouteCreator {

    private static final String TAG = "RouteCreatorImpl";

    private final Map<RouteHandle, Route> registry = new HashMap<>();
    private NavAdapter navAdapter;

    @Override
    public String id() {
        return "RouteCreator";
    }

    @Override
    public RouteHandle createRoute(String title, int argbColor,
            String pointPrefix, List<RoutePointSpec> points) {
        MapView mv = MapView.getMapView();
        Route route = buildRoute(mv, title, argbColor, pointPrefix, points);

        MapGroup routeGroup = mv.getRootGroup().findMapGroup("Route");
        routeGroup.addItem(route);
        route.persist(mv.getMapEventDispatcher(), null, getClass());

        // GeoCalculations demo carried over from the original button handler:
        // walk 500m along the new route and log where that lands.
        GeoPoint sample = findArbitraryPointOnLine(
                Arrays.asList(route.getPoints()), 500);
        Log.d(TAG, "point 500m along " + title + ": " + sample);

        RouteHandle handle = new RouteHandle();
        registry.put(handle, route);
        return handle;
    }

    @Override
    public boolean insertPoints(RouteHandle handle, int index,
            List<RoutePointSpec> points) {
        Route route = registry.get(handle);
        if (route == null)
            return false;
        route.addMarkers(index, toPointItems(points));
        return true;
    }

    @Override
    public void createFlyingRouteWithDetails(String title, int argbColor,
            String pointPrefix) {
        MapView mv = MapView.getMapView();
        Route route = new Route(mv, title, argbColor, pointPrefix,
                UUID.randomUUID().toString());
        route.setRouteMethod(Route.RouteMethod.Flying.toString());
        RouteMapReceiver receiver = RouteMapReceiver.getInstance();
        // Finalize route and show details
        route.setMetaString("entry", "user");
        receiver.getRouteGroup().addItem(route);
        route.setVisible(true);
        receiver.showRouteDetails(route, null, true);
    }

    @Override
    public List<String> completeRouteTitles() {
        List<String> titles = new ArrayList<>();
        RouteMapReceiver receiver = RouteMapReceiver.getInstance();
        if (receiver == null)
            return titles;
        for (Route route : receiver.getCompleteRoutes())
            titles.add(route.getTitle());
        return titles;
    }

    @Override
    public void registerNavigationPort(RouteNavPort port) {
        unregisterNavigationPort();
        navAdapter = new NavAdapter(port);
        RouteNavigator.getInstance()
                .registerRouteNavigatorListener(navAdapter);
        // If navigation is already underway, hook the manager immediately
        // (normally done in onNavigationStarted).
        if (RouteNavigator.getInstance().getNavManager() != null)
            RouteNavigator.getInstance().getNavManager()
                    .registerListener(navAdapter);
    }

    @Override
    public void unregisterNavigationPort() {
        if (navAdapter == null)
            return;
        RouteNavigator.getInstance()
                .unregisterRouteNavigatorListener(navAdapter);
        if (RouteNavigator.getInstance().getNavManager() != null)
            RouteNavigator.getInstance().getNavManager()
                    .unregisterListener(navAdapter);
        navAdapter = null;
    }

    private static Route buildRoute(MapView mv, String title, int argbColor,
            String pointPrefix, List<RoutePointSpec> points) {
        Route route = new Route(mv, title, argbColor, pointPrefix,
                UUID.randomUUID().toString());
        // Adding an array of points is much faster than adding them one at a
        // time (each single add triggers a full refresh of the route).
        route.addMarkers(0, toPointItems(points));
        return route;
    }

    private static PointMapItem[] toPointItems(List<RoutePointSpec> specs) {
        PointMapItem[] items = new PointMapItem[specs.size()];
        for (int i = 0; i < specs.size(); i++) {
            RoutePointSpec spec = specs.get(i);
            GeoPoint gp = new GeoPoint(spec.latitude, spec.longitude,
                    GeoPoint.UNKNOWN);
            items[i] = spec.controlPoint
                    ? Route.createControlPoint(gp, UUID.randomUUID().toString())
                    : Route.createWayPoint(GeoPointMetaData.wrap(gp),
                            UUID.randomUUID().toString());
        }
        return items;
    }

    /**
     * Brute-force "point at distance along a line" demo (GeoCalculations),
     * carried over intact from the original HelloWorld button handler.
     */
    private static GeoPoint findArbitraryPointOnLine(List<GeoPoint> points,
            double distance) {
        for (int i = 1; i < points.size(); ++i) {
            GeoPoint b = points.get(i);
            GeoPoint a = points.get(i - 1);

            final double dist = GeoCalculations.slantDistanceTo(a, b);
            distance -= dist;
            if (distance < 0) {
                final double azimuth = GeoCalculations.bearingTo(b, a);
                final double inclination;
                if (Double.isNaN(b.getAltitude())
                        || Double.isNaN(a.getAltitude())) {
                    inclination = 0;
                } else {
                    final double height = b.getAltitude() - a.getAltitude();
                    inclination = Math.asin(height / dist);
                }
                // back track on the route based on the overage.
                return GeoCalculations.pointAtDistance(b, azimuth,
                        Math.abs(distance), inclination);
            }
        }
        return points.get(points.size() - 1);
    }

    /** Adapts ATAK's two navigation listener interfaces onto the plugin port. */
    private static final class NavAdapter
            implements RouteNavigator.RouteNavigatorListener,
            RouteNavigationManager.RouteNavigationManagerEventListener {

        private final RouteNavPort port;

        NavAdapter(RouteNavPort port) {
            this.port = port;
        }

        /* ---- RouteNavigatorListener ---- */

        @Override
        public void onNavigationStarting(RouteNavigator navigator) {
        }

        @Override
        public void onNavigationStarted(RouteNavigator navigator, Route route) {
            port.onNavigationStarted();
            navigator.getNavManager().registerListener(this);
        }

        @Override
        public void onNavigationStopping(RouteNavigator navigator,
                Route route) {
            navigator.getNavManager().unregisterListener(this);
        }

        @Override
        public void onNavigationStopped(RouteNavigator navigator) {
            port.onNavigationStopped();
        }

        /* ---- RouteNavigationManagerEventListener ---- */

        @Override
        public void onGpsStatusChanged(RouteNavigationManager rnm,
                boolean state) {
            port.onGpsStatusChanged(state);
        }

        @Override
        public void onLocationChanged(RouteNavigationManager rnm,
                GeoPoint oldLocation, GeoPoint newLocation) {
            port.onLocationChanged(oldLocation.getLatitude(),
                    oldLocation.getLongitude(), newLocation.getLatitude(),
                    newLocation.getLongitude());
        }

        @Override
        public void onNavigationObjectiveChanged(RouteNavigationManager rnm,
                PointMapItem newObjective, boolean isFromRouteProgression) {
            port.onObjectiveChanged();
        }

        @Override
        public void onOffRoute(RouteNavigationManager rnm) {
            port.onOffRoute();
        }

        @Override
        public void onReturnedToRoute(RouteNavigationManager rnm) {
            port.onReturnedToRoute();
        }

        @Override
        public void onTriggerEntered(RouteNavigationManager rnm,
                PointMapItem item, int triggerIndex) {
            port.onTriggerEntered();
        }

        @Override
        public void onArrivedAtPoint(RouteNavigationManager rnm,
                PointMapItem item) {
            port.onArrivedAtPoint();
        }

        @Override
        public void onDepartedPoint(RouteNavigationManager rnm,
                PointMapItem item) {
            port.onDepartedPoint();
        }
    }

    /**
     * Real op done and undone: builds a Route with two waypoints under the
     * reserved selfcheck title — never added to a map group, so it leaves no
     * trace — and registers + unregisters a navigation adapter.
     */
    @Override
    public SelfCheckResult selfCheck() {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return SelfCheckResult.skipped(id(), "MapView not ready");
        NavAdapter probeAdapter = null;
        try {
            List<RoutePointSpec> pts = new ArrayList<>();
            pts.add(RoutePointSpec.wayPoint(0.0, 0.0));
            pts.add(RoutePointSpec.wayPoint(0.0001, 0.0));
            Route probe = buildRoute(mv, "helloworld.selfcheck.route", 0xFFFFFFFF,
                    "CP", pts);
            if (probe.getPoints().length < 2)
                return SelfCheckResult.failed(id(),
                        "route built but points missing", null);

            probeAdapter = new NavAdapter(new NoopPort());
            RouteNavigator.getInstance()
                    .registerRouteNavigatorListener(probeAdapter);
            return SelfCheckResult.full(id(),
                    "built + discarded a 2-point route (never mapped); "
                            + "nav listener registered + unregistered");
        } catch (Throwable t) {
            return SelfCheckResult.failed(id(), "route path threw", t);
        } finally {
            try {
                if (probeAdapter != null)
                    RouteNavigator.getInstance()
                            .unregisterRouteNavigatorListener(probeAdapter);
            } catch (Throwable ignore) {
                // best-effort teardown
            }
        }
    }

    /** Silent port for the selfCheck probe adapter. */
    private static final class NoopPort implements RouteNavPort {
        @Override public void onNavigationStarted() {}
        @Override public void onNavigationStopped() {}
        @Override public void onGpsStatusChanged(boolean available) {}
        @Override public void onLocationChanged(double a, double b, double c,
                double d) {}
        @Override public void onObjectiveChanged() {}
        @Override public void onOffRoute() {}
        @Override public void onReturnedToRoute() {}
        @Override public void onTriggerEntered() {}
        @Override public void onArrivedAtPoint() {}
        @Override public void onDepartedPoint() {}
    }
}
