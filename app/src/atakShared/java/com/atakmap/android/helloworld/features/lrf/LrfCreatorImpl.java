package com.atakmap.android.helloworld.features.lrf;

import android.os.Bundle;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.helloworld.tools.LRFPointTool;
import com.atakmap.android.lrf.LocalRangeFinderInput;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.toolbar.ToolManagerBroadcastReceiver;
import com.atakmap.coremap.maps.coords.GeoCalculations;
import com.atakmap.coremap.maps.time.CoordinatedTime;

/**
 * The only place ATAK's LRF plumbing is touched: the tool-manager lifecycle
 * of the plugin's {@link LRFPointTool} and the
 * {@code LocalRangeFinderInput} singleton (the socket-fed dispatcher real LRF
 * hardware readings arrive through). These APIs are source-stable across
 * every supported version, so this lives in {@code src/atakShared}.
 */
public final class LrfCreatorImpl implements LrfCreator {

    private LRFPointTool lrfPointTool;

    @Override
    public String id() {
        return "LrfCreator";
    }

    @Override
    public synchronized void registerPointTool(final LrfPort port) {
        if (lrfPointTool != null)
            return;
        final MapView mapView = MapView.getMapView();
        // Constructing the tool registers it with ToolManagerBroadcastReceiver
        // under LRFPointTool.TOOL_IDENTIFIER; it stays registered (and only
        // hears readings while armed — the tool registers its RangeFinderAction
        // in onToolBegin and clears it in onToolEnd).
        lrfPointTool = new LRFPointTool(mapView,
                new LRFPointTool.LRFCallback() {
                    @Override
                    public void onResults(double distance, double azimuth,
                            double inclination, boolean success) {
                        // The "point from self" projection needs
                        // GeoCalculations + the self marker (both ATAK types),
                        // so it is computed here and crosses the seam as the
                        // display string the legacy demo logged.
                        String pointFromSelf = String.valueOf(
                                GeoCalculations.pointAtDistance(
                                        mapView.getSelfMarker().getPoint(),
                                        distance, azimuth, inclination));
                        port.onRangeFinderResults(distance, azimuth,
                                inclination, success, pointFromSelf);
                    }
                });
    }

    @Override
    public synchronized void disposePointTool() {
        // Legacy parity: LRFPointTool.dispose() is a no-op and the tool is
        // never unregistered from ToolManagerBroadcastReceiver (the legacy
        // receiver never unregistered it either — a fresh registration under
        // the same identifier after a hot reload supersedes the stale one).
        if (lrfPointTool != null) {
            lrfPointTool.dispose();
            lrfPointTool = null;
        }
    }

    @Override
    public void startPointTool() {
        Bundle extras = new Bundle();
        ToolManagerBroadcastReceiver.getInstance().startTool(
                LRFPointTool.TOOL_IDENTIFIER, extras);
    }

    @Override
    public void endCurrentTool() {
        ToolManagerBroadcastReceiver.getInstance().endCurrentTool();
    }

    @Override
    public void simulateRangeFinderReading(double distance, double azimuth,
            double elevation) {
        // note: do not call onRangeFinderInfo as it will bypass other listeners looking to get
        // the data.   The format for the process command is documented as
        // version, uid, system time, distance, azimuth, elevation
        LocalRangeFinderInput.getInstance().process("1," + "fake," +
                new CoordinatedTime().getMilliseconds() + "," + distance + ","
                + azimuth + "," + elevation);
    }

    /**
     * PARTIAL by design: starting the tool would flash ATAK's "Fire Laser
     * Range Finder" prompt (user-visible) and {@code process()} would dispatch
     * to every live LRF listener on the device (irreversible), so the probe
     * resolves both singletons and builds — without dispatching — a reading in
     * the documented process() format (exercising CoordinatedTime linkage).
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "lrf plumbing threw", () -> {
            if (MapView.getMapView() == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            ToolManagerBroadcastReceiver toolManager =
                    ToolManagerBroadcastReceiver.getInstance();
            LocalRangeFinderInput input = LocalRangeFinderInput.getInstance();
            if (toolManager == null || input == null)
                return SelfCheckResult.skipped(id(),
                        "tool manager / LRF input not ready");
            String reading = "1,com.atakmap.android.helloworld.test.lrf,"
                    + new CoordinatedTime().getMilliseconds() + ",1.0,2.0,3.0";
            return SelfCheckResult.partial(id(),
                    "tool manager + LRF input resolved; built reading '"
                            + reading + "' but did not process() it (would "
                            + "dispatch to live LRF listeners) nor start the "
                            + "tool (user-visible prompt)");
        });
    }
}
