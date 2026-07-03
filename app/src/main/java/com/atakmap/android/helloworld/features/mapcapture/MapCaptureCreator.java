package com.atakmap.android.helloworld.features.mapcapture;

import android.widget.LinearLayout;

import com.atakmap.android.helloworld.abstraction.Creator;

/**
 * Abstraction over the offscreen map-render demo (the legacy "blind cast"): a
 * second {@code GLSurfaceView} grafted into the plugin's pane that mirrors the
 * live map by sharing ATAK's EGL context and periodically copying the
 * {@code GLMapView}'s frame into a texture.
 *
 * <p>The plumbing behind this seam ({@code OffscreenMapCapture}) reaches into
 * host GL-surface internals — {@code MapView.getGLSurface()},
 * {@code GLMapView.render()}, render-thread event queueing — exactly the kind
 * of low-level host coupling business logic must not name (ADR-0002).
 * Interface here; implementation in {@code src/atakShared}.
 *
 * <p>Contract quirks the seam preserves (they are the demo's teaching value):
 * <ul>
 *   <li>{@link #attachOffscreenRenderer} is EAGER and BLOCKING: it queues onto
 *   ATAK's render thread to steal the live EGL context and waits — with no
 *   timeout — for the render thread to service that event. Call it on the UI
 *   thread at pane-inflation time, as the legacy receiver did; never from the
 *   load-time systems check.</li>
 *   <li>Attaching adds a 500px-tall {@code GLSurfaceView} to the container,
 *   where it stays for the pane's lifetime — showing/hiding the container is
 *   the caller's concern, not the seam's.</li>
 *   <li>While capturing, a daemon thread queues a texture refresh onto the
 *   map's render thread every ~30 ms; {@link #setCapturing} {@code false}
 *   stops that thread and frees the GL resources on the render thread.</li>
 * </ul>
 */
public interface MapCaptureCreator extends Creator {

    /**
     * Build the offscreen render pipeline and graft its {@code GLSurfaceView}
     * into {@code container}. Blocking — see the class contract. The returned
     * handle is how the mirror loop is toggled and released.
     */
    MapCaptureHandle attachOffscreenRenderer(LinearLayout container);

    /**
     * Start ({@code true}) or stop ({@code false}) mirroring the live map into
     * the offscreen surface behind {@code handle}. Stopping ends the refresh
     * thread and tears the GL resources down on the render thread. No-op for
     * an unknown handle.
     */
    void setCapturing(MapCaptureHandle handle, boolean on);

    /**
     * Evict {@code handle} from the live registry. Does NOT stop an active
     * mirror loop — call {@link #setCapturing}{@code (handle, false)} first.
     * Idempotent.
     */
    void detachOffscreenRenderer(MapCaptureHandle handle);
}
