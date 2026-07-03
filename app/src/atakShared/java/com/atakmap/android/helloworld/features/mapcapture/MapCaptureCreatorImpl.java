package com.atakmap.android.helloworld.features.mapcapture;

import android.widget.LinearLayout;

import com.atakmap.android.helloworld.OffscreenMapCapture;
import com.atakmap.android.helloworld.abstraction.SelfCheckResult;
import com.atakmap.android.helloworld.abstraction.SelfChecks;
import com.atakmap.android.maps.MapView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The feature path's only doorway to the offscreen-render plumbing.
 * {@link OffscreenMapCapture} is the real teaching artifact — how to share
 * ATAK's EGL context with a plugin-owned {@code GLSurfaceView} and mirror the
 * {@code GLMapView} into a texture — and stays a plugin-owned class; this impl
 * fronts it with a {@link MapCaptureHandle} registry so {@code src/main} never
 * holds the GL object.
 */
public final class MapCaptureCreatorImpl implements MapCaptureCreator {

    private final Map<MapCaptureHandle, OffscreenMapCapture> live =
            new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "MapCaptureCreator";
    }

    @Override
    public MapCaptureHandle attachOffscreenRenderer(LinearLayout container) {
        // OffscreenMapCapture's constructor does the whole setup: it queues
        // onto ATAK's render thread to obtain the live EGL context (BLOCKING,
        // no timeout, until the render thread services the event), builds a
        // GLSurfaceView whose EGLContextFactory shares that context, and adds
        // it — 500px tall — to the container. Eager, on the UI thread, at
        // pane-inflation time: exactly how the legacy receiver ran it.
        OffscreenMapCapture capture = new OffscreenMapCapture(container);
        MapCaptureHandle handle = new MapCaptureHandle();
        live.put(handle, capture);
        return handle;
    }

    @Override
    public void setCapturing(MapCaptureHandle handle, boolean on) {
        OffscreenMapCapture capture = live.get(handle);
        if (capture != null)
            capture.capture(on);
    }

    @Override
    public void detachOffscreenRenderer(MapCaptureHandle handle) {
        live.remove(handle);
    }

    /**
     * PARTIAL by design: constructing the real pipeline would block the load
     * on the map's render thread with no timeout (a paused GL surface would
     * hang the systems check) and graft a GLSurfaceView onto a layout, so the
     * probe only resolves the exact call chain the constructor performs first
     * ({@code MapView.getMapView().getGLSurface()}) — the version-sensitive
     * symbols this impl links.
     */
    @Override
    public SelfCheckResult selfCheck() {
        return SelfChecks.attempt(id(), "map GL surface resolution threw",
                () -> {
                    MapView mv = MapView.getMapView();
                    if (mv == null)
                        return SelfCheckResult.skipped(id(),
                                "MapView not ready");
                    Object glSurface = mv.getGLSurface();
                    if (glSurface == null)
                        return SelfCheckResult.skipped(id(),
                                "map GL surface not ready");
                    return SelfCheckResult.partial(id(),
                            "MapView.getGLSurface() resolved; offscreen "
                                    + "pipeline not built (its constructor "
                                    + "blocks on the render thread with no "
                                    + "timeout)");
                });
    }
}
