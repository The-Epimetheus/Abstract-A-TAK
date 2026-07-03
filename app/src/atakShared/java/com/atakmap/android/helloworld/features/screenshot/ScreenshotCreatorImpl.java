package com.atakmap.android.helloworld.features.screenshot;

import android.content.Context;
import android.graphics.PointF;

import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.helloworld.image.MapScreenshotExample;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.tilecapture.TileCapture;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoBounds;
import com.atakmap.coremap.maps.coords.GeoPoint;

/**
 * The seam in front of ATAK's tile-capture workflow. The workflow walkthrough
 * itself lives in {@link MapScreenshotExample} — a heavily commented teaching
 * class that captures the map imagery ({@code TileCapture} +
 * {@code ImageryCaptureTask}), paints the map items on top
 * ({@code MapItemCapturePP} via {@code TiledCanvas}), saves a JPEG under
 * ATAK's {@code tmp} directory and toasts the finished path. That class still
 * sits in {@code src/main/...image} as tracked boundary debt; this Creator is
 * its only remaining referencer, so relocating it behind the seam is a pure
 * file move (deferred, see MIGRATION notes).
 */
public final class ScreenshotCreatorImpl implements ScreenshotCreator {

    private static final String TAG = "ScreenshotCreatorImpl";

    private final Context pluginContext;

    public ScreenshotCreatorImpl(Context pluginContext) {
        // MapScreenshotExample resolves its "finished" toast string from
        // plugin resources, so the workflow needs the plugin context.
        this.pluginContext = pluginContext;
    }

    @Override
    public String id() {
        return "ScreenshotCreator";
    }

    @Override
    public void captureMapScreenshot() {
        MapView mv = MapView.getMapView();
        if (mv == null) {
            // Same terminal condition the workflow reports when the map view
            // hasn't been created yet — surface it before constructing.
            Log.e(TAG, "MapView not ready - cannot capture map screenshot");
            return;
        }
        new MapScreenshotExample(mv, pluginContext).start();
    }

    /**
     * PARTIAL by design: a real capture kicks off an AsyncTask that writes a
     * JPEG and posts a user-visible "finished" toast. The probe instead
     * exercises the workflow's setup path — the same viewport quad the demo
     * builds, {@code TileCapture.create} over its bounds, and the tile-level
     * calculation — then disposes the capture reader without executing the
     * task.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "tile capture setup threw", () -> {
            MapView mv = MapView.getMapView();
            if (mv == null)
                return SelfCheckResult.skipped(id(), "MapView not ready");
            int w = mv.getWidth(), h = mv.getHeight();
            if (w <= 0 || h <= 0)
                return SelfCheckResult.skipped(id(),
                        "map view has no size yet");
            GeoPoint[] quad = new GeoPoint[] {
                    mv.inverse(new PointF(0, 0)).get(),
                    mv.inverse(new PointF(w, 0)).get(),
                    mv.inverse(new PointF(w, h)).get(),
                    mv.inverse(new PointF(0, h)).get()
            };
            for (GeoPoint corner : quad) {
                if (corner == null)
                    return SelfCheckResult.skipped(id(),
                            "map projection not ready (viewport corner unresolved)");
            }
            TileCapture tc = TileCapture
                    .create(GeoBounds.createFromPoints(quad));
            if (tc == null)
                // Same only-if condition the workflow documents: the map
                // view hasn't been created yet.
                return SelfCheckResult.skipped(id(),
                        "TileCapture.create returned null (map view not created yet)");
            try {
                int level = tc.calculateLevel(quad, h, 1);
                return SelfCheckResult.partial(id(),
                        "TileCapture created for the viewport (tile level "
                                + level
                                + "); capture task not executed (writes a file and toasts)");
            } finally {
                // The workflow disposes its capture reader once imagery is
                // captured; the probe must too or it leaks the reader.
                tc.dispose();
            }
        });
    }
}
