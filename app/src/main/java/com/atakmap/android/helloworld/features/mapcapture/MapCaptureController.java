package com.atakmap.android.helloworld.features.mapcapture;

import android.widget.LinearLayout;

import com.atakmap.android.helloworld.abstraction.Disposable;

/**
 * The offscreen map-render feature's Controller: owns the demo's state (is
 * the second render mirroring the live map right now?) while the EGL/GL
 * plumbing hides behind {@link MapCaptureCreator}. ATAK-free;
 * {@link LinearLayout} is Android, boundary-legal.
 */
public class MapCaptureController implements Disposable {

    private final MapCaptureCreator mapCaptureCreator;

    private MapCaptureHandle handle;
    private boolean capturing;

    public MapCaptureController(MapCaptureCreator mapCaptureCreator) {
        this.mapCaptureCreator = mapCaptureCreator;
    }

    /**
     * Build the offscreen pipeline into {@code container} — eagerly, at
     * pane-inflation time, exactly when the legacy receiver constructed it
     * (the blocking EGL handshake is part of the demo; see the Creator's
     * contract). Re-attaching replaces the previous pipeline, stopping its
     * mirror loop first.
     */
    public void attachRenderTarget(LinearLayout container) {
        dispose(); // replace-don't-stack; no-op on first attach
        handle = mapCaptureCreator.attachOffscreenRenderer(container);
    }

    /** Start/stop mirroring the live map into the second render. */
    public void setCapturing(boolean on) {
        if (handle == null)
            return;
        mapCaptureCreator.setCapturing(handle, on);
        capturing = on;
    }

    /**
     * Stop the mirror loop and release the pipeline. The legacy receiver
     * leaked the ~30 ms refresh thread across plugin hot-reloads when the demo
     * was left capturing; stopping it here (PaneController cascades dispose on
     * unload) fixes that — an objective legacy bug, fixed behind the seam.
     * Idempotent.
     */
    @Override
    public void dispose() {
        if (handle == null)
            return;
        if (capturing) {
            // Only ever sent after a capture(true), matching the legacy
            // toggle's strict alternation.
            mapCaptureCreator.setCapturing(handle, false);
            capturing = false;
        }
        mapCaptureCreator.detachOffscreenRenderer(handle);
        handle = null;
    }
}
