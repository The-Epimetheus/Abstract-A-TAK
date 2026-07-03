package com.atakmap.android.helloworld.features.speech;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.helloworld.speechtotext.SpeechBloodHound;
import com.atakmap.android.helloworld.speechtotext.SpeechBrightness;
import com.atakmap.android.helloworld.speechtotext.SpeechDetailOpener;
import com.atakmap.android.helloworld.speechtotext.SpeechItemRemover;
import com.atakmap.android.helloworld.speechtotext.SpeechLinker;
import com.atakmap.android.helloworld.speechtotext.SpeechNavigator;
import com.atakmap.android.helloworld.speechtotext.SpeechNineLine;
import com.atakmap.android.helloworld.speechtotext.SpeechPointDropper;
import com.atakmap.android.image.quickpic.QuickPicReceiver;
import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapView;
import com.atakmap.coremap.conversions.CoordinateFormat;
import com.atakmap.coremap.conversions.CoordinateFormatUtilities;
import com.atakmap.coremap.maps.coords.GeoPoint;

/**
 * The ATAK side of the speech dispatch. The speech action classes each do
 * their whole job in the constructor (verbs disguised as objects), so every
 * method here is construct-and-forget with the live {@code MapView} — the
 * exact call shapes the legacy receiver's switch made, proven across all
 * targeted versions. Source-stable everywhere → shared impl source set.
 */
public final class SpeechCreatorImpl implements SpeechCreator {

    private final Context pluginContext;

    public SpeechCreatorImpl(Context pluginContext) {
        this.pluginContext = pluginContext;
    }

    @Override
    public String id() {
        return "SpeechCreator";
    }

    @Override
    public void navigateTo(String destination, boolean quickNav) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechNavigator(mv, destination, quickNav);
    }

    @Override
    public void plotPoint(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechPointDropper(destination, mv, pluginContext);
    }

    @Override
    public void bloodhound(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechBloodHound(mv, destination, pluginContext);
    }

    @Override
    public void openNineLine(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechNineLine(destination, mv, pluginContext);
    }

    @Override
    public void openCompassOnSelf() {
        // Legacy marked this case "DOESNT WORK": the broadcast goes out but
        // the host never opens the compass. Kept verbatim for fidelity —
        // fixing it needs a different host entry point, not a different seam.
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        AtakBroadcast.getInstance()
                .sendBroadcast(new Intent()
                        .setAction(
                                "com.atakmap.android.maps.COMPASS")
                        .putExtra("targetUID",
                                mv.getSelfMarker().getUID()));
    }

    @Override
    public void adjustBrightness(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechBrightness(mv, pluginContext, destination);
    }

    @Override
    public void deleteItem(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechItemRemover(destination, mv, pluginContext);
    }

    @Override
    public void openDetails(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechDetailOpener(destination, mv);
    }

    @Override
    public void linkItems(String destination) {
        MapView mv = MapView.getMapView();
        if (mv == null)
            return;
        new SpeechLinker(destination, mv, pluginContext);
    }

    @Override
    public void launchQuickPic() {
        AtakBroadcast.getInstance().sendBroadcast(
                new Intent().setAction(QuickPicReceiver.QUICK_PIC));
    }

    @Override
    public double[] mgrsToLatLon(String gridZone, String squareId,
            String easting, String northing) {
        // Throws IllegalArgumentException on malformed components — the
        // legacy contract; the Controller catches and toasts.
        GeoPoint point = CoordinateFormatUtilities.convert(new String[] {
                gridZone, squareId, easting, northing
        }, CoordinateFormat.MGRS);
        return new double[] {
                point.getLatitude(), point.getLongitude()
        };
    }

    /**
     * PARTIAL by design: constructing any speech action class fires its
     * user-visible map action immediately (constructor-as-verb), and the
     * compass/quick-pic paths broadcast to live host components — so the
     * probe exercises the one pure, invisible path instead: the MGRS
     * conversion the speech marker depends on.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "MGRS conversion threw", () -> {
            // The canonical MGRS example coordinate (Honolulu).
            double[] p = mgrsToLatLon("4Q", "FJ", "12345", "67890");
            return SelfCheckResult.partial(id(),
                    "MGRS conversion resolved (" + p[0] + ", " + p[1]
                            + "); speech actions not constructed"
                            + " (each fires a user-visible map action)");
        });
    }
}
