package com.atakmap.android.helloworld.features.coordentry;

import android.content.Context;

import com.atakmap.android.gui.coordinateentry.CoordinateEntryCapability;
import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

/**
 * The only place ATAK's coordinate plumbing is touched:
 * {@code CoordinateEntryCapability} (the stock multi-format entry dialog —
 * a host singleton addressed by the HOST activity context, and titled with a
 * host string resource) and {@code CoordinateFormatUtilities} /
 * {@code CoordinateFormat} for grid-reference conversion. Source-stable
 * across all targeted versions → shared impl source set.
 */
public final class CoordinateEntryCreatorImpl
        implements CoordinateEntryCreator {

    @Override
    public String id() {
        return "CoordinateEntryCreator";
    }

    @Override
    public void promptForCoordinate(final CoordinateEntryPort port) {
        final MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        // The capability is a host singleton and the dialog must be built on
        // the host activity context (plugin context would crash theming) —
        // exactly the kind of host reference this seam exists to contain.
        final Context ctx = mv.getContext();
        // Positional arguments preserved verbatim from the proven legacy
        // call (title, no preferred format, checkable, initial point =
        // map center with elevation, no preset affiliation, no affiliation
        // prompt) — the title is the host's own R+B coordinate-dialog string,
        // so the dialog reads stock in every host language.
        CoordinateEntryCapability.getInstance(ctx).showDialog(
                ctx.getString(com.atakmap.app.R.string.rb_coord_title),
                        null, true,
                        mv.getPointWithElevation(), null, false,
                new CoordinateEntryCapability.ResultCallback() {
                    @Override
                    public void onResultCallback(final String pane,
                            final GeoPointMetaData point,
                            final String suggestedAffiliation) {
                        // The dialog's callback carries no UI-thread
                        // guarantee — the legacy demo posted to the MapView
                        // before toasting. The seam keeps that: the port
                        // always fires on the UI thread.
                        mv.post(new Runnable() {
                            @Override
                            public void run() {
                                GeoPoint p = point.get();
                                port.onCoordinateEntered(
                                        new CoordinateEntryResult(pane,
                                                p.getLatitude(),
                                                p.getLongitude(),
                                                point.toString(),
                                                suggestedAffiliation));
                            }
                        });
                    }
                });
    }

    @Override
    public double[] mgrsToLatLon(MgrsRef ref) {
        try {
            GeoPoint p = CoordinateFormatUtilities.convert(new String[] {
                    ref.gridZone(), ref.squareId(), ref.easting(),
                    ref.northing()
            }, CoordinateFormat.MGRS);
            return new double[] {
                    p.getLatitude(), p.getLongitude()
            };
        } catch (IllegalArgumentException e) {
            // Seam contract: invalid parts -> null, instead of leaking
            // ATAK's IllegalArgumentException across the boundary.
            return null;
        }
    }

    /**
     * PARTIAL by design: showing the real entry dialog would be
     * user-visible, so that path only resolves the capability singleton.
     * The MGRS conversion IS exercised end-to-end (pure math, no artifacts)
     * against a known truth — the NGA textbook reference for the Washington
     * Monument — plus the invalid-input edge of the null contract.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "coordinate plumbing threw", () -> {
            // 18S UJ 23371 06519 = Washington Monument (38.8894, -77.0353).
            double[] ll = mgrsToLatLon(
                    MgrsRef.of("18S", "UJ", "23371", "06519"));
            if (ll == null)
                return SelfCheckResult.failed(id(),
                        "known-good MGRS reference did not convert", null);
            if (Math.abs(ll[0] - 38.8894) > 0.05
                    || Math.abs(ll[1] - -77.0353) > 0.05)
                return SelfCheckResult.failed(id(),
                        "MGRS conversion off truth: lat=" + ll[0] + " lon="
                                + ll[1],
                        null);
            if (mgrsToLatLon(
                    MgrsRef.of("not", "a", "grid", "ref")) != null)
                return SelfCheckResult.failed(id(),
                        "invalid MGRS reference did not map to null", null);

            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.partial(id(),
                        "MGRS conversion verified end-to-end; entry dialog "
                                + "unresolved (MapView not ready)");
            CoordinateEntryCapability cap = CoordinateEntryCapability
                    .getInstance(mv.getContext());
            if (cap == null)
                return SelfCheckResult.failed(id(),
                        "CoordinateEntryCapability.getInstance returned null",
                        null);
            return SelfCheckResult.partial(id(),
                    "MGRS conversion verified end-to-end; entry dialog "
                            + "resolved but not shown (would be user-visible)");
        });
    }
}
